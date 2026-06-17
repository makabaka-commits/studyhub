package com.studyhub.controller;

import com.studyhub.common.ErrorCode;
import com.studyhub.common.Result;
import com.studyhub.dto.NoteAskRequest;
import com.studyhub.dto.NoteCreateRequest;
import com.studyhub.dto.NoteResponse;
import com.studyhub.dto.NoteUpdateRequest;
import com.studyhub.exception.BusinessException;
import com.studyhub.service.NoteService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import com.studyhub.dto.NotePageRequest;
import com.studyhub.dto.PageResponse;

import java.util.List;

@Tag(name = "笔记接口")
@RestController
@RequestMapping("/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @Operation(summary = "发布笔记")
    @PostMapping
    public Result<Long> createNote(@RequestBody @Valid NoteCreateRequest request) {
        Long noteId = noteService.createNote(request);
        return Result.success(noteId);
    }

    @Operation(summary = "查询笔记列表")
    @GetMapping
    public Result<List<NoteResponse>> listNotes() {
        return Result.success(noteService.listNotes());
    }

    @Operation(summary = "查询热门笔记")
    @GetMapping("/hot")
    public Result<List<NoteResponse>> listHotNotes() {
        return Result.success(noteService.listHotNotes());
    }

    @Operation(summary = "生成笔记摘要")
    @PostMapping("/{id}/summary")
    public Result<String> generateSummary(@PathVariable Long id) {
        String summary = noteService.generateSummary(id);
        return Result.success(summary);
    }

    @Operation(summary = "查询笔记详情")
    @GetMapping("/{id}")
    public Result<NoteResponse> getNoteById(@PathVariable Long id) {
        return Result.success(noteService.getNoteById(id));
    }

    @Operation(summary = "分页搜索笔记")
    @GetMapping("/page")
    public Result<PageResponse<NoteResponse>> pageNotes(NotePageRequest request) {
        return Result.success(noteService.pageNotes(request));
    }

    @Operation(summary = "修改笔记")
    @PutMapping
    public Result<Void> updateNote(@RequestBody @Valid NoteUpdateRequest request) {
        noteService.updateNote(request);
        return Result.success();
    }

    @Operation(summary = "推荐相似笔记")
    @GetMapping("/{id}/recommend")
    public Result<List<NoteResponse>> recommendNotes(@PathVariable Long id,
                                                     @RequestParam(defaultValue = "5") int limit) {
        return Result.success(noteService.recommendNotes(id, limit));
    }

    @Operation(summary = "删除笔记")
    @DeleteMapping("/{id}")
    public Result<Void> deleteNote(@PathVariable Long id) {
        noteService.deleteNote(id);
        return Result.success();
    }

    @Operation(summary = "AI 智能问答")
    @PostMapping("/{id}/ask")
    public Result<String> askNote(@PathVariable Long id,
                                  @RequestBody @Valid NoteAskRequest request) {
        // 确保路径中的 id 和请求体中的 noteId 一致
        if (!id.equals(request.getNoteId())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "noteId mismatch");
        }
        String answer = noteService.askNote(id, request.getQuestion());
        return Result.success(answer);
    }
}
