package org.example.tacoerp.domain.position;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("position")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Position {
    @Id
    private Long id;
    private String code;
    private String name;
    private int level;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
