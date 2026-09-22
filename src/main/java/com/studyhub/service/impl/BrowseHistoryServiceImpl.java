package com.studyhub.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.studyhub.common.LoginUserHolder;
import com.studyhub.converter.NoteConverter;
import com.studyhub.converter.UserConverter;
import com.studyhub.dto.NoteResponse;
import com.studyhub.dto.PageRequest;
import com.studyhub.dto.PageResponse;
import com.studyhub.dto.UserBriefResponse;
import com.studyhub.entity.BrowseHistory;
import com.studyhub.entity.Note;
import com.studyhub.entity.User;
import com.studyhub.mapper.BrowseHistoryMapper;
import com.studyhub.mapper.NoteMapper;
import com.studyhub.mapper.UserMapper;
import com.studyhub.service.BrowseHistoryService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BrowseHistoryServiceImpl implements BrowseHistoryService {

    private final BrowseHistoryMapper browseHistoryMapper;
    private final NoteMapper noteMapper;
    private final UserMapper userMapper;

    public BrowseHistoryServiceImpl(BrowseHistoryMapper browseHistoryMapper,
                                    NoteMapper noteMapper,
                                    UserMapper userMapper) {
        this.browseHistoryMapper = browseHistoryMapper;
        this.noteMapper = noteMapper;
        this.userMapper = userMapper;
    }

    @Override
    public void recordBrowse(Long userId, Long noteId) {
        browseHistoryMapper.upsert(userId, noteId);
    }

    @Override
    public List<NoteResponse> listMyBrowseHistory() {
        Long userId = LoginUserHolder.getUserId();

        LambdaQueryWrapper<BrowseHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BrowseHistory::getUserId, userId);
        queryWrapper.orderByDesc(BrowseHistory::getUpdatedAt);

        List<BrowseHistory> histories = browseHistoryMapper.selectList(queryWrapper);

        List<Long> noteIds = histories.stream()
                .map(BrowseHistory::getNoteId)
                .toList();

        if (noteIds.isEmpty()) {
            return List.of();
        }

        List<Note> notes = noteMapper.selectBatchIds(noteIds);

        return notes.stream()
                .filter(note -> Integer.valueOf(1).equals(note.getStatus()))
                .map(this::buildNoteResponse)
                .toList();
    }

    @Override
    public PageResponse<NoteResponse> pageMyBrowseHistory(PageRequest request) {
        Long userId = LoginUserHolder.getUserId();

        LambdaQueryWrapper<BrowseHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BrowseHistory::getUserId, userId);
        queryWrapper.orderByDesc(BrowseHistory::getUpdatedAt);

        Page<BrowseHistory> page = new Page<>(getPageNum(request), getPageSize(request));
        Page<BrowseHistory> resultPage = browseHistoryMapper.selectPage(page, queryWrapper);

        List<Long> noteIds = resultPage.getRecords().stream()
                .map(BrowseHistory::getNoteId)
                .toList();

        List<NoteResponse> records = buildNoteResponsesByIds(noteIds);

        PageResponse<NoteResponse> response = new PageResponse<>();
        response.setTotal(resultPage.getTotal());
        response.setPageNum(resultPage.getCurrent());
        response.setPageSize(resultPage.getSize());
        response.setRecords(records);
        return response;
    }

    @Override
    public void clearMyBrowseHistory() {
        Long userId = LoginUserHolder.getUserId();

        LambdaQueryWrapper<BrowseHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BrowseHistory::getUserId, userId);

        browseHistoryMapper.delete(queryWrapper);
    }

    private NoteResponse buildNoteResponse(Note note) {
        User user = userMapper.selectById(note.getUserId());
        UserBriefResponse author = UserConverter.toBriefResponse(user);
        return NoteConverter.toResponse(note, author);
    }

    private List<NoteResponse> buildNoteResponsesByIds(List<Long> noteIds) {
        if (noteIds.isEmpty()) {
            return List.of();
        }

        List<Note> notes = noteMapper.selectBatchIds(noteIds);
        Map<Long, Note> noteMap = notes.stream()
                .filter(note -> Integer.valueOf(1).equals(note.getStatus()))
                .collect(Collectors.toMap(Note::getId, Function.identity()));

        return noteIds.stream()
                .map(noteMap::get)
                .filter(note -> note != null)
                .map(this::buildNoteResponse)
                .toList();
    }

    private int getPageNum(PageRequest request) {
        return request.getPageNum() == null || request.getPageNum() < 1 ? 1 : request.getPageNum();
    }

    private int getPageSize(PageRequest request) {
        int pageSize = request.getPageSize() == null || request.getPageSize() < 1 ? 10 : request.getPageSize();
        return Math.min(pageSize, 50);
    }
}
