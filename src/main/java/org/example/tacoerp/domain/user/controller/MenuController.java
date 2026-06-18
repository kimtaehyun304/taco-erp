package org.example.tacoerp.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.example.tacoerp.domain.user.entity.Menu;
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

import java.util.List;

@Controller
@RequestMapping("/admin/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;
    private final RoleService roleService;
    private final AuditLogService auditLogService;

    @GetMapping
    public Mono<String> list(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        model.addAttribute("currentUser", principal);
        return Mono.zip(
            menuService.findAll().collectList(),
            roleService.findAll().collectList(),
            menuService.findByUserId(principal.getUserId()).collectList()
        ).doOnNext(tuple -> {
            model.addAttribute("menus", tuple.getT3());
            model.addAttribute("allMenus", tuple.getT1());
            model.addAttribute("roles", tuple.getT2());
        }).thenReturn("admin/menus/list");
    }

    @GetMapping("/new")
    public Mono<String> createForm(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        model.addAttribute("currentUser", principal);
        model.addAttribute("menu", new Menu());
        return Mono.zip(
            menuService.findAll().collectList(),
            roleService.findAll().collectList(),
            menuService.findByUserId(principal.getUserId()).collectList()
        ).doOnNext(tuple -> {
            model.addAttribute("allMenus", tuple.getT1());
            model.addAttribute("roles", tuple.getT2());
            model.addAttribute("menus", tuple.getT3());
        }).thenReturn("admin/menus/form");
    }

    @GetMapping("/{id}/edit")
    public Mono<String> editForm(@PathVariable Long id,
                                  @AuthenticationPrincipal CustomUserDetails principal,
                                  Model model) {
        model.addAttribute("currentUser", principal);
        return Mono.zip(
            menuService.findById(id),
            menuService.findAll().collectList(),
            roleService.findAll().collectList(),
            menuService.findRolesByMenuId(id).map(mr -> mr.getRoleId()).collectList(),
            menuService.findByUserId(principal.getUserId()).collectList()
        ).doOnNext(tuple -> {
            model.addAttribute("menu", tuple.getT1());
            model.addAttribute("allMenus", tuple.getT2());
            model.addAttribute("roles", tuple.getT3());
            model.addAttribute("menuRoleIds", tuple.getT4());
            model.addAttribute("menus", tuple.getT5());
        }).thenReturn("admin/menus/form");
    }

    @PostMapping
    public Mono<String> create(@ModelAttribute Menu menu,
                                @RequestParam(value = "roleIds", required = false) List<Long> roleIds,
                                @AuthenticationPrincipal CustomUserDetails principal,
                                ServerWebExchange exchange) {
        List<Long> finalRoles = roleIds != null ? roleIds : List.of();
        return menuService.create(menu, finalRoles)
                .flatMap(saved -> auditLogService.log(
                    principal.getUserId(), principal.getUsername(),
                    "CREATE", "MENU", String.valueOf(saved.getId()),
                    "메뉴 생성: " + saved.getName(),
                    exchange.getRequest().getRemoteAddress() != null
                        ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown"
                ))
                .thenReturn("redirect:/admin/menus");
    }

    @PostMapping("/{id}")
    public Mono<String> update(@PathVariable Long id, @ModelAttribute Menu menu,
                                @RequestParam(value = "roleIds", required = false) List<Long> roleIds,
                                @AuthenticationPrincipal CustomUserDetails principal,
                                ServerWebExchange exchange) {
        List<Long> finalRoles = roleIds != null ? roleIds : List.of();
        return menuService.update(id, menu, finalRoles)
                .flatMap(saved -> auditLogService.log(
                    principal.getUserId(), principal.getUsername(),
                    "UPDATE", "MENU", String.valueOf(id),
                    "메뉴 수정: " + saved.getName(),
                    exchange.getRequest().getRemoteAddress() != null
                        ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown"
                ))
                .thenReturn("redirect:/admin/menus");
    }

    @PostMapping("/{id}/delete")
    public Mono<String> delete(@PathVariable Long id,
                                @AuthenticationPrincipal CustomUserDetails principal,
                                ServerWebExchange exchange) {
        return menuService.findById(id)
                .flatMap(m -> menuService.delete(id)
                    .then(auditLogService.log(
                        principal.getUserId(), principal.getUsername(),
                        "DELETE", "MENU", String.valueOf(id),
                        "메뉴 삭제: " + m.getName(),
                        exchange.getRequest().getRemoteAddress() != null
                            ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown"
                    ))
                )
                .thenReturn("redirect:/admin/menus");
    }
}
