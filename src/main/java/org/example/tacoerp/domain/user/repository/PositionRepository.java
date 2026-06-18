package org.example.tacoerp.domain.user.repository;

import org.example.tacoerp.domain.user.entity.Position;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface PositionRepository extends ReactiveCrudRepository<Position, Long> {
    Flux<Position> findAllByOrderByLevelAsc();
}
