package com.studyhub.controller;

import com.studyhub.common.Result;
import com.studyhub.dto.NotificationResponse;
import com.studyhub.dto.PageRequest;
import com.studyhub.dto.PageResponse;
import com.studyhub.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "通知接口")
@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Operation(summary = "查询我的通知")
    @GetMapping
    public Result<List<NotificationResponse>> listMyNotifications() {
        return Result.success(notificationService.listMyNotifications());
    }

    @Operation(summary = "分页查询我的通知")
    @GetMapping("/page")
    public Result<PageResponse<NotificationResponse>> pageMyNotifications(PageRequest request) {
        return Result.success(notificationService.pageMyNotifications(request));
    }

    @Operation(summary = "查询未读通知数量")
    @GetMapping("/unread-count")
    public Result<Long> countUnread() {
        return Result.success(notificationService.countUnread());
    }

    @Operation(summary = "标记通知已读")
    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return Result.success();
    }

    @Operation(summary = "全部通知标记已读")
    @PutMapping("/read-all")
    public Result<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return Result.success();
    }
}
