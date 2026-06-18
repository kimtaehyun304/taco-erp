package org.example.tacoerp.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.example.tacoerp.domain.user.entity.Department;
import org.example.tacoerp.domain.user.service.AuditLogService;
import org.example.tacoerp.domain.user.service.DepartmentService;
import org.example.tacoerp.domain.user.service.MenuService;
import org.example.tacoerp.global.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/admin/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;
    private final AuditLogService auditLogService;
    private final MenuService menuService;

    @GetMapping
    public Mono<String> list(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        model.addAttribute("currentUser", principal);
        return Mono.zip(
            departmentService.findAll().collectList(),
            menuService.findByUserId(principal.getUserId()).collectList()
        ).doOnNext(tuple -> {
            model.addAttribute("departments", tuple.getT1());
            model.addAttribute("menus", tuple.getT2());
        }).thenReturn("admin/departments/list");
    }

    @GetMapping("/new")
    public Mono<String> createForm(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        model.addAttribute("currentUser", principal);
        model.addAttribute("dept", new Department());
        return Mono.zip(
            departmentService.findAll().collectList(),
            menuService.findByUserId(principal.getUserId()).collectList()
        ).doOnNext(tuple -> {
            model.addAttribute("departments", tuple.getT1());
            model.addAttribute("menus", tuple.getT2());
        }).thenReturn("admin/departments/form");
    }

    @GetMapping("/{id}/edit")
    public Mono<String> editForm(@PathVariable Long id,
                                  @AuthenticationPrincipal CustomUserDetails principal,
                                  Model model) {
        model.addAttribute("currentUser", principal);
        return Mono.zip(
            departmentService.findById(id),
            departmentService.findAll().collectList(),
            menuService.findByUserId(principal.getUserId()).collectList()
        ).doOnNext(tuple -> {
            model.addAttribute("dept", tuple.getT1());
            model.addAttribute("departments", tuple.getT2());
            model.addAttribute("menus", tuple.getT3());
        }).thenReturn("admin/departments/form");
    }

    @PostMapping
    public Mono<String> create(@ModelAttribute Department dept,
                                @AuthenticationPrincipal CustomUserDetails principal,
                                ServerWebExchange exchange) {
        return departmentService.create(dept)
                .flatMap(saved -> auditLogService.log(
                    principal.getUserId(), principal.getUsername(),
                    "CREATE", "DEPARTMENT", String.valueOf(saved.getId()),
                    "부서 생성: " + saved.getName(),
                    exchange.getRequest().getRemoteAddress() != null
                        ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown"
                ))
                .thenReturn("redirect:/admin/departments");
    }

    @PostMapping("/{id}")
    public Mono<String> update(@PathVariable Long id,
                                @ModelAttribute Department dept,
                                @AuthenticationPrincipal CustomUserDetails principal,
                                ServerWebExchange exchange) {
        return departmentService.update(id, dept)
                .flatMap(saved -> auditLogService.log(
                    principal.getUserId(), principal.getUsername(),
                    "UPDATE", "DEPARTMENT", String.valueOf(id),
                    "부서 수정: " + saved.getName(),
                    exchange.getRequest().getRemoteAddress() != null
                        ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown"
                ))
                .thenReturn("redirect:/admin/departments");
    }

    @PostMapping("/{id}/delete")
    public Mono<String> delete(@PathVariable Long id,
                                @AuthenticationPrincipal CustomUserDetails principal,
                                ServerWebExchange exchange) {
        return departmentService.findById(id)
                .flatMap(d -> departmentService.delete(id)
                    .then(auditLogService.log(
                        principal.getUserId(), principal.getUsername(),
                        "DELETE", "DEPARTMENT", String.valueOf(id),
                        "부서 삭제: " + d.getName(),
                        exchange.getRequest().getRemoteAddress() != null
                            ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown"
                    ))
                )
                .thenReturn("redirect:/admin/departments");
    }
}
