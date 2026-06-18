package org.example.tacoerp.domain.user.repository;

import org.example.tacoerp.domain.user.entity.Role;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface RoleRepository extends ReactiveCrudRepository<Role, Long> {
    Mono<Role> findByName(String name);
}
