package org.example.tacoerp.domain.board.service;

import lombok.RequiredArgsConstructor;
import org.example.tacoerp.domain.board.entity.Board;
import org.example.tacoerp.domain.board.repository.BoardRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BoardService {

    private static final int MAX_PINNED_NOTICE = 5;

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

        if (board.isPinned()) {
            return validatePinLimit(board.getBoardType())
                    .then(boardRepository.save(board));
        }
        return boardRepository.save(board);
    }

    public Mono<Board> update(Long id, Board updated) {
        return boardRepository.findById(id)
                .flatMap(board -> {
                    boolean wasPinned = board.isPinned();
                    boolean willBePinned = updated.isPinned();

                    Mono<Void> pinCheck = (!wasPinned && willBePinned)
                            ? validatePinLimit(board.getBoardType())
                            : Mono.empty();

                    return pinCheck.then(Mono.defer(() -> {
                        board.setTitle(updated.getTitle());
                        board.setContent(updated.getContent());
                        board.setPinned(willBePinned);
                        board.setUpdatedAt(LocalDateTime.now());
                        return boardRepository.save(board);
                    }));
                });
    }

    /** 같은 게시판 타입에서 고정 글이 이미 MAX_PINNED_NOTICE개 이상이면 거부 */
    private Mono<Void> validatePinLimit(String boardType) {
        return boardRepository.countByBoardTypeAndIsPinnedTrue(boardType)
                .flatMap(count -> {
                    if (count >= MAX_PINNED_NOTICE) {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "상단 고정은 최대 " + MAX_PINNED_NOTICE + "개까지만 가능합니다. 기존 고정 글을 먼저 해제해주세요."));
                    }
                    return Mono.empty();
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
