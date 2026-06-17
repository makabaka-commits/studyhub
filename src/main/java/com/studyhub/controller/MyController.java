package com.studyhub.controller;

import com.studyhub.common.Result;
import com.studyhub.dto.CommentResponse;
import com.studyhub.dto.NoteResponse;
import com.studyhub.dto.PageRequest;
import com.studyhub.dto.PageResponse;
import com.studyhub.service.MyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "个人中心接口")
@RestController
@RequestMapping("/me")
public class MyController {

    private final MyService myService;

    public MyController(MyService myService) {
        this.myService = myService;
    }

    @Operation(summary = "查询我的笔记")
    @GetMapping("/notes")
    public Result<List<NoteResponse>> listMyNotes() {
        return Result.success(myService.listMyNotes());
    }

    @Operation(summary = "分页查询我的笔记")
    @GetMapping("/notes/page")
    public Result<PageResponse<NoteResponse>> pageMyNotes(PageRequest request) {
        return Result.success(myService.pageMyNotes(request));
    }

    @Operation(summary = "查询我的评论")
    @GetMapping("/comments")
    public Result<List<CommentResponse>> listMyComments() {
        return Result.success(myService.listMyComments());
    }

    @Operation(summary = "分页查询我的评论")
    @GetMapping("/comments/page")
    public Result<PageResponse<CommentResponse>> pageMyComments(PageRequest request) {
        return Result.success(myService.pageMyComments(request));
    }

    @Operation(summary = "查询我的收藏")
    @GetMapping("/favorites")
    public Result<List<NoteResponse>> listMyFavoriteNotes() {
        return Result.success(myService.listMyFavoriteNotes());
    }

    @Operation(summary = "分页查询我的收藏")
    @GetMapping("/favorites/page")
    public Result<PageResponse<NoteResponse>> pageMyFavoriteNotes(PageRequest request) {
        return Result.success(myService.pageMyFavoriteNotes(request));
    }

    @Operation(summary = "查询我的点赞")
    @GetMapping("/likes")
    public Result<List<NoteResponse>> listMyLikedNotes() {
        return Result.success(myService.listMyLikedNotes());
    }

    @Operation(summary = "分页查询我的点赞")
    @GetMapping("/likes/page")
    public Result<PageResponse<NoteResponse>> pageMyLikedNotes(PageRequest request) {
        return Result.success(myService.pageMyLikedNotes(request));
    }
}
