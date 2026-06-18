package org.example.tacoerp.domain.hr.repository;

import org.example.tacoerp.domain.hr.entity.LeaveRequest;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface LeaveRequestRepository extends ReactiveCrudRepository<LeaveRequest, Long> {
    Flux<LeaveRequest> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);
    Flux<LeaveRequest> findByStatusOrderByCreatedAtDesc(String status);
}
