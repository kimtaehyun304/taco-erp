package org.example.tacoerp.domain.board.service;

import lombok.RequiredArgsConstructor;
import org.example.tacoerp.domain.board.entity.Board;
import org.example.tacoerp.domain.board.repository.BoardRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;

    public Flux<Board> findByType(String boardType) {
        return boardRepository.findByBoardTypeOrderByIsPinnedDescCreatedAtDesc(boardType, PageRequest.of(0, 100));
    }

    public Flux<Board> search(String boardType, String title) {
        return boardRepository.findByBoardTypeAndTitleContainingOrderByCreatedAtDesc(boardType, title);
    }

    public Mono<Board> findById(Long id) {
        return boardRepository.findById(id);
    }

    public Mono<Board> create(Board board, Long authorId) {
        board.setAuthorId(authorId);
        board.setViewCount(0);
        board.setCreatedAt(LocalDateTime.now());
        board.setUpdatedAt(LocalDateTime.now());
        return boardRepository.save(board);
    }

    public Mono<Board> update(Long id, Board updated) {
        return boardRepository.findById(id)
                .flatMap(board -> {
                    board.setTitle(updated.getTitle());
                    board.setContent(updated.getContent());
                    board.setPinned(updated.isPinned());
                    board.setUpdatedAt(LocalDateTime.now());
                    return boardRepository.save(board);
                });
    }

    public Mono<Void> delete(Long id) {
        return boardRepository.deleteById(id);
    }

    public Mono<Board> incrementView(Long id) {
        return boardRepository.findById(id)
                .flatMap(board -> {
                    board.setViewCount(board.getViewCount() + 1);
                    return boardRepository.save(board);
                });
    }
}
