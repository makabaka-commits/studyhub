package com.studyhub.controller;

import com.studyhub.common.Result;
import com.studyhub.dto.CommentCreateRequest;
import com.studyhub.dto.CommentResponse;
import com.studyhub.dto.PageRequest;
import com.studyhub.dto.PageResponse;
import com.studyhub.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import com.studyhub.common.annotation.RateLimit;

import java.util.List;

@Tag(name = "评论接口")
@RestController
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @Operation(summary = "发表评论")
    @PostMapping
    public Result<Long> createComment(@RequestBody @Valid CommentCreateRequest request) {
        Long commentId = commentService.createComment(request);
        return Result.success(commentId);
    }
    @RateLimit(key = "'comment:' + #request.noteId", window = 60, limit = 5, message = "评论太频繁了")

    @Operation(summary = "查询笔记评论")
    @GetMapping("/note/{noteId}")
    public Result<List<CommentResponse>> listCommentsByNoteId(@PathVariable Long noteId) {
        return Result.success(commentService.listCommentsByNoteId(noteId));
    }

    @Operation(summary = "分页查询笔记评论")
    @GetMapping("/note/{noteId}/page")
    public Result<PageResponse<CommentResponse>> pageCommentsByNoteId(@PathVariable Long noteId, PageRequest request) {
        return Result.success(commentService.pageCommentsByNoteId(noteId, request));
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return Result.success();
    }
}
