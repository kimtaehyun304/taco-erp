package org.example.tacoerp.domain.approval.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Table("approval_lines")
public class ApprovalLine {
    @Id
    private Long id;
    private Long documentId;
    private Long approverId;
    private int stepOrder;
    private String status; // WAITING, APPROVED, REJECTED
    private String comment;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
}
