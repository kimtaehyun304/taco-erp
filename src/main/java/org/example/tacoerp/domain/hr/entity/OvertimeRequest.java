package org.example.tacoerp.domain.hr.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Table("overtime_requests")
public class OvertimeRequest {
    @Id
    private Long id;
    private Long employeeId;
    private LocalDate workDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal hours;          // 초과근무 시간 (소수점 0.5 단위)
    private String overtimeType;       // WEEKDAY, WEEKEND, HOLIDAY
    private String reason;
    private String status;             // PENDING, APPROVED, REJECTED, CANCELLED
    private Long approvedBy;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
