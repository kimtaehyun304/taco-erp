package org.example.tacoerp.domain.user.repository;

import org.example.tacoerp.domain.user.entity.Menu;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface MenuRepository extends ReactiveCrudRepository<Menu, Long> {
    Flux<Menu> findByParentIdIsNullOrderBySortOrder();
    Flux<Menu> findByParentIdOrderBySortOrder(Long parentId);

    @Query("SELECT DISTINCT m.* FROM menus m " +
           "JOIN menu_roles mr ON m.id = mr.menu_id " +
           "JOIN user_roles ur ON mr.role_id = ur.role_id " +
           "WHERE ur.user_id = :userId " +
           "ORDER BY m.sort_order")
    Flux<Menu> findMenusByUserId(Long userId);
}
