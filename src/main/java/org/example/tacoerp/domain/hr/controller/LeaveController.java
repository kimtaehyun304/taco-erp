package org.example.tacoerp.domain.hr.controller;

import lombok.RequiredArgsConstructor;
import org.example.tacoerp.domain.hr.entity.LeaveRequest;
import org.example.tacoerp.domain.hr.service.EmployeeService;
import org.example.tacoerp.domain.hr.service.LeaveService;
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

import java.util.List;

@Controller
@RequestMapping("/hr/leave")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;
    private final EmployeeService employeeService;
    private final MenuService menuService;
    private final AuditLogService auditLogService;

    private String ipOf(ServerWebExchange exchange) {
        return exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }

    private boolean isManager(CustomUserDetails principal) {
        return principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_HR"));
    }

    /** 팀장 이상(MANAGER/ADMIN/HR) 여부 - 본인 휴가 신청 시 자동 승인 대상 판단용 */
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

        // 일반 사용자는 본인 신청 내역만, 관리자/HR은 전체(또는 대기중) 조회
        Mono<List<LeaveRequest>> requestsMono;
        if (!manager) {
            requestsMono = employeeService.findByUserId(principal.getUserId())
                    .flatMapMany(emp -> leaveService.findByEmployee(emp.getId()))
                    .collectList();
        } else {
            requestsMono = (status != null && !status.isBlank())
                    ? leaveService.findPending().collectList()
                    : leaveService.findAll().collectList();
        }

        return Mono.zip(
                requestsMono,
                employeeService.findAll().collectList(),
                menuService.findByUserId(principal.getUserId()).collectList()
        ).doOnNext(t -> {
            model.addAttribute("requests", t.getT1());
            model.addAttribute("employees", t.getT2());
            model.addAttribute("menus", t.getT3());
        }).thenReturn("hr/leave/list");
    }

    @GetMapping("/new")
    public Mono<String> createForm(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        model.addAttribute("currentUser", principal);
        model.addAttribute("request", new LeaveRequest());
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
        }).thenReturn("hr/leave/form");
    }

    @PostMapping
    public Mono<String> create(@ModelAttribute LeaveRequest request,
                                @AuthenticationPrincipal CustomUserDetails principal,
                                ServerWebExchange exchange) {
        boolean manager = isManager(principal);
        boolean managerOrAbove = isManagerOrAbove(principal);

        if (manager) {
            // 관리자/HR은 임의의 직원 명의로 등록 가능 (폼에서 넘어온 employeeId 그대로 사용)
            // ADMIN/HR이 등록하는 신청은 본인 명의든 다른 직원 대리 등록이든 결재 없이 자동 승인
            return leaveService.create(request, true, principal.getUserId())
                    .flatMap(saved -> auditLogService.log(
                            principal.getUserId(), principal.getUsername(),
                            "CREATE", "LEAVE_REQUEST", String.valueOf(saved.getId()),
                            "휴가 등록(자동승인): 직원ID " + saved.getEmployeeId() + ", "
                                    + saved.getStartDate() + " ~ " + saved.getEndDate() + " (" + saved.getDays() + "일)",
                            ipOf(exchange)
                    ))
                    .thenReturn("redirect:/hr/leave");
        }

        // 일반 사용자는 반드시 본인 employeeId로만 신청 가능 (위변조 방지)
        return employeeService.findByUserId(principal.getUserId())
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "사원 정보가 없습니다.")))
                .flatMap(emp -> {
                    request.setEmployeeId(emp.getId());
                    // 팀장(MANAGER) 본인 신청은 결재 단계 없이 자동 승인
                    return leaveService.create(request, managerOrAbove, principal.getUserId());
                })
                .flatMap(saved -> auditLogService.log(
                        principal.getUserId(), principal.getUsername(),
                        "CREATE", "LEAVE_REQUEST", String.valueOf(saved.getId()),
                        "휴가 신청" + (managerOrAbove ? "(자동승인)" : "") + ": "
                                + saved.getStartDate() + " ~ " + saved.getEndDate() + " (" + saved.getDays() + "일)",
                        ipOf(exchange)
                ))
                .thenReturn("redirect:/hr/leave");
    }

    // 승인은 관리자/HR만 (SecurityConfig에서도 차단되지만 컨트롤러에서도 한 번 더 방어)
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
                .flatMap(approverId -> leaveService.approve(id, approverId))
                .flatMap(saved -> auditLogService.log(
                        principal.getUserId(), principal.getUsername(),
                        "APPROVE", "LEAVE_REQUEST", String.valueOf(id),
                        "휴가 승인: 직원ID " + saved.getEmployeeId() + ", "
                                + saved.getStartDate() + " ~ " + saved.getEndDate(),
                        ipOf(exchange)
                ))
                .thenReturn("redirect:/hr/leave");
    }

    // 반려는 관리자/HR만
    @PostMapping("/{id}/reject")
    public Mono<String> reject(@PathVariable Long id,
                                @AuthenticationPrincipal CustomUserDetails principal,
                                ServerWebExchange exchange) {
        if (!isManager(principal)) {
            return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "반려 권한이 없습니다."));
        }
        return leaveService.reject(id)
                .flatMap(saved -> auditLogService.log(
                        principal.getUserId(), principal.getUsername(),
                        "REJECT", "LEAVE_REQUEST", String.valueOf(id),
                        "휴가 반려: 직원ID " + saved.getEmployeeId() + ", "
                                + saved.getStartDate() + " ~ " + saved.getEndDate(),
                        ipOf(exchange)
                ))
                .thenReturn("redirect:/hr/leave");
    }

    // 삭제(취소)는 본인 신청 건이거나 관리자/HR만 가능
    @PostMapping("/{id}/delete")
    public Mono<String> delete(@PathVariable Long id,
                                @AuthenticationPrincipal CustomUserDetails principal,
                                ServerWebExchange exchange) {
        if (isManager(principal)) {
            return leaveService.findById(id)
                    .flatMap(req -> leaveService.delete(id)
                            .then(auditLogService.log(
                                    principal.getUserId(), principal.getUsername(),
                                    "DELETE", "LEAVE_REQUEST", String.valueOf(id),
                                    "휴가 신청 삭제: 직원ID " + req.getEmployeeId() + ", "
                                            + req.getStartDate() + " ~ " + req.getEndDate(),
                                    ipOf(exchange)
                            )))
                    .thenReturn("redirect:/hr/leave");
        }
        return employeeService.findByUserId(principal.getUserId())
                .flatMap(emp -> leaveService.findById(id)
                        .flatMap(req -> {
                            if (!req.getEmployeeId().equals(emp.getId())) {
                                return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "본인 신청 건만 취소할 수 있습니다."));
                            }
                            return leaveService.delete(id)
                                    .then(auditLogService.log(
                                            principal.getUserId(), principal.getUsername(),
                                            "DELETE", "LEAVE_REQUEST", String.valueOf(id),
                                            "휴가 신청 취소: " + req.getStartDate() + " ~ " + req.getEndDate(),
                                            ipOf(exchange)
                                    ));
                        }))
                .thenReturn("redirect:/hr/leave");
    }
}
