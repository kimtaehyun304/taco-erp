package org.example.tacoerp.domain.user.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Table("positions")
public class Position {
    @Id
    private Long id;
    private String name;
    private int level;
    private LocalDateTime createdAt;
}
