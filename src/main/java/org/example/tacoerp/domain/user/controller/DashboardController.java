package org.example.tacoerp.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.example.tacoerp.domain.user.service.AuditLogService;
import org.example.tacoerp.domain.user.service.DepartmentService;
import org.example.tacoerp.domain.user.service.MenuService;
import org.example.tacoerp.domain.user.service.UserService;
import org.example.tacoerp.global.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final UserService userService;
    private final DepartmentService departmentService;
    private final AuditLogService auditLogService;
    private final MenuService menuService;

    @GetMapping({"/", "/dashboard"})
    public Mono<String> dashboard(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        model.addAttribute("currentUser", principal);

        return Mono.zip(
            userService.findAll().count(),
            departmentService.findAll().count(),
            auditLogService.findRecent(5).collectList(),
            menuService.findByUserId(principal.getUserId()).collectList()
        ).doOnNext(tuple -> {
            model.addAttribute("userCount", tuple.getT1());
            model.addAttribute("deptCount", tuple.getT2());
            model.addAttribute("recentLogs", tuple.getT3());
            model.addAttribute("menus", tuple.getT4());
        }).thenReturn("dashboard");
    }
}
