package org.example.tacoerp.domain.approval.repository;

import org.example.tacoerp.domain.approval.entity.ApprovalNotification;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ApprovalNotificationRepository extends ReactiveCrudRepository<ApprovalNotification, Long> {
    Flux<ApprovalNotification> findByUserIdOrderByCreatedAtDesc(Long userId);
    Flux<ApprovalNotification> findByUserIdAndIsReadFalse(Long userId);
    Mono<Long> countByUserIdAndIsReadFalse(Long userId);
}
