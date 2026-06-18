package org.example.tacoerp.domain.hr.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Table("employees")
public class Employee {
    @Id
    private Long id;
    private Long userId;
    private String employeeNo;
    private String name;
    private LocalDate birthDate;
    private String gender;
    private String address;
    private Long departmentId;
    private Long positionId;
    private LocalDate hireDate;
    private LocalDate resignDate;
    private String status; // ACTIVE, RESIGNED, LEAVE
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
