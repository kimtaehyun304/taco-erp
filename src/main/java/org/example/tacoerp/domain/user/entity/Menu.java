package org.example.tacoerp.domain.user.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Table("menus")
public class Menu {
    @Id
    private Long id;
    private String name;
    private String url;
    private String icon;
    private Long parentId;
    private int sortOrder;
    private LocalDateTime createdAt;
}
