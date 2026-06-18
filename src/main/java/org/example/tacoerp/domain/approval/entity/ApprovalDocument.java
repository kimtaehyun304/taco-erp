package org.example.tacoerp.domain.approval.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Table("approval_documents")
public class ApprovalDocument {
    @Id
    private Long id;
    private String title;
    private String content;
    private String documentType;
    private Long drafterId;
    private String status; // DRAFT, IN_PROGRESS, APPROVED, REJECTED, CANCELLED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
