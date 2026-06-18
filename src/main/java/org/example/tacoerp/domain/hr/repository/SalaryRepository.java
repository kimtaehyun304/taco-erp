package org.example.tacoerp.domain.hr.repository;

import org.example.tacoerp.domain.hr.entity.Salary;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SalaryRepository extends ReactiveCrudRepository<Salary, Long> {
    Flux<Salary> findByEmployeeIdOrderByPayMonthDesc(Long employeeId);
    Mono<Salary> findByEmployeeIdAndPayMonth(Long employeeId, String payMonth);
}
