package org.example.tacoerp.global.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tacoerp.domain.user.repository.UserRepository;
import org.example.tacoerp.domain.user.repository.UserRoleRepository;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements ReactiveUserDetailsService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    @Override
    public Mono<UserDetails> findByUsername(String username) {

        log.info("로그인 시도 username={}", username);

        return userRepository.findByUsername(username)
                .doOnNext(user -> log.info(
                        "조회된 사용자 id={}, username={}, password={}",
                        user.getId(),
                        user.getUsername(),
                        user.getPassword()
                ))
                .switchIfEmpty(Mono.error(
                        new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username)
                ))
                .flatMap(user ->
                        userRoleRepository.findRoleNamesByUserId(user.getId())
                                .collectList()
                                .doOnNext(roles -> log.info("roles={}", roles))
                                .map(roles -> (UserDetails) new CustomUserDetails(user, roles))
                )
                .doOnNext(userDetails -> log.info(
                        "생성된 UserDetails username={}, authorities={}",
                        userDetails.getUsername(),
                        userDetails.getAuthorities()
                ))
                .doOnError(error -> log.error("인증용 사용자 조회 실패", error));
    }
}
