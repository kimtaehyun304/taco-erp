package org.example.tacoerp.domain.hr.controller;

import lombok.RequiredArgsConstructor;
import org.example.tacoerp.domain.hr.entity.LeaveRequest;
import org.example.tacoerp.domain.hr.service.EmployeeService;
import org.example.tacoerp.domain.hr.service.LeaveService;
import org.example.tacoerp.domain.user.service.MenuService;
import org.example.tacoerp.global.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Controller
@RequestMapping("/hr/leave")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;
    private final EmployeeService employeeService;
    private final MenuService menuService;

    @GetMapping
    public Mono<String> list(@AuthenticationPrincipal CustomUserDetails principal,
                              @RequestParam(required = false) String status,
                              Model model) {
        model.addAttribute("currentUser", principal);
        model.addAttribute("filterStatus", status);

        var requestsMono = (status != null && !status.isBlank())
                ? leaveService.findPending().collectList()
                : leaveService.findAll().collectList();

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
        return Mono.zip(
                employeeService.findAll().collectList(),
                menuService.findByUserId(principal.getUserId()).collectList()
        ).doOnNext(t -> {
            model.addAttribute("employees", t.getT1());
            model.addAttribute("menus", t.getT2());
        }).thenReturn("hr/leave/form");
    }

    @PostMapping
    public Mono<String> create(@ModelAttribute LeaveRequest request,
                                @AuthenticationPrincipal CustomUserDetails principal) {
        return leaveService.create(request)
                .thenReturn("redirect:/hr/leave");
    }

    @PostMapping("/{id}/approve")
    public Mono<String> approve(@PathVariable Long id,
                                 @AuthenticationPrincipal CustomUserDetails principal) {
        return employeeService.findByUserId(principal.getUserId())
                .flatMap(emp -> leaveService.approve(id, emp.getId()))
                .thenReturn("redirect:/hr/leave");
    }

    @PostMapping("/{id}/reject")
    public Mono<String> reject(@PathVariable Long id) {
        return leaveService.reject(id)
                .thenReturn("redirect:/hr/leave");
    }

    @PostMapping("/{id}/delete")
    public Mono<String> delete(@PathVariable Long id) {
        return leaveService.delete(id)
                .thenReturn("redirect:/hr/leave");
    }
}
