package org.example.tacoerp.domain.approval.repository;

import org.example.tacoerp.domain.approval.entity.ApprovalDocument;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface ApprovalDocumentRepository extends ReactiveCrudRepository<ApprovalDocument, Long> {
    Flux<ApprovalDocument> findByDrafterIdOrderByCreatedAtDesc(Long drafterId);
    Flux<ApprovalDocument> findByStatusOrderByCreatedAtDesc(String status);
}
