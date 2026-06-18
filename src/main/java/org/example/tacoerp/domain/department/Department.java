package org.example.tacoerp.domain.department;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("department")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {
    @Id
    private Long id;
    private String code;
    private String name;
    private Long parentId;
    private int sortOrder;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
