package org.example.tacoerp.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.tacoerp.domain.user.entity.Department;
import org.example.tacoerp.domain.user.repository.DepartmentRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public Flux<Department> findAll() {
        return departmentRepository.findAll();
    }

    public Mono<Department> findById(Long id) {
        return departmentRepository.findById(id);
    }

    public Mono<Department> create(Department dept) {
        dept.setCreatedAt(LocalDateTime.now());
        dept.setUpdatedAt(LocalDateTime.now());
        return departmentRepository.save(dept);
    }

    public Mono<Department> update(Long id, Department updated) {
        return departmentRepository.findById(id)
                .flatMap(dept -> {
                    dept.setName(updated.getName());
                    dept.setParentId(updated.getParentId());
                    dept.setSortOrder(updated.getSortOrder());
                    dept.setUpdatedAt(LocalDateTime.now());
                    return departmentRepository.save(dept);
                });
    }

    public Mono<Void> delete(Long id) {
        return departmentRepository.deleteById(id);
    }
}
