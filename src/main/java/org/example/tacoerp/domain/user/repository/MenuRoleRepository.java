package org.example.tacoerp.domain.user.repository;

import org.example.tacoerp.domain.user.entity.MenuRole;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MenuRoleRepository extends ReactiveCrudRepository<MenuRole, Long> {
    Flux<MenuRole> findByMenuId(Long menuId);
    Flux<MenuRole> findByRoleId(Long roleId);

    @Modifying
    @Query("DELETE FROM menu_roles WHERE menu_id = :menuId")
    Mono<Void> deleteByMenuId(Long menuId);

    @Modifying
    @Query("DELETE FROM menu_roles WHERE role_id = :roleId")
    Mono<Void> deleteByRoleId(Long roleId);
}
