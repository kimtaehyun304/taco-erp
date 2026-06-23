package org.example.tacoerp.domain.hr.service;

import lombok.RequiredArgsConstructor;
import org.example.tacoerp.domain.hr.entity.Attendance;
import org.example.tacoerp.domain.hr.repository.AttendanceRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;

    public Flux<Attendance> findByEmployeeAndMonth(Long employeeId, LocalDate from, LocalDate to) {
        return attendanceRepository.findByEmployeeIdAndWorkDateBetweenOrderByWorkDateDesc(employeeId, from, to);
    }

    public Flux<Attendance> findByDate(LocalDate date) {
        return attendanceRepository.findByWorkDateOrderByEmployeeId(date);
    }

    public Mono<Attendance> findById(Long id) {
        return attendanceRepository.findById(id);
    }

    /** 팀장 이상: 전체 직원의 해당 월 근태 조회 */
    public Flux<Attendance> findAllByMonth(LocalDate from, LocalDate to) {
        return attendanceRepository.findByWorkDateBetweenOrderByWorkDateDescEmployeeIdAsc(from, to);
    }

    /** 팀장 이상: 전체 직원의 해당 월 지각 기록만 조회 */
    public Flux<Attendance> findLateByMonth(LocalDate from, LocalDate to) {
        return attendanceRepository.findByStatusAndWorkDateBetweenOrderByWorkDateDesc("LATE", from, to);
    }

    public Mono<Attendance> checkIn(Long employeeId) {
        LocalDate today = LocalDate.now();
        return attendanceRepository.findByEmployeeIdAndWorkDate(employeeId, today)
                .switchIfEmpty(Mono.defer(() -> {
                    LocalDateTime now = LocalDateTime.now();
                    // 09:00 이후 출근 시 지각 처리
                    String status = (now.getHour() > 9 || (now.getHour() == 9 && now.getMinute() > 0))
                            ? "LATE" : "NORMAL";
                    Attendance att = Attendance.builder()
                            .employeeId(employeeId)
                            .workDate(today)
                            .checkIn(now)
                            .status(status)
                            .createdAt(now)
                            .build();
                    return attendanceRepository.save(att);
                }));
    }

    public Mono<Attendance> checkOut(Long employeeId) {
        return attendanceRepository.findByEmployeeIdAndWorkDate(employeeId, LocalDate.now())
                .flatMap(att -> {
                    att.setCheckOut(LocalDateTime.now());
                    return attendanceRepository.save(att);
                });
    }

    /** 지각 사유 등록/수정 */
    public Mono<Attendance> saveLateReason(Long id, String reason) {
        return attendanceRepository.findById(id)
                .flatMap(att -> {
                    att.setLateReason(reason);
                    return attendanceRepository.save(att);
                });
    }

    public Mono<Attendance> save(Attendance attendance) {
        if (attendance.getCreatedAt() == null) attendance.setCreatedAt(LocalDateTime.now());
        return attendanceRepository.save(attendance);
    }

    public Mono<Void> delete(Long id) {
        return attendanceRepository.deleteById(id);
    }
}
