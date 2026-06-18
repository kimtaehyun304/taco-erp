package org.example.tacoerp.domain.approval.repository;

import org.example.tacoerp.domain.approval.entity.ApprovalLine;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ApprovalLineRepository extends ReactiveCrudRepository<ApprovalLine, Long> {
    Flux<ApprovalLine> findByDocumentIdOrderByStepOrder(Long documentId);
    Flux<ApprovalLine> findByApproverIdAndStatusOrderByCreatedAtDesc(Long approverId, String status);
    Mono<ApprovalLine> findByDocumentIdAndStepOrder(Long documentId, int stepOrder);

    @Query("SELECT al.* FROM approval_lines al " +
           "JOIN approval_documents ad ON al.document_id = ad.id " +
           "WHERE al.approver_id = :approverId AND al.status = 'WAITING' " +
           "ORDER BY al.created_at DESC")
    Flux<ApprovalLine> findPendingByApproverId(Long approverId);
}
