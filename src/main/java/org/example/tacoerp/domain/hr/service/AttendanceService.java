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

    public Mono<Attendance> checkIn(Long employeeId) {
        LocalDate today = LocalDate.now();
        return attendanceRepository.findByEmployeeIdAndWorkDate(employeeId, today)
                .switchIfEmpty(Mono.defer(() -> {
                    Attendance att = Attendance.builder()
                            .employeeId(employeeId)
                            .workDate(today)
                            .checkIn(LocalDateTime.now())
                            .status("NORMAL")
                            .createdAt(LocalDateTime.now())
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

    public Mono<Attendance> save(Attendance attendance) {
        if (attendance.getCreatedAt() == null) attendance.setCreatedAt(LocalDateTime.now());
        return attendanceRepository.save(attendance);
    }

    public Mono<Void> delete(Long id) {
        return attendanceRepository.deleteById(id);
    }
}
