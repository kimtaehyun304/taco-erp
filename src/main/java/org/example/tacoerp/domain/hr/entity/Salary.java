package org.example.tacoerp.domain.hr.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Table("salaries")
public class Salary {
    @Id
    private Long id;
    private Long employeeId;
    private BigDecimal baseSalary;
    private BigDecimal bonus;
    private BigDecimal deduction;
    private BigDecimal netSalary;
    private String payMonth; // yyyy-MM
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
}
