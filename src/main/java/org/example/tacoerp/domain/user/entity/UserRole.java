package org.example.tacoerp.domain.user.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Table("user_roles")
public class UserRole {
    @Id
    private Long id;
    private Long userId;
    private Long roleId;
}
