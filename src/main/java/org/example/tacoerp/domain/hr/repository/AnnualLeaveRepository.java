package org.example.tacoerp.domain.hr.repository;

import org.example.tacoerp.domain.hr.entity.AnnualLeave;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface AnnualLeaveRepository extends ReactiveCrudRepository<AnnualLeave, Long> {
    Mono<AnnualLeave> findByEmployeeIdAndYear(Long employeeId, int year);
}
