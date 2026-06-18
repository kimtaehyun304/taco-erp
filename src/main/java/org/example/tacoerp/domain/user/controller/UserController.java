package org.example.tacoerp.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.example.tacoerp.domain.user.entity.User;
import org.example.tacoerp.domain.user.service.AuditLogService;
import org.example.tacoerp.domain.user.service.DepartmentService;
import org.example.tacoerp.domain.user.service.MenuService;
import org.example.tacoerp.domain.user.service.PositionService;
import org.example.tacoerp.domain.user.service.RoleService;
import org.example.tacoerp.domain.user.service.UserService;
import org.example.tacoerp.global.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final DepartmentService departmentService;
    private final PositionService positionService;
    private final RoleService roleService;
    private final AuditLogService auditLogService;
    private final MenuService menuService;

    @GetMapping
    public Mono<String> list(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        model.addAttribute("currentUser", principal);
        return Mono.zip(
            userService.findAll().collectList(),
            departmentService.findAll().collectList(),
            positionService.findAll().collectList(),
            menuService.findByUserId(principal.getUserId()).collectList()
        ).doOnNext(tuple -> {
            model.addAttribute("users", tuple.getT1());
            model.addAttribute("departments", tuple.getT2());
            model.addAttribute("positions", tuple.getT3());
            model.addAttribute("menus", tuple.getT4());
        }).thenReturn("admin/users/list");
    }

    @GetMapping("/new")
    public Mono<String> createForm(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        model.addAttribute("currentUser", principal);
        model.addAttribute("user", new User());
        return Mono.zip(
            departmentService.findAll().collectList(),
            positionService.findAll().collectList(),
            roleService.findAll().collectList(),
            menuService.findByUserId(principal.getUserId()).collectList()
        ).doOnNext(tuple -> {
            model.addAttribute("departments", tuple.getT1());
            model.addAttribute("positions", tuple.getT2());
            model.addAttribute("roles", tuple.getT3());
            model.addAttribute("menus", tuple.getT4());
        }).thenReturn("admin/users/form");
    }

    @GetMapping("/{id}/edit")
    public Mono<String> editForm(@PathVariable Long id,
                                  @AuthenticationPrincipal CustomUserDetails principal,
                                  Model model) {
        model.addAttribute("currentUser", principal);
        return Mono.zip(
            userService.findById(id),
            departmentService.findAll().collectList(),
            positionService.findAll().collectList(),
            roleService.findAll().collectList(),
            userService.getRoleNames(id).collectList(),
            menuService.findByUserId(principal.getUserId()).collectList()
        ).doOnNext(tuple -> {
            model.addAttribute("user", tuple.getT1());
            model.addAttribute("departments", tuple.getT2());
            model.addAttribute("positions", tuple.getT3());
            model.addAttribute("roles", tuple.getT4());
            model.addAttribute("userRoles", tuple.getT5());
            model.addAttribute("menus", tuple.getT6());
        }).thenReturn("admin/users/form");
    }

    @PostMapping
    public Mono<String> create(@ModelAttribute User user,
                                @RequestParam(value = "roleIds", required = false) List<Long> roleIds,
                                @AuthenticationPrincipal CustomUserDetails principal,
                                ServerWebExchange exchange) {
        List<Long> finalRoles = roleIds != null ? roleIds : List.of();
        return userService.create(user, finalRoles)
                .flatMap(saved -> auditLogService.log(
                    principal.getUserId(), principal.getUsername(),
                    "CREATE", "USER", String.valueOf(saved.getId()),
                    "사용자 생성: " + saved.getUsername(),
                    exchange.getRequest().getRemoteAddress() != null
                        ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown"
                ))
                .thenReturn("redirect:/admin/users");
    }

    @PostMapping("/{id}")
    public Mono<String> update(@PathVariable Long id,
                                @ModelAttribute User user,
                                @RequestParam(value = "roleIds", required = false) List<Long> roleIds,
                                @AuthenticationPrincipal CustomUserDetails principal,
                                ServerWebExchange exchange) {
        return userService.update(id, user)
                .flatMap(saved -> auditLogService.log(
                    principal.getUserId(), principal.getUsername(),
                    "UPDATE", "USER", String.valueOf(id),
                    "사용자 수정: " + saved.getUsername(),
                    exchange.getRequest().getRemoteAddress() != null
                        ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown"
                ))
                .thenReturn("redirect:/admin/users");
    }

    @PostMapping("/{id}/delete")
    public Mono<String> delete(@PathVariable Long id,
                                @AuthenticationPrincipal CustomUserDetails principal,
                                ServerWebExchange exchange) {
        return userService.findById(id)
                .flatMap(u -> userService.delete(id)
                    .then(auditLogService.log(
                        principal.getUserId(), principal.getUsername(),
                        "DELETE", "USER", String.valueOf(id),
                        "사용자 삭제: " + u.getUsername(),
                        exchange.getRequest().getRemoteAddress() != null
                            ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown"
                    ))
                )
                .thenReturn("redirect:/admin/users");
    }
}
