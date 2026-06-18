package org.example.tacoerp.domain.user.repository;

import org.example.tacoerp.domain.user.entity.Department;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface DepartmentRepository extends ReactiveCrudRepository<Department, Long> {
    Flux<Department> findByParentIdIsNullOrderBySortOrder();
    Flux<Department> findByParentIdOrderBySortOrder(Long parentId);
}
