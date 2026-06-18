package org.example.tacoerp.domain.hr.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Table("annual_leaves")
public class AnnualLeave {
    @Id
    private Long id;
    private Long employeeId;
    private int year;
    private BigDecimal totalDays;
    private BigDecimal usedDays;
    private BigDecimal remainingDays;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
