package org.example.tacoerp.domain.hr.controller;

import lombok.RequiredArgsConstructor;
import org.example.tacoerp.domain.hr.entity.Attendance;
import org.example.tacoerp.domain.hr.service.AttendanceService;
import org.example.tacoerp.domain.hr.service.EmployeeService;
import org.example.tacoerp.domain.user.service.MenuService;
import org.example.tacoerp.domain.user.service.PositionService;
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
    private final PositionService positionService;

    // 팀장 이상으로 판단하는 직위 level 기준 (level이 낮을수록 높은 직급)
    private static final int MANAGER_LEVEL = 3; // 1=사장, 2=부장, 3=팀장 이상

    private Mono<Boolean> isManager(CustomUserDetails principal) {
        Long positionId = principal.getUser().getPositionId();
        if (positionId == null) return Mono.just(false);
        return positionService.findById(positionId)
                .map(pos -> pos.getLevel() <= MANAGER_LEVEL)
                .defaultIfEmpty(false);
    }

    @GetMapping
    public Mono<String> list(@AuthenticationPrincipal CustomUserDetails principal,
                             @RequestParam(required = false) String month,
                             @RequestParam(required = false) Long employeeId,
                             @RequestParam(required = false, defaultValue = "my") String view,
                             Model model) {
        model.addAttribute("currentUser", principal);

        YearMonth ym = (month != null && !month.isBlank())
                ? YearMonth.parse(month)
                : YearMonth.now();

        LocalDate from = ym.atDay(1);
        LocalDate to = ym.atEndOfMonth();
        model.addAttribute("month", ym.toString());
        model.addAttribute("selectedEmployeeId", employeeId);
        model.addAttribute("view", view);

        return isManager(principal).flatMap(isManager -> {
            model.addAttribute("isManager", isManager);

            return employeeService.findAll().collectList().flatMap(employees -> {
                model.addAttribute("employees", employees);

                // 팀장 이상이고 전체현황 탭인 경우
                if (isManager && "all".equals(view)) {
                    return Mono.zip(
                            attendanceService.findAllByMonth(from, to).collectList(),
                            attendanceService.findLateByMonth(from, to).collectList(),
                            menuService.findByUserId(principal.getUserId()).collectList()
                    ).doOnNext(t -> {
                        model.addAttribute("allAttendances", t.getT1());
                        model.addAttribute("lateAttendances", t.getT2());
                        model.addAttribute("menus", t.getT3());
                    }).thenReturn("hr/attendance/list");
                }

                // 내 근태 탭 (기본)
                return employeeService.findByUserId(principal.getUserId())
                        .flatMap(myEmp -> {
                            Long targetEmpId = employeeId != null ? employeeId : myEmp.getId();
                            model.addAttribute("selectedEmployeeId", targetEmpId);
                            return Mono.zip(
                                    attendanceService.findByEmployeeAndMonth(targetEmpId, from, to).collectList(),
                                    menuService.findByUserId(principal.getUserId()).collectList()
                            ).doOnNext(t -> {
                                model.addAttribute("attendances", t.getT1());
                                model.addAttribute("menus", t.getT2());
                            }).thenReturn("hr/attendance/list");
                        })
                        .switchIfEmpty(
                            menuService.findByUserId(principal.getUserId()).collectList()
                                .doOnNext(m -> {
                                    model.addAttribute("menus", m);
                                    model.addAttribute("attendances", java.util.List.of());
                                })
                                .thenReturn("hr/attendance/list")
                        );
            });
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

    /** 지각 사유 등록/수정 */
    @PostMapping("/{id}/late-reason")
    public Mono<String> saveLateReason(@PathVariable Long id,
                                       @RequestParam String lateReason,
                                       @RequestParam(required = false, defaultValue = "") String month) {
        return attendanceService.saveLateReason(id, lateReason)
                .thenReturn("redirect:/hr/attendance?month=" + month);
    }

    @PostMapping("/{id}/delete")
    public Mono<String> delete(@PathVariable Long id) {
        return attendanceService.delete(id)
                .thenReturn("redirect:/hr/attendance");
    }
}
