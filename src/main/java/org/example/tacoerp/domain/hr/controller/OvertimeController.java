package org.example.tacoerp.domain.hr.controller;

import lombok.RequiredArgsConstructor;
import org.example.tacoerp.domain.hr.entity.OvertimeRequest;
import org.example.tacoerp.domain.hr.service.EmployeeService;
import org.example.tacoerp.domain.hr.service.OvertimeService;
import org.example.tacoerp.domain.user.service.AuditLogService;
import org.example.tacoerp.domain.user.service.MenuService;
import org.example.tacoerp.global.security.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/hr/overtime")
@RequiredArgsConstructor
public class OvertimeController {

    private final OvertimeService overtimeService;
    private final EmployeeService employeeService;
    private final MenuService menuService;
    private final AuditLogService auditLogService;

    private String ipOf(ServerWebExchange exchange) {
        return exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }

    /** ADMIN / HR 여부 (승인 권한) */
    private boolean isManager(CustomUserDetails principal) {
        return principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_HR"));
    }

    /** 팀장 이상 여부 (자동 승인 대상) */
    private boolean isManagerOrAbove(CustomUserDetails principal) {
        return principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                        || a.getAuthority().equals("ROLE_HR")
                        || a.getAuthority().equals("ROLE_MANAGER"));
    }

    @GetMapping
    public Mono<String> list(@AuthenticationPrincipal CustomUserDetails principal,
                             @RequestParam(required = false) String status,
                             Model model) {
        model.addAttribute("currentUser", principal);
        model.addAttribute("filterStatus", status);
        boolean manager = isManager(principal);
        model.addAttribute("isManager", manager);

        var requestsMono = manager
                ? ("PENDING".equals(status) ? overtimeService.findPending() : overtimeService.findAll()).collectList()
                : employeeService.findByUserId(principal.getUserId())
                        .flatMapMany(emp -> overtimeService.findByEmployee(emp.getId()))
                        .collectList();

        return Mono.zip(
                requestsMono,
                employeeService.findAll().collectList(),
                menuService.findByUserId(principal.getUserId()).collectList()
        ).doOnNext(t -> {
            model.addAttribute("requests", t.getT1());
            model.addAttribute("employees", t.getT2());
            model.addAttribute("menus", t.getT3());
        }).thenReturn("hr/overtime/list");
    }

    @GetMapping("/new")
    public Mono<String> createForm(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        model.addAttribute("currentUser", principal);
        boolean manager = isManager(principal);
        model.addAttribute("isManager", manager);
        model.addAttribute("autoApprove", isManagerOrAbove(principal));

        return Mono.zip(
                employeeService.findAll().collectList(),
                menuService.findByUserId(principal.getUserId()).collectList(),
                employeeService.findByUserId(principal.getUserId()).map(e -> e.getId()).defaultIfEmpty(-1L)
        ).doOnNext(t -> {
            model.addAttribute("employees", t.getT1());
            model.addAttribute("menus", t.getT2());
            model.addAttribute("myEmployeeId", t.getT3());
        }).thenReturn("hr/overtime/form");
    }

    @PostMapping
    public Mono<String> create(@ModelAttribute OvertimeRequest request,
                               @AuthenticationPrincipal CustomUserDetails principal,
                               ServerWebExchange exchange) {
        boolean manager = isManager(principal);
        boolean managerOrAbove = isManagerOrAbove(principal);

        if (manager) {
            return overtimeService.create(request, true, principal.getUserId())
                    .flatMap(saved -> auditLogService.log(
                            principal.getUserId(), principal.getUsername(),
                            "CREATE", "OVERTIME_REQUEST", String.valueOf(saved.getId()),
                            "초과근무 등록(자동승인): 직원ID " + saved.getEmployeeId()
                                    + ", " + saved.getWorkDate() + " " + saved.getHours() + "h",
                            ipOf(exchange)
                    ))
                    .thenReturn("redirect:/hr/overtime");
        }

        return employeeService.findByUserId(principal.getUserId())
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "사원 정보가 없습니다.")))
                .flatMap(emp -> {
                    request.setEmployeeId(emp.getId());
                    return overtimeService.create(request, managerOrAbove, principal.getUserId());
                })
                .flatMap(saved -> auditLogService.log(
                        principal.getUserId(), principal.getUsername(),
                        "CREATE", "OVERTIME_REQUEST", String.valueOf(saved.getId()),
                        "초과근무 신청" + (managerOrAbove ? "(자동승인)" : "")
                                + ": " + saved.getWorkDate() + " " + saved.getHours() + "h",
                        ipOf(exchange)
                ))
                .thenReturn("redirect:/hr/overtime");
    }

    @PostMapping("/{id}/approve")
    public Mono<String> approve(@PathVariable Long id,
                                @AuthenticationPrincipal CustomUserDetails principal,
                                ServerWebExchange exchange) {
        if (!isManager(principal)) {
            return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "승인 권한이 없습니다."));
        }
        return employeeService.findByUserId(principal.getUserId())
                .map(emp -> emp.getId())
                .defaultIfEmpty(principal.getUserId())
                .flatMap(approverId -> overtimeService.approve(id, approverId))
                .flatMap(saved -> auditLogService.log(
                        principal.getUserId(), principal.getUsername(),
                        "APPROVE", "OVERTIME_REQUEST", String.valueOf(id),
                        "초과근무 승인: 직원ID " + saved.getEmployeeId() + ", " + saved.getWorkDate(),
                        ipOf(exchange)
                ))
                .thenReturn("redirect:/hr/overtime");
    }

    @PostMapping("/{id}/reject")
    public Mono<String> reject(@PathVariable Long id,
                               @AuthenticationPrincipal CustomUserDetails principal,
                               ServerWebExchange exchange) {
        if (!isManager(principal)) {
            return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "반려 권한이 없습니다."));
        }
        return overtimeService.reject(id)
                .flatMap(saved -> auditLogService.log(
                        principal.getUserId(), principal.getUsername(),
                        "REJECT", "OVERTIME_REQUEST", String.valueOf(id),
                        "초과근무 반려: 직원ID " + saved.getEmployeeId() + ", " + saved.getWorkDate(),
                        ipOf(exchange)
                ))
                .thenReturn("redirect:/hr/overtime");
    }

    @PostMapping("/{id}/delete")
    public Mono<String> delete(@PathVariable Long id,
                               @AuthenticationPrincipal CustomUserDetails principal,
                               ServerWebExchange exchange) {
        if (isManager(principal)) {
            return overtimeService.findById(id)
                    .flatMap(req -> overtimeService.delete(id)
                            .then(auditLogService.log(
                                    principal.getUserId(), principal.getUsername(),
                                    "DELETE", "OVERTIME_REQUEST", String.valueOf(id),
                                    "초과근무 신청 삭제: 직원ID " + req.getEmployeeId() + ", " + req.getWorkDate(),
                                    ipOf(exchange)
                            )))
                    .thenReturn("redirect:/hr/overtime");
        }
        return employeeService.findByUserId(principal.getUserId())
                .flatMap(emp -> overtimeService.findById(id)
                        .flatMap(req -> {
                            if (!req.getEmployeeId().equals(emp.getId())) {
                                return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "본인 신청 건만 취소할 수 있습니다."));
                            }
                            return overtimeService.delete(id)
                                    .then(auditLogService.log(
                                            principal.getUserId(), principal.getUsername(),
                                            "DELETE", "OVERTIME_REQUEST", String.valueOf(id),
                                            "초과근무 신청 취소: " + req.getWorkDate() + " " + req.getHours() + "h",
                                            ipOf(exchange)
                                    ));
                        }))
                .thenReturn("redirect:/hr/overtime");
    }
}
