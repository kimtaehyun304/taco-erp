package org.example.tacoerp.domain.hr.repository;

import org.example.tacoerp.domain.hr.entity.OvertimeRequest;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface OvertimeRequestRepository extends ReactiveCrudRepository<OvertimeRequest, Long> {
    Flux<OvertimeRequest> findByEmployeeIdOrderByWorkDateDesc(Long employeeId);
    Flux<OvertimeRequest> findByStatusOrderByCreatedAtDesc(String status);
    Flux<OvertimeRequest> findAllByOrderByCreatedAtDesc();
}
