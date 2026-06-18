package org.example.tacoerp.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.example.tacoerp.domain.user.entity.Role;
import org.example.tacoerp.domain.user.service.AuditLogService;
import org.example.tacoerp.domain.user.service.MenuService;
import org.example.tacoerp.domain.user.service.RoleService;
import org.example.tacoerp.global.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/admin/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;
    private final AuditLogService auditLogService;
    private final MenuService menuService;

    @GetMapping
    public Mono<String> list(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        model.addAttribute("currentUser", principal);
        return Mono.zip(
            roleService.findAll().collectList(),
            menuService.findByUserId(principal.getUserId()).collectList()
        ).doOnNext(tuple -> {
            model.addAttribute("roles", tuple.getT1());
            model.addAttribute("menus", tuple.getT2());
        }).thenReturn("admin/roles/list");
    }

    @GetMapping("/new")
    public Mono<String> createForm(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        model.addAttribute("currentUser", principal);
        model.addAttribute("role", new Role());
        return menuService.findByUserId(principal.getUserId()).collectList()
                .doOnNext(menus -> model.addAttribute("menus", menus))
                .thenReturn("admin/roles/form");
    }

    @GetMapping("/{id}/edit")
    public Mono<String> editForm(@PathVariable Long id,
                                  @AuthenticationPrincipal CustomUserDetails principal,
                                  Model model) {
        model.addAttribute("currentUser", principal);
        return Mono.zip(
            roleService.findById(id),
            menuService.findByUserId(principal.getUserId()).collectList()
        ).doOnNext(tuple -> {
            model.addAttribute("role", tuple.getT1());
            model.addAttribute("menus", tuple.getT2());
        }).thenReturn("admin/roles/form");
    }

    @PostMapping
    public Mono<String> create(@ModelAttribute Role role,
                                @AuthenticationPrincipal CustomUserDetails principal,
                                ServerWebExchange exchange) {
        return roleService.create(role)
                .flatMap(saved -> auditLogService.log(
                    principal.getUserId(), principal.getUsername(),
                    "CREATE", "ROLE", String.valueOf(saved.getId()),
                    "역할 생성: " + saved.getName(),
                    exchange.getRequest().getRemoteAddress() != null
                        ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown"
                ))
                .thenReturn("redirect:/admin/roles");
    }

    @PostMapping("/{id}")
    public Mono<String> update(@PathVariable Long id, @ModelAttribute Role role,
                                @AuthenticationPrincipal CustomUserDetails principal,
                                ServerWebExchange exchange) {
        return roleService.update(id, role)
                .flatMap(saved -> auditLogService.log(
                    principal.getUserId(), principal.getUsername(),
                    "UPDATE", "ROLE", String.valueOf(id),
                    "역할 수정: " + saved.getName(),
                    exchange.getRequest().getRemoteAddress() != null
                        ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown"
                ))
                .thenReturn("redirect:/admin/roles");
    }

    @PostMapping("/{id}/delete")
    public Mono<String> delete(@PathVariable Long id,
                                @AuthenticationPrincipal CustomUserDetails principal,
                                ServerWebExchange exchange) {
        return roleService.findById(id)
                .flatMap(r -> roleService.delete(id)
                    .then(auditLogService.log(
                        principal.getUserId(), principal.getUsername(),
                        "DELETE", "ROLE", String.valueOf(id),
                        "역할 삭제: " + r.getName(),
                        exchange.getRequest().getRemoteAddress() != null
                            ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown"
                    ))
                )
                .thenReturn("redirect:/admin/roles");
    }
}
