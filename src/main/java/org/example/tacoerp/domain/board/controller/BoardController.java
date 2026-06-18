package org.example.tacoerp.domain.board.controller;

import lombok.RequiredArgsConstructor;
import org.example.tacoerp.domain.board.entity.Board;
import org.example.tacoerp.domain.board.service.BoardService;
import org.example.tacoerp.domain.user.service.MenuService;
import org.example.tacoerp.global.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/board")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;
    private final MenuService menuService;

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

        var boardsMono = (keyword != null && !keyword.isBlank())
                ? boardService.search(type, keyword).collectList()
                : boardService.findByType(type).collectList();

        return Mono.zip(boardsMono, menuService.findByUserId(principal.getUserId()).collectList())
                .doOnNext(tuple -> {
                    model.addAttribute("boards", tuple.getT1());
                    model.addAttribute("menus", tuple.getT2());
                })
                .thenReturn("board/" + view + "/list");
    }

    // ── 상세 ──────────────────────────────────────────
    @GetMapping("/{type}/{id}")
    public Mono<String> detail(@PathVariable String type,
                                @PathVariable Long id,
                                @AuthenticationPrincipal CustomUserDetails principal,
                                Model model) {
        model.addAttribute("currentUser", principal);
        model.addAttribute("boardType", type.toUpperCase());

        return boardService.incrementView(id)
                .flatMap(board -> menuService.findByUserId(principal.getUserId()).collectList()
                        .doOnNext(menus -> {
                            model.addAttribute("board", board);
                            model.addAttribute("menus", menus);
                        })
                )
                .thenReturn("board/" + type + "/detail");
    }

    // ── 등록 폼 ──────────────────────────────────────
    @GetMapping("/{type}/new")
    public Mono<String> createForm(@PathVariable String type,
                                    @AuthenticationPrincipal CustomUserDetails principal,
                                    Model model) {
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
                                @AuthenticationPrincipal CustomUserDetails principal) {
        board.setBoardType(type.toUpperCase());
        return boardService.create(board, principal.getUserId())
                .thenReturn("redirect:/board/" + type);
    }

    // ── 수정 ──────────────────────────────────────────
    @PostMapping("/{type}/{id}")
    public Mono<String> update(@PathVariable String type,
                                @PathVariable Long id,
                                @ModelAttribute Board board) {
        return boardService.update(id, board)
                .thenReturn("redirect:/board/" + type);
    }

    // ── 삭제 ──────────────────────────────────────────
    @PostMapping("/{type}/{id}/delete")
    public Mono<String> delete(@PathVariable String type, @PathVariable Long id) {
        return boardService.delete(id)
                .thenReturn("redirect:/board/" + type);
    }
}
