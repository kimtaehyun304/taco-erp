package org.example.tacoerp.domain.user.repository;

import org.example.tacoerp.domain.user.entity.AuditLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface AuditLogRepository extends ReactiveCrudRepository<AuditLog, Long> {
    Flux<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Flux<AuditLog> findByUsernameContainingOrderByCreatedAtDesc(String username);
}
