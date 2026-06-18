package org.example.tacoerp.domain.user.repository;

import org.example.tacoerp.domain.user.entity.UserRole;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRoleRepository extends ReactiveCrudRepository<UserRole, Long> {
    Flux<UserRole> findByUserId(Long userId);
    Mono<Void> deleteByUserId(Long userId);

    @Query("SELECT r.name FROM roles r JOIN user_roles ur ON r.id = ur.role_id WHERE ur.user_id = :userId")
    Flux<String> findRoleNamesByUserId(Long userId);
}
