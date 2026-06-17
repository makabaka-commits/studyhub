package com.studyhub.controller;

import com.studyhub.common.Result;
import com.studyhub.common.annotation.RequireAdmin;
import com.studyhub.dto.*;
import com.studyhub.service.NoteService;
import com.studyhub.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "管理员接口")
@RestController
@RequestMapping("/admin")
public class AdminController {

    private final NoteService noteService;
    private final UserService userService;

    public AdminController(NoteService noteService, UserService userService) {
        this.noteService = noteService;
        this.userService = userService;
    }

    @Operation(summary = "审核通过笔记")
    @PutMapping("/notes/{noteId}/approve")
    @RequireAdmin
    public Result<Void> approveNote(@PathVariable Long noteId) {
        noteService.approveNote(noteId);
        return Result.success();
    }

    @Operation(summary = "拒绝笔记")
    @PutMapping("/notes/{noteId}/reject")
    @RequireAdmin
    public Result<Void> rejectNote(@PathVariable Long noteId) {
        noteService.rejectNote(noteId);
        return Result.success();
    }

    @Operation(summary = "分页查询待审核笔记")
    @GetMapping("/notes/pending")
    @RequireAdmin
    public Result<PageResponse<NoteResponse>> pagePendingNotes(NotePageRequest request) {
        return Result.success(noteService.pagePendingNotes(request));
    }

    @Operation(summary = "查询所有用户列表")
    @GetMapping("/users")
    @RequireAdmin
    public Result<List<UserBriefResponse>> listAllUsers() {
        return Result.success(userService.listAllUsers());
    }

    @Operation(summary = "禁用用户")
    @PutMapping("/users/{userId}/ban")
    @RequireAdmin
    public Result<Void> banUser(@PathVariable Long userId) {
        userService.banUser(userId);
        return Result.success();
    }

    @Operation(summary = "启用用户")
    @PutMapping("/users/{userId}/unban")
    @RequireAdmin
    public Result<Void> unbanUser(@PathVariable Long userId) {
        userService.unbanUser(userId);
        return Result.success();
    }

    @Operation(summary = "获取仪表盘统计数据")
    @GetMapping("/dashboard")
    @RequireAdmin
    public Result<DashboardResponse> getDashboard() {
        return Result.success(noteService.getDashboard());
    }
}