package org.example.tacoerp.domain.hr.repository;

import org.example.tacoerp.domain.hr.entity.Employee;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EmployeeRepository extends ReactiveCrudRepository<Employee, Long> {
    Mono<Employee> findByUserId(Long userId);
    Mono<Employee> findByEmployeeNo(String employeeNo);
    Flux<Employee> findByDepartmentId(Long departmentId);
    Flux<Employee> findByStatus(String status);
    Flux<Employee> findByNameContaining(String name);
}
