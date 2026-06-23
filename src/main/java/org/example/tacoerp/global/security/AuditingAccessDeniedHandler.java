package org.example.tacoerp.global.security;

import lombok.RequiredArgsConstructor;
import org.example.tacoerp.domain.user.service.AuditLogService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 권한이 없는 리소스에 접근을 시도하면(403) 감사 로그에 ACCESS_DENIED 기록을 남긴다.
 */
@Component
@RequiredArgsConstructor
public class AuditingAccessDeniedHandler implements ServerAccessDeniedHandler {

    private final AuditLogService auditLogService;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException denied) {
        String path = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethod() != null
                ? exchange.getRequest().getMethod().name() : "UNKNOWN";
        String ip = exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown";

        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .flatMap(auth -> {
                    String username = auth != null ? auth.getName() : "anonymous";
                    Long userId = (auth != null && auth.getPrincipal() instanceof CustomUserDetails cud)
                            ? cud.getUserId() : null;
                    return auditLogService.log(userId, username, "ACCESS_DENIED", "HTTP", path,
                            method + " " + path + " 접근 거부 (권한 없음)", ip);
                })
                .switchIfEmpty(auditLogService.log(null, "anonymous", "ACCESS_DENIED", "HTTP", path,
                        method + " " + path + " 접근 거부 (권한 없음)", ip))
                .then(Mono.defer(() -> {
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }));
    }
}
