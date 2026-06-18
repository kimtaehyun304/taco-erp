package org.example.tacoerp.domain.hr.controller;

import lombok.RequiredArgsConstructor;
import org.example.tacoerp.domain.hr.entity.Attendance;
import org.example.tacoerp.domain.hr.service.AttendanceService;
import org.example.tacoerp.domain.hr.service.EmployeeService;
import org.example.tacoerp.domain.user.service.MenuService;
import org.example.tacoerp.global.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.YearMonth;

@Controller
@RequestMapping("/hr/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final EmployeeService employeeService;
    private final MenuService menuService;

    @GetMapping
    public Mono<String> list(@AuthenticationPrincipal CustomUserDetails principal,
                              @RequestParam(required = false) String month,
                              @RequestParam(required = false) Long employeeId,
                              Model model) {
        model.addAttribute("currentUser", principal);

        YearMonth ym = (month != null && !month.isBlank())
                ? YearMonth.parse(month)
                : YearMonth.now();

        LocalDate from = ym.atDay(1);
        LocalDate to = ym.atEndOfMonth();
        model.addAttribute("month", ym.toString());
        model.addAttribute("selectedEmployeeId", employeeId);

        Long targetEmpId = employeeId;

        return employeeService.findAll().collectList()
                .flatMap(employees -> {
                    model.addAttribute("employees", employees);
                    Long empId = targetEmpId != null ? targetEmpId
                            : (employees.isEmpty() ? null : employees.get(0).getId());

                    if (empId == null) {
                        model.addAttribute("attendances", java.util.List.of());
                        return menuService.findByUserId(principal.getUserId()).collectList()
                                .doOnNext(m -> model.addAttribute("menus", m))
                                .thenReturn("hr/attendance/list");
                    }

                    final Long finalEmpId = empId;
                    model.addAttribute("selectedEmployeeId", finalEmpId);
                    return Mono.zip(
                            attendanceService.findByEmployeeAndMonth(finalEmpId, from, to).collectList(),
                            menuService.findByUserId(principal.getUserId()).collectList()
                    ).doOnNext(t -> {
                        model.addAttribute("attendances", t.getT1());
                        model.addAttribute("menus", t.getT2());
                    }).thenReturn("hr/attendance/list");
                });
    }

    @PostMapping("/check-in")
    public Mono<String> checkIn(@AuthenticationPrincipal CustomUserDetails principal) {
        return employeeService.findByUserId(principal.getUserId())
                .flatMap(emp -> attendanceService.checkIn(emp.getId()))
                .thenReturn("redirect:/hr/attendance");
    }

    @PostMapping("/check-out")
    public Mono<String> checkOut(@AuthenticationPrincipal CustomUserDetails principal) {
        return employeeService.findByUserId(principal.getUserId())
                .flatMap(emp -> attendanceService.checkOut(emp.getId()))
                .thenReturn("redirect:/hr/attendance");
    }

    @PostMapping("/{id}/delete")
    public Mono<String> delete(@PathVariable Long id) {
        return attendanceService.delete(id)
                .thenReturn("redirect:/hr/attendance");
    }
}
