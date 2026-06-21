package org.example.tacoerp.domain.hr.controller;

import lombok.RequiredArgsConstructor;
import org.example.tacoerp.domain.hr.entity.Employee;
import org.example.tacoerp.domain.hr.service.EmployeeService;
import org.example.tacoerp.domain.user.service.AuditLogService;
import org.example.tacoerp.domain.user.service.DepartmentService;
import org.example.tacoerp.domain.user.service.MenuService;
import org.example.tacoerp.domain.user.service.PositionService;
import org.example.tacoerp.global.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/hr/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    private final DepartmentService departmentService;
    private final PositionService positionService;
    private final MenuService menuService;
    private final AuditLogService auditLogService;

    private String ipOf(ServerWebExchange exchange) {
        return exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }

    @GetMapping
    public Mono<String> list(@AuthenticationPrincipal CustomUserDetails principal,
                              @RequestParam(required = false) String keyword,
                              @RequestParam(required = false) String status,
                              Model model) {
        model.addAttribute("currentUser", principal);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);

        var empMono = (keyword != null && !keyword.isBlank())
                ? employeeService.findByName(keyword).collectList()
                : (status != null && !status.isBlank())
                    ? employeeService.findByStatus(status).collectList()
                    : employeeService.findAll().collectList();

        return Mono.zip(
                empMono,
                departmentService.findAll().collectList(),
                positionService.findAll().collectList(),
                menuService.findByUserId(principal.getUserId()).collectList()
        ).doOnNext(t -> {
            model.addAttribute("employees", t.getT1());
            model.addAttribute("departments", t.getT2());
            model.addAttribute("positions", t.getT3());
            model.addAttribute("menus", t.getT4());
        }).thenReturn("hr/employees/list");
    }

    @GetMapping("/{id}")
    public Mono<String> detail(@PathVariable Long id,
                                @AuthenticationPrincipal CustomUserDetails principal,
                                Model model) {
        model.addAttribute("currentUser", principal);
        return Mono.zip(
                employeeService.findById(id),
                departmentService.findAll().collectList(),
                positionService.findAll().collectList(),
                menuService.findByUserId(principal.getUserId()).collectList()
        ).doOnNext(t -> {
            model.addAttribute("employee", t.getT1());
            model.addAttribute("departments", t.getT2());
            model.addAttribute("positions", t.getT3());
            model.addAttribute("menus", t.getT4());
        }).thenReturn("hr/employees/detail");
    }

    @GetMapping("/new")
    public Mono<String> createForm(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        model.addAttribute("currentUser", principal);
        model.addAttribute("employee", new Employee());
        return Mono.zip(
                departmentService.findAll().collectList(),
                positionService.findAll().collectList(),
                menuService.findByUserId(principal.getUserId()).collectList()
        ).doOnNext(t -> {
            model.addAttribute("departments", t.getT1());
            model.addAttribute("positions", t.getT2());
            model.addAttribute("menus", t.getT3());
        }).thenReturn("hr/employees/form");
    }

    @GetMapping("/{id}/edit")
    public Mono<String> editForm(@PathVariable Long id,
                                  @AuthenticationPrincipal CustomUserDetails principal,
                                  Model model) {
        model.addAttribute("currentUser", principal);
        return Mono.zip(
                employeeService.findById(id),
                departmentService.findAll().collectList(),
                positionService.findAll().collectList(),
                menuService.findByUserId(principal.getUserId()).collectList()
        ).doOnNext(t -> {
            model.addAttribute("employee", t.getT1());
            model.addAttribute("departments", t.getT2());
            model.addAttribute("positions", t.getT3());
            model.addAttribute("menus", t.getT4());
        }).thenReturn("hr/employees/form");
    }

    @PostMapping
    public Mono<String> create(@ModelAttribute Employee employee,
                                @AuthenticationPrincipal CustomUserDetails principal,
                                ServerWebExchange exchange) {
        return employeeService.create(employee)
                .flatMap(saved -> auditLogService.log(
                        principal.getUserId(), principal.getUsername(),
                        "CREATE", "EMPLOYEE", String.valueOf(saved.getId()),
                        "직원 등록: " + saved.getName() + " (" + saved.getEmployeeNo() + ")",
                        ipOf(exchange)
                ))
                .thenReturn("redirect:/hr/employees");
    }

    @PostMapping("/{id}")
    public Mono<String> update(@PathVariable Long id, @ModelAttribute Employee employee,
                                @AuthenticationPrincipal CustomUserDetails principal,
                                ServerWebExchange exchange) {
        return employeeService.update(id, employee)
                .flatMap(saved -> auditLogService.log(
                        principal.getUserId(), principal.getUsername(),
                        "UPDATE", "EMPLOYEE", String.valueOf(id),
                        "직원 정보 수정: " + saved.getName() + " (" + saved.getEmployeeNo() + ")",
                        ipOf(exchange)
                ))
                .thenReturn("redirect:/hr/employees/" + id);
    }

    @PostMapping("/{id}/delete")
    public Mono<String> delete(@PathVariable Long id,
                                @AuthenticationPrincipal CustomUserDetails principal,
                                ServerWebExchange exchange) {
        return employeeService.findById(id)
                .flatMap(emp -> employeeService.delete(id)
                        .then(auditLogService.log(
                                principal.getUserId(), principal.getUsername(),
                                "DELETE", "EMPLOYEE", String.valueOf(id),
                                "직원 삭제: " + emp.getName() + " (" + emp.getEmployeeNo() + ")",
                                ipOf(exchange)
                        ))
                )
                .thenReturn("redirect:/hr/employees");
    }
}
