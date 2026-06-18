package org.example.tacoerp.domain.hr.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Table("hr_transfers")
public class HrTransfer {
    @Id
    private Long id;
    private Long employeeId;
    private String transferType; // DEPARTMENT, POSITION, HIRE, RESIGN
    private Long fromDepartmentId;
    private Long toDepartmentId;
    private Long fromPositionId;
    private Long toPositionId;
    private LocalDate transferDate;
    private String reason;
    private Long createdBy;
    private LocalDateTime createdAt;
}
