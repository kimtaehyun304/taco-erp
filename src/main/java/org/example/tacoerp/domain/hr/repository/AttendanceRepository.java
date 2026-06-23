package org.example.tacoerp.domain.hr.repository;

import org.example.tacoerp.domain.hr.entity.Attendance;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface AttendanceRepository extends ReactiveCrudRepository<Attendance, Long> {
    Mono<Attendance> findByEmployeeIdAndWorkDate(Long employeeId, LocalDate workDate);
    Flux<Attendance> findByEmployeeIdAndWorkDateBetweenOrderByWorkDateDesc(Long employeeId, LocalDate start, LocalDate end);
    Flux<Attendance> findByWorkDateOrderByEmployeeId(LocalDate workDate);
    Flux<Attendance> findByWorkDateBetweenOrderByWorkDateDescEmployeeIdAsc(LocalDate start, LocalDate end);
    Flux<Attendance> findByStatusAndWorkDateBetweenOrderByWorkDateDesc(String status, LocalDate start, LocalDate end);
}
