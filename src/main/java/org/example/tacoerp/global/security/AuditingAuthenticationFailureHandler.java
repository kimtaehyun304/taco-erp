package org.example.tacoerp.global.security;

import lombok.RequiredArgsConstructor;
import org.example.tacoerp.domain.user.service.AuditLogService;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationFailureHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * 로그인 실패 시 감사 로그에 LOGIN_FAILURE 기록을 남기고
 * 기존처럼 /login?error로 리다이렉트한다.
 */
@Component
@RequiredArgsConstructor
public class AuditingAuthenticationFailureHandler implements ServerAuthenticationFailureHandler {

    private final AuditLogService auditLogService;

    private final RedirectServerAuthenticationFailureHandler delegate =
            new RedirectServerAuthenticationFailureHandler("/login?error");

    @Override
    public Mono<Void> onAuthenticationFailure(WebFilterExchange webFilterExchange, AuthenticationException exception) {
        String ip = resolveIp(webFilterExchange);

        return webFilterExchange.getExchange().getFormData()
                .map(formData -> formData.getFirst("username"))
                .defaultIfEmpty("unknown")
                .flatMap(username -> auditLogService.log(null, username, "LOGIN_FAILURE", "AUTH", username,
                        "로그인 실패: " + username + " (" + exception.getMessage() + ")", ip))
                .then(delegate.onAuthenticationFailure(webFilterExchange, exception));
    }

    private String resolveIp(WebFilterExchange exchange) {
        var remote = exchange.getExchange().getRequest().getRemoteAddress();
        return remote != null ? remote.getAddress().getHostAddress() : "unknown";
    }
}
