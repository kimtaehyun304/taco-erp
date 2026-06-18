package org.example.tacoerp.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.tacoerp.domain.user.entity.Position;
import org.example.tacoerp.domain.user.repository.PositionRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PositionService {

    private final PositionRepository positionRepository;

    public Flux<Position> findAll() {
        return positionRepository.findAll();
    }

    public Mono<Position> findById(Long id) {
        return positionRepository.findById(id);
    }

    public Mono<Position> create(Position position) {
        position.setCreatedAt(LocalDateTime.now());
        return positionRepository.save(position);
    }

    public Mono<Position> update(Long id, Position updated) {
        return positionRepository.findById(id)
                .flatMap(pos -> {
                    pos.setName(updated.getName());
                    pos.setLevel(updated.getLevel());
                    return positionRepository.save(pos);
                });
    }

    public Mono<Void> delete(Long id) {
        return positionRepository.deleteById(id);
    }
}
