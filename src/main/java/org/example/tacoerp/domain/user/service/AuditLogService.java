package org.example.tacoerp.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.tacoerp.domain.user.entity.AuditLog;
import org.example.tacoerp.domain.user.repository.AuditLogRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public Flux<AuditLog> findRecent(int size) {
        return auditLogRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, size));
    }

    public Flux<AuditLog> searchByUsername(String username) {
        return auditLogRepository.findByUsernameContainingOrderByCreatedAtDesc(username);
    }

    public Mono<AuditLog> log(Long userId, String username, String action,
                               String targetType, String targetId, String detail, String ip) {
        AuditLog log = AuditLog.builder()
                .userId(userId)
                .username(username)
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .detail(detail)
                .ipAddress(ip)
                .createdAt(LocalDateTime.now())
                .build();
        return auditLogRepository.save(log);
    }
}
