package org.example.tacoerp.domain.user.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Table("users")
public class User {
    @Id
    private Long id;
    private String username;
    private String password;
    private String name;
    private String email;
    private String phone;
    private Long departmentId;
    private Long positionId;
    private boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
