package org.example.tacoerp.domain.board.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Table("boards")
public class Board {
    @Id
    private Long id;
    private String title;
    private String content;
    private String boardType; // NOTICE, ARCHIVE, FAQ
    private Long authorId;
    private int viewCount;
    private boolean isPinned;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
