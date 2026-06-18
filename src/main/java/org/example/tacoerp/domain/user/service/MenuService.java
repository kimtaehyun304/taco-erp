package org.example.tacoerp.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.tacoerp.domain.user.entity.Menu;
import org.example.tacoerp.domain.user.entity.MenuRole;
import org.example.tacoerp.domain.user.repository.MenuRepository;
import org.example.tacoerp.domain.user.repository.MenuRoleRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;
    private final MenuRoleRepository menuRoleRepository;

    public Flux<Menu> findAll() {
        return menuRepository.findAll();
    }

    public Mono<Menu> findById(Long id) {
        return menuRepository.findById(id);
    }

    public Flux<Menu> findByUserId(Long userId) {
        return menuRepository.findMenusByUserId(userId);
    }

    public Flux<MenuRole> findRolesByMenuId(Long menuId) {
        return menuRoleRepository.findByMenuId(menuId);
    }

    public Mono<Menu> create(Menu menu, List<Long> roleIds) {
        menu.setCreatedAt(LocalDateTime.now());
        return menuRepository.save(menu)
                .flatMap(saved ->
                    Flux.fromIterable(roleIds)
                        .flatMap(roleId -> menuRoleRepository.save(
                            MenuRole.builder().menuId(saved.getId()).roleId(roleId).build()
                        ))
                        .then(Mono.just(saved))
                );
    }

    public Mono<Menu> update(Long id, Menu updated, List<Long> roleIds) {
        return menuRepository.findById(id)
                .flatMap(menu -> {
                    menu.setName(updated.getName());
                    menu.setUrl(updated.getUrl());
                    menu.setIcon(updated.getIcon());
                    menu.setParentId(updated.getParentId());
                    menu.setSortOrder(updated.getSortOrder());
                    return menuRepository.save(menu);
                })
                .flatMap(saved ->
                    menuRoleRepository.deleteByMenuId(saved.getId())
                        .thenMany(Flux.fromIterable(roleIds)
                            .flatMap(roleId -> menuRoleRepository.save(
                                MenuRole.builder().menuId(saved.getId()).roleId(roleId).build()
                            )))
                        .then(Mono.just(saved))
                );
    }

    public Mono<Void> delete(Long id) {
        return menuRoleRepository.deleteByMenuId(id)
                .then(menuRepository.deleteById(id));
    }
}
