package org.example.tacoerp.domain.board.repository;

import org.example.tacoerp.domain.board.entity.Board;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface BoardRepository extends ReactiveCrudRepository<Board, Long> {
    Flux<Board> findByBoardTypeOrderByIsPinnedDescCreatedAtDesc(String boardType, Pageable pageable);
    Flux<Board> findByBoardTypeAndTitleContainingOrderByCreatedAtDesc(String boardType, String title);
}
