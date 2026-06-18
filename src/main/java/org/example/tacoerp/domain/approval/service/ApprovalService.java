package org.example.tacoerp.domain.approval.service;

import lombok.RequiredArgsConstructor;
import org.example.tacoerp.domain.approval.entity.ApprovalDocument;
import org.example.tacoerp.domain.approval.entity.ApprovalLine;
import org.example.tacoerp.domain.approval.entity.ApprovalNotification;
import org.example.tacoerp.domain.approval.repository.ApprovalDocumentRepository;
import org.example.tacoerp.domain.approval.repository.ApprovalLineRepository;
import org.example.tacoerp.domain.approval.repository.ApprovalNotificationRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApprovalService {

    private final ApprovalDocumentRepository documentRepository;
    private final ApprovalLineRepository lineRepository;
    private final ApprovalNotificationRepository notificationRepository;

    // ── 문서 조회 ────────────────────────────────────────
    public Mono<ApprovalDocument> findById(Long id) {
        return documentRepository.findById(id);
    }

    /** 내가 기안한 문서 */
    public Flux<ApprovalDocument> findMyDrafts(Long userId) {
        return documentRepository.findByDrafterIdOrderByCreatedAtDesc(userId);
    }

    /** 내가 결재해야 할 문서 (WAITING 상태 결재선이 나로 된 것) */
    public Flux<ApprovalDocument> findInbox(Long userId) {
        return lineRepository.findPendingByApproverId(userId)
                .flatMap(line -> documentRepository.findById(line.getDocumentId()));
    }

    /** 결재선 조회 */
    public Flux<ApprovalLine> findLines(Long documentId) {
        return lineRepository.findByDocumentIdOrderByStepOrder(documentId);
    }

    /** 알림 조회 */
    public Flux<ApprovalNotification> findNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Mono<Long> countUnread(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    // ── 기안 ─────────────────────────────────────────────
    public Mono<ApprovalDocument> draft(ApprovalDocument doc, List<Long> approverIds) {
        doc.setStatus("IN_PROGRESS");
        doc.setCreatedAt(LocalDateTime.now());
        doc.setUpdatedAt(LocalDateTime.now());

        return documentRepository.save(doc)
                .flatMap(saved -> {
                    // 결재선 생성
                    return Flux.fromIterable(approverIds)
                            .index()
                            .flatMap(tuple -> {
                                int step = tuple.getT1().intValue() + 1;
                                Long approverId = tuple.getT2();
                                ApprovalLine line = ApprovalLine.builder()
                                        .documentId(saved.getId())
                                        .approverId(approverId)
                                        .stepOrder(step)
                                        .status(step == 1 ? "WAITING" : "WAITING")
                                        .createdAt(LocalDateTime.now())
                                        .build();
                                return lineRepository.save(line)
                                        .flatMap(l -> notify(approverId, saved.getId(),
                                                "'" + saved.getTitle() + "' 문서의 결재 요청이 도착했습니다."));
                            })
                            .then(Mono.just(saved));
                });
    }

    // ── 결재 승인 ─────────────────────────────────────────
    public Mono<ApprovalLine> approve(Long lineId, String comment, Long approverId) {
        return lineRepository.findById(lineId)
                .flatMap(line -> {
                    line.setStatus("APPROVED");
                    line.setComment(comment);
                    line.setApprovedAt(LocalDateTime.now());
                    return lineRepository.save(line)
                            .flatMap(saved -> {
                                // 다음 결재자 확인
                                return lineRepository.findByDocumentIdAndStepOrder(
                                        saved.getDocumentId(), saved.getStepOrder() + 1)
                                        .flatMap(nextLine -> notify(nextLine.getApproverId(),
                                                saved.getDocumentId(), "결재 차례가 되었습니다."))
                                        .switchIfEmpty(
                                                // 마지막 결재자 → 문서 완결
                                                documentRepository.findById(saved.getDocumentId())
                                                        .flatMap(doc -> {
                                                            doc.setStatus("APPROVED");
                                                            doc.setUpdatedAt(LocalDateTime.now());
                                                            return documentRepository.save(doc)
                                                                    .flatMap(d -> notify(d.getDrafterId(),
                                                                            d.getId(), "'" + d.getTitle() + "' 문서가 최종 승인되었습니다."));
                                                        })
                                        )
                                        .thenReturn(saved);
                            });
                });
    }

    // ── 결재 반려 ─────────────────────────────────────────
    public Mono<ApprovalLine> reject(Long lineId, String comment) {
        return lineRepository.findById(lineId)
                .flatMap(line -> {
                    line.setStatus("REJECTED");
                    line.setComment(comment);
                    line.setApprovedAt(LocalDateTime.now());
                    return lineRepository.save(line)
                            .flatMap(saved ->
                                    documentRepository.findById(saved.getDocumentId())
                                            .flatMap(doc -> {
                                                doc.setStatus("REJECTED");
                                                doc.setUpdatedAt(LocalDateTime.now());
                                                return documentRepository.save(doc)
                                                        .flatMap(d -> notify(d.getDrafterId(), d.getId(),
                                                                "'" + d.getTitle() + "' 문서가 반려되었습니다."));
                                            })
                                            .thenReturn(saved)
                            );
                });
    }

    // ── 기안 취소 ─────────────────────────────────────────
    public Mono<Void> cancel(Long documentId) {
        return documentRepository.findById(documentId)
                .flatMap(doc -> {
                    doc.setStatus("CANCELLED");
                    doc.setUpdatedAt(LocalDateTime.now());
                    return documentRepository.save(doc);
                }).then();
    }

    // ── 알림 읽음 처리 ────────────────────────────────────
    public Mono<Void> markAllRead(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalse(userId)
                .flatMap(n -> {
                    n.setRead(true);
                    return notificationRepository.save(n);
                }).then();
    }

    // ── 내부 알림 생성 ────────────────────────────────────
    private Mono<ApprovalNotification> notify(Long userId, Long documentId, String message) {
        return notificationRepository.save(
                ApprovalNotification.builder()
                        .userId(userId)
                        .documentId(documentId)
                        .message(message)
                        .isRead(false)
                        .createdAt(LocalDateTime.now())
                        .build()
        );
    }
}
