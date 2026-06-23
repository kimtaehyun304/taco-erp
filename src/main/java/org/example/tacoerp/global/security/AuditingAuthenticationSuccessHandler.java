package org.example.tacoerp.global.security;

import lombok.RequiredArgsConstructor;
import org.example.tacoerp.domain.user.service.AuditLogService;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * 로그인 성공 시 감사 로그(AuditLog)에 LOGIN 기록을 남기고
 * 기존처럼 /dashboard로 리다이렉트한다.
 */
@Component
@RequiredArgsConstructor
public class AuditingAuthenticationSuccessHandler implements ServerAuthenticationSuccessHandler {

    private final AuditLogService auditLogService;

    private final RedirectServerAuthenticationSuccessHandler delegate =
            new RedirectServerAuthenticationSuccessHandler("/dashboard");

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        String username = authentication.getName();
        Long userId = (authentication.getPrincipal() instanceof CustomUserDetails cud) ? cud.getUserId() : null;
        String ip = resolveIp(webFilterExchange);

        return auditLogService.log(userId, username, "LOGIN", "AUTH", username,
                        "로그인 성공: " + username, ip)
                .then(delegate.onAuthenticationSuccess(webFilterExchange, authentication));
    }

    private String resolveIp(WebFilterExchange exchange) {
        var remote = exchange.getExchange().getRequest().getRemoteAddress();
        return remote != null ? remote.getAddress().getHostAddress() : "unknown";
    }
}
