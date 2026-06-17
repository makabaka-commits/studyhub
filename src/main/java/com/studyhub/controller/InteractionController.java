package com.studyhub.controller;

import com.studyhub.common.Result;
import com.studyhub.service.InteractionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import com.studyhub.common.annotation.RateLimit;

@Tag(name = "互动接口")
@RestController
@RequestMapping("/notes")
public class InteractionController {

    private final InteractionService interactionService;

    public InteractionController(InteractionService interactionService) {
        this.interactionService = interactionService;
    }

    @Operation(summary = "点赞笔记")
    @PostMapping("/{noteId}/like")
    @RateLimit(key = "'like:' + #noteId", window = 10, limit = 3, message = "点赞太频繁了，请稍后再试")
    public Result<Void> likeNote(@PathVariable Long noteId) {
        interactionService.likeNote(noteId);
        return Result.success();
    }

    @Operation(summary = "取消点赞")
    @DeleteMapping("/{noteId}/like")
    public Result<Void> unlikeNote(@PathVariable Long noteId) {
        interactionService.unlikeNote(noteId);
        return Result.success();
    }

    @Operation(summary = "收藏笔记")
    @PostMapping("/{noteId}/favorite")
    @RateLimit(key = "'favorite:' + #noteId", window = 10, limit = 3, message = "收藏太频繁了，请稍后再试")
    public Result<Void> favoriteNote(@PathVariable Long noteId) {
        interactionService.favoriteNote(noteId);
        return Result.success();
    }

    @Operation(summary = "取消收藏")
    @DeleteMapping("/{noteId}/favorite")
    public Result<Void> unfavoriteNote(@PathVariable Long noteId) {
        interactionService.unfavoriteNote(noteId);
        return Result.success();
    }
}
