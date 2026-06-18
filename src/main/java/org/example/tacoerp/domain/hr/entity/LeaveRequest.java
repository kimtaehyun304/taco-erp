package org.example.tacoerp.domain.hr.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Table("leave_requests")
public class LeaveRequest {
    @Id
    private Long id;
    private Long employeeId;
    private String leaveType; // ANNUAL, SICK, SPECIAL, UNPAID
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal days;
    private String reason;
    private String status; // PENDING, APPROVED, REJECTED, CANCELLED
    private Long approvedBy;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
