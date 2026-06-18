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
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.logout.RedirectServerLogoutSuccessHandler;

import java.net.URI;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

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
                        .pathMatchers(HttpMethod.POST, "/hr/**").hasAnyRole("ADMIN", "HR")
                        .pathMatchers(HttpMethod.PUT, "/hr/**").hasAnyRole("ADMIN", "HR")
                        .pathMatchers(HttpMethod.DELETE, "/hr/**").hasAnyRole("ADMIN", "HR")
                        .anyExchange().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .authenticationSuccessHandler(new RedirectServerAuthenticationSuccessHandler("/dashboard"))
                        .authenticationManager(authenticationManager())
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(logoutSuccessHandler)
                )
                .build();
    }
}
