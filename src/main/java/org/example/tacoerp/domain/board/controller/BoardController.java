package org.example.tacoerp.domain.board.controller;

import lombok.RequiredArgsConstructor;
import org.example.tacoerp.domain.board.entity.Board;
import org.example.tacoerp.domain.board.service.BoardService;
import org.example.tacoerp.domain.user.entity.User;
import org.example.tacoerp.domain.user.service.AuditLogService;
import org.example.tacoerp.domain.user.service.MenuService;
import org.example.tacoerp.domain.user.service.UserService;
import org.example.tacoerp.global.security.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/board")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;
    private final MenuService menuService;
    private final UserService userService;
    private final AuditLogService auditLogService;

    private String ipOf(ServerWebExchange exchange) {
        return exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }

    /** 팀장 이상(MANAGER/ADMIN/HR) 여부 - 공지사항 작성 권한 판단용 */
    private boolean isManagerOrAbove(CustomUserDetails principal) {
        return principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                        || a.getAuthority().equals("ROLE_HR")
                        || a.getAuthority().equals("ROLE_MANAGER"));
    }

    // ── 목록 ──────────────────────────────────────────
    @GetMapping("/notice")
    public Mono<String> notice(@AuthenticationPrincipal CustomUserDetails principal,
                                @RequestParam(required = false) String keyword,
                                Model model) {
        return list("NOTICE", "notice", keyword, principal, model);
    }

    @GetMapping("/archive")
    public Mono<String> archive(@AuthenticationPrincipal CustomUserDetails principal,
                                 @RequestParam(required = false) String keyword,
                                 Model model) {
        return list("ARCHIVE", "archive", keyword, principal, model);
    }

    @GetMapping("/faq")
    public Mono<String> faq(@AuthenticationPrincipal CustomUserDetails principal,
                             @RequestParam(required = false) String keyword,
                             Model model) {
        return list("FAQ", "faq", keyword, principal, model);
    }

    private Mono<String> list(String type, String view, String keyword,
                               CustomUserDetails principal, Model model) {
        model.addAttribute("currentUser", principal);
        model.addAttribute("boardType", type);
        model.addAttribute("keyword", keyword);
        model.addAttribute("canWriteNotice", isManagerOrAbove(principal));

        var boardsMono = (keyword != null && !keyword.isBlank())
                ? boardService.search(type, keyword).collectList()
                : boardService.findByType(type).collectList();

        return boardsMono.flatMap(boards ->
                Mono.zip(
                        buildAuthorNameMap(boards),
                        menuService.findByUserId(principal.getUserId()).collectList()
                ).doOnNext(tuple -> {
                    model.addAttribute("boards", boards);
                    model.addAttribute("authorNames", tuple.getT1());
                    model.addAttribute("menus", tuple.getT2());
                })
        ).thenReturn("board/" + view + "/list");
    }

    // ── 상세 ──────────────────────────────────────────
    @GetMapping("/{type}/{id}")
    public Mono<String> detail(@PathVariable String type,
                                @PathVariable Long id,
                                @AuthenticationPrincipal CustomUserDetails principal,
                                Model model) {
        model.addAttribute("currentUser", principal);
        model.addAttribute("boardType", type.toUpperCase());
        model.addAttribute("canWriteNotice", isManagerOrAbove(principal));

        return boardService.incrementView(id)
                .flatMap(board -> Mono.zip(
                        userService.findById(board.getAuthorId()).map(User::getName).defaultIfEmpty("알 수 없음"),
                        menuService.findByUserId(principal.getUserId()).collectList()
                ).doOnNext(tuple -> {
                    model.addAttribute("board", board);
                    model.addAttribute("authorName", tuple.getT1());
                    model.addAttribute("menus", tuple.getT2());
                }))
                .thenReturn("board/" + type + "/detail");
    }

    // ── 등록 폼 ──────────────────────────────────────
    @GetMapping("/{type}/new")
    public Mono<String> createForm(@PathVariable String type,
                                    @AuthenticationPrincipal CustomUserDetails principal,
                                    Model model) {
        if ("notice".equalsIgnoreCase(type) && !isManagerOrAbove(principal)) {
            return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "공지사항은 팀장 이상만 작성할 수 있습니다."));
        }

        model.addAttribute("currentUser", principal);
        model.addAttribute("board", new Board());
        model.addAttribute("boardType", type.toUpperCase());

        return menuService.findByUserId(principal.getUserId()).collectList()
                .doOnNext(menus -> model.addAttribute("menus", menus))
                .thenReturn("board/" + type + "/form");
    }

    // ── 수정 폼 ──────────────────────────────────────
    @GetMapping("/{type}/{id}/edit")
    public Mono<String> editForm(@PathVariable String type,
                                  @PathVariable Long id,
                                  @AuthenticationPrincipal CustomUserDetails principal,
                                  Model model) {
        if ("notice".equalsIgnoreCase(type) && !isManagerOrAbove(principal)) {
            return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "공지사항은 팀장 이상만 수정할 수 있습니다."));
        }

        model.addAttribute("currentUser", principal);
        model.addAttribute("boardType", type.toUpperCase());

        return Mono.zip(
                boardService.findById(id),
                menuService.findByUserId(principal.getUserId()).collectList()
        ).doOnNext(tuple -> {
            model.addAttribute("board", tuple.getT1());
            model.addAttribute("menus", tuple.getT2());
        }).thenReturn("board/" + type + "/form");
    }

    // ── 저장 ──────────────────────────────────────────
    @PostMapping("/{type}")
    public Mono<String> create(@PathVariable String type,
                                @ModelAttribute Board board,
                                @AuthenticationPrincipal CustomUserDetails principal,
                                ServerWebExchange exchange) {
        if ("notice".equalsIgnoreCase(type) && !isManagerOrAbove(principal)) {
            return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "공지사항은 팀장 이상만 작성할 수 있습니다."));
        }
        board.setBoardType(type.toUpperCase());
        return boardService.create(board, principal.getUserId())
                .flatMap(saved -> auditLogService.log(
                        principal.getUserId(), principal.getUsername(),
                        "CREATE", "BOARD_" + saved.getBoardType(), String.valueOf(saved.getId()),
                        "게시글 작성: " + saved.getTitle() + (saved.isPinned() ? " (상단고정)" : ""),
                        ipOf(exchange)
                ))
                .thenReturn("redirect:/board/" + type);
    }

    // ── 수정 ──────────────────────────────────────────
    @PostMapping("/{type}/{id}")
    public Mono<String> update(@PathVariable String type,
                                @PathVariable Long id,
                                @ModelAttribute Board board,
                                @AuthenticationPrincipal CustomUserDetails principal,
                                ServerWebExchange exchange) {
        if ("notice".equalsIgnoreCase(type) && !isManagerOrAbove(principal)) {
            return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "공지사항은 팀장 이상만 수정할 수 있습니다."));
        }
        return boardService.update(id, board)
                .flatMap(saved -> auditLogService.log(
                        principal.getUserId(), principal.getUsername(),
                        "UPDATE", "BOARD_" + saved.getBoardType(), String.valueOf(id),
                        "게시글 수정: " + saved.getTitle() + (saved.isPinned() ? " (상단고정)" : ""),
                        ipOf(exchange)
                ))
                .thenReturn("redirect:/board/" + type);
    }

    // ── 삭제 ──────────────────────────────────────────
    @PostMapping("/{type}/{id}/delete")
    public Mono<String> delete(@PathVariable String type, @PathVariable Long id,
                                @AuthenticationPrincipal CustomUserDetails principal,
                                ServerWebExchange exchange) {
        if ("notice".equalsIgnoreCase(type) && !isManagerOrAbove(principal)) {
            return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "공지사항은 팀장 이상만 삭제할 수 있습니다."));
        }
        return boardService.findById(id)
                .flatMap(board -> boardService.delete(id)
                        .then(auditLogService.log(
                                principal.getUserId(), principal.getUsername(),
                                "DELETE", "BOARD_" + board.getBoardType(), String.valueOf(id),
                                "게시글 삭제: " + board.getTitle(),
                                ipOf(exchange)
                        ))
                )
                .thenReturn("redirect:/board/" + type);
    }

    /** 게시글 목록의 작성자 ID들을 한 번에 조회해서 {작성자ID: 이름} 맵으로 만든다 */
    private Mono<Map<Long, String>> buildAuthorNameMap(List<Board> boards) {
        List<Long> authorIds = boards.stream()
                .map(Board::getAuthorId)
                .distinct()
                .collect(Collectors.toList());

        if (authorIds.isEmpty()) {
            return Mono.just(Map.of());
        }

        return userService.findAll()
                .filter(u -> authorIds.contains(u.getId()))
                .collectMap(User::getId, User::getName);
    }
}
