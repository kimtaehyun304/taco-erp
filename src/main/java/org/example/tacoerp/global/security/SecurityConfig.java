package org.example.tacoerp.global.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.logout.RedirectServerLogoutSuccessHandler;

import java.net.URI;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final AuditingAuthenticationSuccessHandler auditingAuthenticationSuccessHandler;
    private final AuditingAuthenticationFailureHandler auditingAuthenticationFailureHandler;
    private final AuditingAccessDeniedHandler auditingAccessDeniedHandler;

    @Bean
    ApplicationRunner test(PasswordEncoder encoder) {

        String encoded = passwordEncoder().encode("admin");

        log.info("encoded={}", encoded);

        log.info("encoder={}", encoder.getClass().getName());
        return args -> {
            log.info("match={}",
                    encoder.matches(
                            "admin",
                            "$2a$10$pxcQ6zVCIGx8M/akyvypr.bPlYR4zdcXNaZBt90j39Ewb6nRQ3IJa"
                    ));
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ReactiveAuthenticationManager authenticationManager() {
        UserDetailsRepositoryReactiveAuthenticationManager manager =
                new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
        manager.setPasswordEncoder(passwordEncoder());
        return manager;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        RedirectServerLogoutSuccessHandler logoutSuccessHandler = new RedirectServerLogoutSuccessHandler();
        logoutSuccessHandler.setLogoutSuccessUrl(URI.create("/login?logout"));

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/login", "/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                        .pathMatchers("/h2-console/**").permitAll()
                        .pathMatchers("/admin/**").hasRole("ADMIN")
                        .pathMatchers("/admin/positions/**").hasRole("ADMIN")
                        .pathMatchers("/admin/menus/**").hasRole("ADMIN")
                        .pathMatchers("/admin/audit-logs/**").hasRole("ADMIN")
                        // 휴가 신청/조회/취소는 일반 사용자도 가능
                        .pathMatchers(HttpMethod.GET, "/hr/leave", "/hr/leave/new").authenticated()
                        .pathMatchers(HttpMethod.POST, "/hr/leave").authenticated()
                        .pathMatchers(HttpMethod.POST, "/hr/leave/*/delete").authenticated()
                        // 출근/퇴근/지각 사유 등록은 일반 직원도 가능
                        .pathMatchers(HttpMethod.POST, "/hr/attendance/check-in", "/hr/attendance/check-out").authenticated()
                        .pathMatchers(HttpMethod.POST, "/hr/attendance/*/late-reason").authenticated()
                        // 초과근무 신청/취소는 일반 직원도 가능
                        .pathMatchers(HttpMethod.GET, "/hr/overtime", "/hr/overtime/new").authenticated()
                        .pathMatchers(HttpMethod.POST, "/hr/overtime").authenticated()
                        .pathMatchers(HttpMethod.POST, "/hr/overtime/*/delete").authenticated()
                        // 휴가 승인/반려는 관리자(HR)만 가능
                        .pathMatchers(HttpMethod.POST, "/hr/leave/*/approve", "/hr/leave/*/reject").hasAnyRole("ADMIN", "HR")
                        // 나머지 HR 변경(직원/근태/급여 관리 등)은 관리자(HR) 전용
                        .pathMatchers(HttpMethod.POST, "/hr/**").hasAnyRole("ADMIN", "HR")
                        .pathMatchers(HttpMethod.PUT, "/hr/**").hasAnyRole("ADMIN", "HR")
                        .pathMatchers(HttpMethod.DELETE, "/hr/**").hasAnyRole("ADMIN", "HR")
                        // 공지사항 작성/수정/삭제는 팀장 이상(MANAGER/HR/ADMIN)만 가능
                        .pathMatchers(HttpMethod.GET, "/board/notice/new", "/board/notice/*/edit").hasAnyRole("ADMIN", "HR", "MANAGER")
                        .pathMatchers(HttpMethod.POST, "/board/notice").hasAnyRole("ADMIN", "HR", "MANAGER")
                        .pathMatchers(HttpMethod.POST, "/board/notice/*").hasAnyRole("ADMIN", "HR", "MANAGER")
                        .pathMatchers(HttpMethod.POST, "/board/notice/*/delete").hasAnyRole("ADMIN", "HR", "MANAGER")
                        .anyExchange().authenticated()
                )
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .accessDeniedHandler(auditingAccessDeniedHandler)
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .authenticationSuccessHandler(auditingAuthenticationSuccessHandler)
                        .authenticationFailureHandler(auditingAuthenticationFailureHandler)
                        .authenticationManager(authenticationManager())
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(logoutSuccessHandler)
                )
                .build();
    }
}
