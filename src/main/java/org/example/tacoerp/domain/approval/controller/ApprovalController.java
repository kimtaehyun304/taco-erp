package org.example.tacoerp.domain.approval.controller;

import lombok.RequiredArgsConstructor;
import org.example.tacoerp.domain.approval.entity.ApprovalDocument;
import org.example.tacoerp.domain.approval.service.ApprovalService;
import org.example.tacoerp.domain.user.service.MenuService;
import org.example.tacoerp.domain.user.service.UserService;
import org.example.tacoerp.global.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Controller
@RequestMapping("/approval")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService approvalService;
    private final UserService userService;
    private final MenuService menuService;

    // ── 기안함 ────────────────────────────────────────────
    @GetMapping("/drafts")
    public Mono<String> drafts(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        model.addAttribute("currentUser", principal);
        return Mono.zip(
                approvalService.findMyDrafts(principal.getUserId()).collectList(),
                menuService.findByUserId(principal.getUserId()).collectList()
        ).doOnNext(t -> {
            model.addAttribute("documents", t.getT1());
            model.addAttribute("menus", t.getT2());
        }).thenReturn("approval/drafts");
    }

    // ── 결재함 ────────────────────────────────────────────
    @GetMapping("/inbox")
    public Mono<String> inbox(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        model.addAttribute("currentUser", principal);
        return Mono.zip(
                approvalService.findInbox(principal.getUserId()).collectList(),
                menuService.findByUserId(principal.getUserId()).collectList()
        ).doOnNext(t -> {
            model.addAttribute("documents", t.getT1());
            model.addAttribute("menus", t.getT2());
        }).thenReturn("approval/inbox");
    }

    // ── 기안 작성 폼 ──────────────────────────────────────
    @GetMapping("/new")
    public Mono<String> newForm(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        model.addAttribute("currentUser", principal);
        model.addAttribute("document", new ApprovalDocument());
        return Mono.zip(
                userService.findAll().collectList(),
                menuService.findByUserId(principal.getUserId()).collectList()
        ).doOnNext(t -> {
            model.addAttribute("users", t.getT1());
            model.addAttribute("menus", t.getT2());
        }).thenReturn("approval/form");
    }

    // ── 문서 상세 ─────────────────────────────────────────
    @GetMapping("/{id}")
    public Mono<String> detail(@PathVariable Long id,
                                @AuthenticationPrincipal CustomUserDetails principal,
                                Model model) {
        model.addAttribute("currentUser", principal);
        return Mono.zip(
                approvalService.findById(id),
                approvalService.findLines(id).collectList(),
                userService.findAll().collectList(),
                menuService.findByUserId(principal.getUserId()).collectList()
        ).doOnNext(t -> {
            model.addAttribute("document", t.getT1());
            model.addAttribute("lines", t.getT2());
            model.addAttribute("users", t.getT3());
            model.addAttribute("menus", t.getT4());
        }).thenReturn("approval/detail");
    }

    // ── 기안 제출 ─────────────────────────────────────────
    @PostMapping
    public Mono<String> submit(@ModelAttribute ApprovalDocument document,
                                @RequestParam(value = "approverIds", required = false) List<Long> approverIds,
                                @AuthenticationPrincipal CustomUserDetails principal) {
        document.setDrafterId(principal.getUserId());
        List<Long> finalApprovers = approverIds != null ? approverIds : List.of();
        return approvalService.draft(document, finalApprovers)
                .thenReturn("redirect:/approval/drafts");
    }

    // ── 결재 승인 ─────────────────────────────────────────
    @PostMapping("/lines/{lineId}/approve")
    public Mono<String> approve(@PathVariable Long lineId,
                                 @RequestParam(required = false, defaultValue = "") String comment,
                                 @AuthenticationPrincipal CustomUserDetails principal,
                                 ServerWebExchange exchange) {
        return approvalService.approve(lineId, comment, principal.getUserId())
                .flatMap(line -> Mono.just("redirect:/approval/" + line.getDocumentId()));
    }

    // ── 결재 반려 ─────────────────────────────────────────
    @PostMapping("/lines/{lineId}/reject")
    public Mono<String> reject(@PathVariable Long lineId,
                                @RequestParam(required = false, defaultValue = "") String comment) {
        return approvalService.reject(lineId, comment)
                .flatMap(line -> Mono.just("redirect:/approval/" + line.getDocumentId()));
    }

    // ── 기안 취소 ─────────────────────────────────────────
    @PostMapping("/{id}/cancel")
    public Mono<String> cancel(@PathVariable Long id) {
        return approvalService.cancel(id)
                .thenReturn("redirect:/approval/drafts");
    }

    // ── 알림 목록 ─────────────────────────────────────────
    @GetMapping("/notifications")
    public Mono<String> notifications(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        model.addAttribute("currentUser", principal);
        return Mono.zip(
                approvalService.findNotifications(principal.getUserId()).collectList(),
                menuService.findByUserId(principal.getUserId()).collectList()
        ).doOnNext(t -> {
            model.addAttribute("notifications", t.getT1());
            model.addAttribute("menus", t.getT2());
        })
        .flatMap(t -> approvalService.markAllRead(principal.getUserId()))
        .thenReturn("approval/notifications");
    }
}
