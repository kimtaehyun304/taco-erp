package org.example.tacoerp.domain.approval.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Table("approval_notifications")
public class ApprovalNotification {
    @Id
    private Long id;
    private Long userId;
    private Long documentId;
    private String message;
    private boolean isRead;
    private LocalDateTime createdAt;
}
