package org.example.tacoerp.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.tacoerp.domain.user.entity.User;
import org.example.tacoerp.domain.user.entity.UserRole;
import org.example.tacoerp.domain.user.repository.UserRepository;
import org.example.tacoerp.domain.user.repository.UserRoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    public Flux<User> findAll() {
        return userRepository.findAll();
    }

    public Mono<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Mono<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Mono<User> create(User user, List<Long> roleIds) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user)
                .flatMap(saved ->
                    Flux.fromIterable(roleIds)
                        .flatMap(roleId -> userRoleRepository.save(
                            UserRole.builder().userId(saved.getId()).roleId(roleId).build()
                        ))
                        .then(Mono.just(saved))
                );
    }

    public Mono<User> update(Long id, User updated) {
        return userRepository.findById(id)
                .flatMap(user -> {
                    user.setName(updated.getName());
                    user.setEmail(updated.getEmail());
                    user.setPhone(updated.getPhone());
                    user.setDepartmentId(updated.getDepartmentId());
                    user.setPositionId(updated.getPositionId());
                    user.setUpdatedAt(LocalDateTime.now());
                    return userRepository.save(user);
                });
    }

    public Mono<Void> delete(Long id) {
        return userRoleRepository.deleteByUserId(id)
                .then(userRepository.deleteById(id));
    }

    public Flux<String> getRoleNames(Long userId) {
        return userRoleRepository.findRoleNamesByUserId(userId);
    }
}
