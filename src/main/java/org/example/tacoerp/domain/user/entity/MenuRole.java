package org.example.tacoerp.domain.user.entity;

import lombok.*;
import org.springframework.data.relational.core.mapping.Table;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Table("menu_roles")
public class MenuRole {
    private Long menuId;
    private Long roleId;
}
