package org.example.tacoerp.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.example.tacoerp.domain.user.entity.Position;
import org.example.tacoerp.domain.user.service.AuditLogService;
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
@RequestMapping("/admin/positions")
@RequiredArgsConstructor
public class PositionController {

    private final PositionService positionService;
    private final AuditLogService auditLogService;
    private final MenuService menuService;

    @GetMapping
    public Mono<String> list(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        model.addAttribute("currentUser", principal);
        return Mono.zip(
            positionService.findAll().collectList(),
            menuService.findByUserId(principal.getUserId()).collectList()
        ).doOnNext(tuple -> {
            model.addAttribute("positions", tuple.getT1());
            model.addAttribute("menus", tuple.getT2());
        }).thenReturn("admin/positions/list");
    }

    @GetMapping("/new")
    public Mono<String> createForm(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        model.addAttribute("currentUser", principal);
        model.addAttribute("position", new Position());
        return menuService.findByUserId(principal.getUserId()).collectList()
                .doOnNext(menus -> model.addAttribute("menus", menus))
                .thenReturn("admin/positions/form");
    }

    @GetMapping("/{id}/edit")
    public Mono<String> editForm(@PathVariable Long id,
                                  @AuthenticationPrincipal CustomUserDetails principal,
                                  Model model) {
        model.addAttribute("currentUser", principal);
        return Mono.zip(
            positionService.findById(id),
            menuService.findByUserId(principal.getUserId()).collectList()
        ).doOnNext(tuple -> {
            model.addAttribute("position", tuple.getT1());
            model.addAttribute("menus", tuple.getT2());
        }).thenReturn("admin/positions/form");
    }

    @PostMapping
    public Mono<String> create(@ModelAttribute Position position,
                                @AuthenticationPrincipal CustomUserDetails principal,
                                ServerWebExchange exchange) {
        return positionService.create(position)
                .flatMap(saved -> auditLogService.log(
                    principal.getUserId(), principal.getUsername(),
                    "CREATE", "POSITION", String.valueOf(saved.getId()),
                    "직급 생성: " + saved.getName(),
                    exchange.getRequest().getRemoteAddress() != null
                        ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown"
                ))
                .thenReturn("redirect:/admin/positions");
    }

    @PostMapping("/{id}")
    public Mono<String> update(@PathVariable Long id, @ModelAttribute Position position,
                                @AuthenticationPrincipal CustomUserDetails principal,
                                ServerWebExchange exchange) {
        return positionService.update(id, position)
                .flatMap(saved -> auditLogService.log(
                    principal.getUserId(), principal.getUsername(),
                    "UPDATE", "POSITION", String.valueOf(id),
                    "직급 수정: " + saved.getName(),
                    exchange.getRequest().getRemoteAddress() != null
                        ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown"
                ))
                .thenReturn("redirect:/admin/positions");
    }

    @PostMapping("/{id}/delete")
    public Mono<String> delete(@PathVariable Long id,
                                @AuthenticationPrincipal CustomUserDetails principal,
                                ServerWebExchange exchange) {
        return positionService.findById(id)
                .flatMap(p -> positionService.delete(id)
                    .then(auditLogService.log(
                        principal.getUserId(), principal.getUsername(),
                        "DELETE", "POSITION", String.valueOf(id),
                        "직급 삭제: " + p.getName(),
                        exchange.getRequest().getRemoteAddress() != null
                            ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown"
                    ))
                )
                .thenReturn("redirect:/admin/positions");
    }
}
