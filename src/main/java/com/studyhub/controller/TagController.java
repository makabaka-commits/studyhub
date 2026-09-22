package com.studyhub.controller;

import com.studyhub.common.Result;
import com.studyhub.dto.NoteResponse;
import com.studyhub.dto.NoteTagBindRequest;
import com.studyhub.dto.PageRequest;
import com.studyhub.dto.PageResponse;
import com.studyhub.dto.TagCreateRequest;
import com.studyhub.entity.Tag;
import com.studyhub.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.studyhub.common.annotation.RequireAdmin;

@io.swagger.v3.oas.annotations.tags.Tag(name = "标签接口")
@RestController
@RequestMapping("/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @Operation(summary = "创建标签")
    @PostMapping
    public Result<Long> createTag(@RequestBody @Valid TagCreateRequest request) {
        return Result.success(tagService.createTag(request));
    }

    @Operation(summary = "查询标签列表")
    @GetMapping
    public Result<List<Tag>> listTags() {
        return Result.success(tagService.listTags());
    }

    @Operation(summary = "绑定标签到笔记")
    @PostMapping("/bind")
    public Result<Void> bindTagToNote(@RequestBody @Valid NoteTagBindRequest request) {
        tagService.bindTagToNote(request);
        return Result.success();
    }

    @Operation(summary = "查询笔记标签")
    @GetMapping("/note/{noteId}")
    public Result<List<Tag>> listTagsByNoteId(@PathVariable Long noteId) {
        return Result.success(tagService.listTagsByNoteId(noteId));
    }

    @Operation(summary = "查询标签下的笔记")
    @GetMapping("/{tagId}/notes")
    public Result<List<NoteResponse>> listNotesByTagId(@PathVariable Long tagId) {
        return Result.success(tagService.listNotesByTagId(tagId));
    }

    @Operation(summary = "分页查询标签下的笔记")
    @GetMapping("/{tagId}/notes/page")
    public Result<PageResponse<NoteResponse>> pageNotesByTagId(@PathVariable Long tagId, PageRequest request) {
        return Result.success(tagService.pageNotesByTagId(tagId, request));
    }

    @Operation(summary = "取消笔记标签绑定")
    @DeleteMapping("/bind")
    public Result<Void> unbindTagFromNote(@RequestBody @Valid NoteTagBindRequest request) {
        tagService.unbindTagFromNote(request);
        return Result.success();
    }

    @Operation(summary = "删除标签")
    @DeleteMapping("/{tagId}")
    @RequireAdmin
    public Result<Void> deleteTag(@PathVariable Long tagId) {
        tagService.deleteTag(tagId);
        return Result.success();
    }
}
