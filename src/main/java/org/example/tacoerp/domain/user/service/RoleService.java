package org.example.tacoerp.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.tacoerp.domain.user.entity.Role;
import org.example.tacoerp.domain.user.repository.RoleRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    public Flux<Role> findAll() {
        return roleRepository.findAll();
    }

    public Mono<Role> findById(Long id) {
        return roleRepository.findById(id);
    }

    public Mono<Role> create(Role role) {
        role.setCreatedAt(LocalDateTime.now());
        return roleRepository.save(role);
    }

    public Mono<Role> update(Long id, Role updated) {
        return roleRepository.findById(id)
                .flatMap(role -> {
                    role.setName(updated.getName());
                    role.setDescription(updated.getDescription());
                    return roleRepository.save(role);
                });
    }

    public Mono<Void> delete(Long id) {
        return roleRepository.deleteById(id);
    }
}
