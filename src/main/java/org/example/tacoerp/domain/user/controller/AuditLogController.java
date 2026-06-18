package org.example.tacoerp.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.example.tacoerp.domain.user.service.AuditLogService;
import org.example.tacoerp.domain.user.service.MenuService;
import org.example.tacoerp.global.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/admin/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;
    private final MenuService menuService;

    @GetMapping
    public Mono<String> list(@AuthenticationPrincipal CustomUserDetails principal,
                              @RequestParam(required = false) String username,
                              Model model) {
        model.addAttribute("currentUser", principal);
        model.addAttribute("searchUsername", username);

        var logsMono = (username != null && !username.isBlank())
            ? auditLogService.searchByUsername(username).collectList()
            : auditLogService.findRecent(100).collectList();

        return Mono.zip(
            logsMono,
            menuService.findByUserId(principal.getUserId()).collectList()
        ).doOnNext(tuple -> {
            model.addAttribute("logs", tuple.getT1());
            model.addAttribute("menus", tuple.getT2());
        }).thenReturn("admin/audit-logs/list");
    }
}
