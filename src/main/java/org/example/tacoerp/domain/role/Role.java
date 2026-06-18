package org.example.tacoerp.domain.role;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("role")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {
    @Id
    private Long id;
    private String code;
    private String name;
    private String description;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
