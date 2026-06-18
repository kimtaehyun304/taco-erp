package org.example.tacoerp.domain.hr.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Table("attendances")
public class Attendance {
    @Id
    private Long id;
    private Long employeeId;
    private LocalDate workDate;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private String status; // NORMAL, LATE, EARLY_LEAVE, ABSENT
    private String note;
    private LocalDateTime createdAt;
}
