package org.example.tacoerp.domain.user.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Table("audit_logs")
public class AuditLog {
    @Id
    private Long id;
    private Long userId;
    private String username;
    private String action;
    private String targetType;
    private String targetId;
    private String detail;
    private String ipAddress;
    private LocalDateTime createdAt;
}
