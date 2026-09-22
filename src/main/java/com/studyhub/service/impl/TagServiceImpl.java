package com.studyhub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.studyhub.common.ErrorCode;
import com.studyhub.converter.NoteConverter;
import com.studyhub.converter.UserConverter;
import com.studyhub.dto.NoteResponse;
import com.studyhub.dto.NoteTagBindRequest;
import com.studyhub.dto.PageRequest;
import com.studyhub.dto.PageResponse;
import com.studyhub.dto.TagCreateRequest;
import com.studyhub.dto.UserBriefResponse;
import com.studyhub.entity.Note;
import com.studyhub.entity.NoteTag;
import com.studyhub.entity.NoteStatus;
import com.studyhub.common.LoginUserHolder;
import com.studyhub.entity.Tag;
import com.studyhub.entity.User;
import com.studyhub.exception.BusinessException;
import com.studyhub.mapper.NoteMapper;
import com.studyhub.mapper.NoteTagMapper;
import com.studyhub.mapper.TagMapper;
import com.studyhub.mapper.UserMapper;
import com.studyhub.service.TagService;
import org.springframework.stereotype.Service;
import com.studyhub.common.CacheUtil;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TagServiceImpl implements TagService {

    private final TagMapper tagMapper;
    private final NoteTagMapper noteTagMapper;
    private final NoteMapper noteMapper;
    private final UserMapper userMapper;
    private final CacheUtil cacheUtil;

    public TagServiceImpl(TagMapper tagMapper,
                          NoteTagMapper noteTagMapper,
                          NoteMapper noteMapper,
                          UserMapper userMapper,
                          CacheUtil cacheUtil) {
        this.tagMapper = tagMapper;
        this.noteTagMapper = noteTagMapper;
        this.noteMapper = noteMapper;
        this.userMapper = userMapper;
        this.cacheUtil = cacheUtil;
    }

    @Override
    public Long createTag(TagCreateRequest request) {
        String name = request.getName().trim();

        LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Tag::getName, name);

        Tag existing = tagMapper.selectOne(queryWrapper);
        if (existing != null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "tag already exists");
        }

        Tag tag = new Tag();
        tag.setName(name);

        int rows = tagMapper.insert(tag);
        if (rows != 1) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "create tag failed");
        }
        cacheUtil.evict("tags:list");
        return tag.getId();
    }

    @Override
    public List<Tag> listTags() {
        String cacheKey = "tags:list";
        List<Tag> cached = cacheUtil.get(cacheKey,
                new com.fasterxml.jackson.core.type.TypeReference<List<Tag>>() {});
        if (cached != null) {
            return cached;
        }

        List<Tag> tags = tagMapper.selectList(null);
        cacheUtil.put(cacheKey, tags, 300L);
        return tags;
    }

    @Override
    public void bindTagToNote(NoteTagBindRequest request) {
        Note note = noteMapper.selectById(request.getNoteId());
        if (note == null || NoteStatus.DELETED.equals(note.getStatus())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "note not found");
        }
        if (!note.getUserId().equals(LoginUserHolder.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "no permission");
        }

        Tag tag = tagMapper.selectById(request.getTagId());
        if (tag == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "tag not found");
        }

        LambdaQueryWrapper<NoteTag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NoteTag::getNoteId, request.getNoteId());
        queryWrapper.eq(NoteTag::getTagId, request.getTagId());

        NoteTag existing = noteTagMapper.selectOne(queryWrapper);
        if (existing != null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "tag already bound");
        }

        NoteTag noteTag = new NoteTag();
        noteTag.setNoteId(request.getNoteId());
        noteTag.setTagId(request.getTagId());

        noteTagMapper.insert(noteTag);
    }

    @Override
    public List<Tag> listTagsByNoteId(Long noteId) {
        LambdaQueryWrapper<NoteTag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NoteTag::getNoteId, noteId);

        List<NoteTag> noteTags = noteTagMapper.selectList(queryWrapper);
        List<Long> tagIds = noteTags.stream()
                .map(NoteTag::getTagId)
                .toList();

        if (tagIds.isEmpty()) {
            return List.of();
        }

        return tagMapper.selectBatchIds(tagIds);
    }

    @Override
    public List<NoteResponse> listNotesByTagId(Long tagId) {
        LambdaQueryWrapper<NoteTag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NoteTag::getTagId, tagId);

        List<NoteTag> noteTags = noteTagMapper.selectList(queryWrapper);
        List<Long> noteIds = noteTags.stream()
                .map(NoteTag::getNoteId)
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
    public PageResponse<NoteResponse> pageNotesByTagId(Long tagId, PageRequest request) {
        LambdaQueryWrapper<NoteTag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NoteTag::getTagId, tagId);

        Page<NoteTag> page = new Page<>(getPageNum(request), getPageSize(request));
        Page<NoteTag> resultPage = noteTagMapper.selectPage(page, queryWrapper);

        List<Long> noteIds = resultPage.getRecords().stream()
                .map(NoteTag::getNoteId)
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
    public void unbindTagFromNote(NoteTagBindRequest request) {
        Note note = noteMapper.selectById(request.getNoteId());
        if (note == null || NoteStatus.DELETED.equals(note.getStatus())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "note not found");
        }
        if (!note.getUserId().equals(LoginUserHolder.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "no permission");
        }
        LambdaQueryWrapper<NoteTag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NoteTag::getNoteId, request.getNoteId());
        queryWrapper.eq(NoteTag::getTagId, request.getTagId());

        NoteTag existing = noteTagMapper.selectOne(queryWrapper);
        if (existing == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "tag binding not found");
        }

        noteTagMapper.deleteById(existing.getId());
    }

    @Override
    public void deleteTag(Long tagId) {
        Tag tag = tagMapper.selectById(tagId);
        if (tag == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "tag not found");
        }

        LambdaQueryWrapper<NoteTag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NoteTag::getTagId, tagId);
        noteTagMapper.delete(queryWrapper);

        tagMapper.deleteById(tagId);
        cacheUtil.evict("tags:list");
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
