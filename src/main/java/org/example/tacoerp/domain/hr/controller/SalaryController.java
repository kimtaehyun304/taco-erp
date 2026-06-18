package org.example.tacoerp.domain.hr.controller;

import lombok.RequiredArgsConstructor;
import org.example.tacoerp.domain.hr.entity.Salary;
import org.example.tacoerp.domain.hr.service.EmployeeService;
import org.example.tacoerp.domain.hr.service.SalaryService;
import org.example.tacoerp.domain.user.service.MenuService;
import org.example.tacoerp.global.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/hr/salary")
@RequiredArgsConstructor
public class SalaryController {

    private final SalaryService salaryService;
    private final EmployeeService employeeService;
    private final MenuService menuService;

    @GetMapping
    public Mono<String> list(@AuthenticationPrincipal CustomUserDetails principal,
                              @RequestParam(required = false) Long employeeId,
                              Model model) {
        model.addAttribute("currentUser", principal);
        model.addAttribute("selectedEmployeeId", employeeId);

        return employeeService.findAll().collectList()
                .flatMap(employees -> {
                    model.addAttribute("employees", employees);
                    Long empId = employeeId != null ? employeeId
                            : (employees.isEmpty() ? null : employees.get(0).getId());

                    if (empId == null) {
                        model.addAttribute("salaries", java.util.List.of());
                        return menuService.findByUserId(principal.getUserId()).collectList()
                                .doOnNext(m -> model.addAttribute("menus", m))
                                .thenReturn("hr/salary/list");
                    }

                    final Long finalEmpId = empId;
                    model.addAttribute("selectedEmployeeId", finalEmpId);
                    return Mono.zip(
                            salaryService.findByEmployee(finalEmpId).collectList(),
                            menuService.findByUserId(principal.getUserId()).collectList()
                    ).doOnNext(t -> {
                        model.addAttribute("salaries", t.getT1());
                        model.addAttribute("menus", t.getT2());
                    }).thenReturn("hr/salary/list");
                });
    }

    @GetMapping("/new")
    public Mono<String> createForm(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        model.addAttribute("currentUser", principal);
        model.addAttribute("salary", new Salary());
        return Mono.zip(
                employeeService.findAll().collectList(),
                menuService.findByUserId(principal.getUserId()).collectList()
        ).doOnNext(t -> {
            model.addAttribute("employees", t.getT1());
            model.addAttribute("menus", t.getT2());
        }).thenReturn("hr/salary/form");
    }

    @GetMapping("/{id}/edit")
    public Mono<String> editForm(@PathVariable Long id,
                                  @AuthenticationPrincipal CustomUserDetails principal,
                                  Model model) {
        model.addAttribute("currentUser", principal);
        return Mono.zip(
                salaryService.findById(id),
                employeeService.findAll().collectList(),
                menuService.findByUserId(principal.getUserId()).collectList()
        ).doOnNext(t -> {
            model.addAttribute("salary", t.getT1());
            model.addAttribute("employees", t.getT2());
            model.addAttribute("menus", t.getT3());
        }).thenReturn("hr/salary/form");
    }

    @PostMapping
    public Mono<String> create(@ModelAttribute Salary salary) {
        return salaryService.create(salary)
                .thenReturn("redirect:/hr/salary");
    }

    @PostMapping("/{id}")
    public Mono<String> update(@PathVariable Long id, @ModelAttribute Salary salary) {
        return salaryService.update(id, salary)
                .thenReturn("redirect:/hr/salary");
    }

    @PostMapping("/{id}/delete")
    public Mono<String> delete(@PathVariable Long id) {
        return salaryService.delete(id)
                .thenReturn("redirect:/hr/salary");
    }
}
