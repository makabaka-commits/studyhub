package com.studyhub.controller;

import com.studyhub.common.Result;
import com.studyhub.dto.NoteResponse;
import com.studyhub.dto.PageRequest;
import com.studyhub.dto.PageResponse;
import com.studyhub.service.BrowseHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "浏览历史接口")
@RestController
@RequestMapping("/me/browse-history")
public class BrowseHistoryController {

    private final BrowseHistoryService browseHistoryService;

    public BrowseHistoryController(BrowseHistoryService browseHistoryService) {
        this.browseHistoryService = browseHistoryService;
    }

    @Operation(summary = "查询我的浏览历史")
    @GetMapping
    public Result<List<NoteResponse>> listMyBrowseHistory() {
        return Result.success(browseHistoryService.listMyBrowseHistory());
    }

    @Operation(summary = "分页查询我的浏览历史")
    @GetMapping("/page")
    public Result<PageResponse<NoteResponse>> pageMyBrowseHistory(PageRequest request) {
        return Result.success(browseHistoryService.pageMyBrowseHistory(request));
    }

    @Operation(summary = "清空我的浏览历史")
    @DeleteMapping
    public Result<Void> clearMyBrowseHistory() {
        browseHistoryService.clearMyBrowseHistory();
        return Result.success();
    }
}
