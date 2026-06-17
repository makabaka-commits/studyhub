package com.studyhub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studyhub.common.ErrorCode;
import com.studyhub.dto.DashboardResponse;
import com.studyhub.dto.*;
import com.studyhub.entity.Note;
import com.studyhub.entity.NoteStatus;
import com.studyhub.exception.BusinessException;
import com.studyhub.mapper.NoteMapper;
import com.studyhub.mq.BrowseHistoryProducer;
import com.studyhub.service.NoteService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.studyhub.converter.NoteConverter;
import com.studyhub.converter.UserConverter;
import com.studyhub.entity.User;
import com.studyhub.mapper.UserMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.studyhub.service.AiSummaryService;
import com.studyhub.common.LoginUserHolder;
import com.studyhub.service.BrowseHistoryService;
import com.studyhub.entity.NoteTag;
import com.studyhub.entity.Tag;
import com.studyhub.mapper.CommentMapper;
import com.studyhub.mapper.NoteTagMapper;
import com.studyhub.service.TagService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClient;
import com.studyhub.common.CacheUtil;

@Service
public class NoteServiceImpl implements NoteService {

    private static final String HOT_NOTE_KEY = "studyhub:hot:notes";
    private final BrowseHistoryService browseHistoryService;

    private final NoteMapper noteMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final AiSummaryService aiSummaryService;
    private final UserMapper userMapper;
    private final TagService tagService;
    private final NoteTagMapper noteTagMapper;
    private final RestClient aiRestClient;
    private final CacheUtil cacheUtil;
    private final BrowseHistoryProducer browseHistoryProducer;
    private final CommentMapper commentMapper;
    @Value("${ai.deepseek.model}")
    private String model;

    public NoteServiceImpl(NoteMapper noteMapper,
                           StringRedisTemplate stringRedisTemplate,
                           AiSummaryService aiSummaryService,
                           UserMapper userMapper,
                           BrowseHistoryService browseHistoryService,
                           TagService tagService,
                           NoteTagMapper noteTagMapper,
                           RestClient aiRestClient,
                           CacheUtil cacheUtil,
                           BrowseHistoryProducer browseHistoryProducer,
                           CommentMapper commentMapper) {
        this.noteMapper = noteMapper;
        this.stringRedisTemplate = stringRedisTemplate;
        this.aiSummaryService = aiSummaryService;
        this.userMapper = userMapper;
        this.browseHistoryService = browseHistoryService;
        this.tagService = tagService;
        this.noteTagMapper = noteTagMapper;
        this.aiRestClient = aiRestClient;
        this.cacheUtil = cacheUtil;
        this.browseHistoryProducer = browseHistoryProducer;
        this.commentMapper = commentMapper;
    }
    private NoteResponse buildNoteResponse(Note note) {
        User user = userMapper.selectById(note.getUserId());
        UserBriefResponse author = UserConverter.toBriefResponse(user);
        return NoteConverter.toResponse(note, author);
    }

    @Override
    public Long createNote(NoteCreateRequest request) {
        Long userId = LoginUserHolder.getUserId();
        Note note = new Note();

        note.setUserId(userId);
        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        note.setViewCount(0);
        note.setLikeCount(0);
        note.setFavoriteCount(0);
        note.setStatus(1);

        int rows = noteMapper.insert(note);
        if (rows != 1) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "create note failed");
        }
        // 清除笔记列表缓存
        cacheUtil.evict("notes:list");
        // 清除热门笔记缓存（如果有的话）
        stringRedisTemplate.delete(HOT_NOTE_KEY);
        return note.getId();
    }

    @Override
    public NoteResponse getNoteById(Long id) {
        Note note = noteMapper.selectById(id);
        if (note == null || Integer.valueOf(0).equals(note.getStatus())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "note not found");
        }

        int viewCount = note.getViewCount() == null ? 0 : note.getViewCount();
        note.setViewCount(viewCount + 1);
        noteMapper.updateById(note);

        stringRedisTemplate.opsForZSet().incrementScore(HOT_NOTE_KEY, String.valueOf(id), 1);
        Long userId = LoginUserHolder.getUserId();
        if (userId != null) {
            browseHistoryProducer.sendBrowseHistory(new BrowseHistoryMessage(userId, id));
        }
        return buildNoteResponse(note);
    }

    @Override
    public List<NoteResponse> listNotes() {
        String cacheKey = "notes:list";
        List<NoteResponse> cached = cacheUtil.get(cacheKey,
                new com.fasterxml.jackson.core.type.TypeReference<List<NoteResponse>>() {});
        if (cached != null) {
            return cached;
        }

        // 2. 缓存没有，查数据库
        LambdaQueryWrapper<Note> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Note::getStatus, NoteStatus.APPROVED);
        queryWrapper.orderByDesc(Note::getCreatedAt);

        List<Note> notes = noteMapper.selectList(queryWrapper);
        List<NoteResponse> result = notes.stream()
                .map(this::buildNoteResponse)
                .toList();

        // 3. 存入缓存，过期时间 5 分钟
        cacheUtil.put(cacheKey, result, 300L);

        return result;
    }

    @Override
    public List<NoteResponse> listHotNotes() {
        Set<String> noteIdSet = stringRedisTemplate.opsForZSet()
                .reverseRange(HOT_NOTE_KEY, 0, 9);

        if (noteIdSet == null || noteIdSet.isEmpty()) {
            LambdaQueryWrapper<Note> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Note::getStatus, NoteStatus.APPROVED);
            queryWrapper.orderByDesc(Note::getViewCount);

            Page<Note> page = new Page<>(1, 10);
            List<Note> notes = noteMapper.selectPage(page, queryWrapper).getRecords();
            for (Note note : notes) {
                int viewCount = note.getViewCount() == null ? 0 : note.getViewCount();
                stringRedisTemplate.opsForZSet()
                        .add(HOT_NOTE_KEY, String.valueOf(note.getId()), viewCount);
            }
            return notes.stream()
                    .map(this::buildNoteResponse)
                    .toList();
        }

        List<Long> noteIds = noteIdSet.stream()
                .map(Long::valueOf)
                .toList();

        List<Note> notes = noteMapper.selectBatchIds(noteIds);
        Map<Long, Note> noteMap = notes.stream()
                .filter(note -> Integer.valueOf(1).equals(note.getStatus()))
                .collect(Collectors.toMap(Note::getId, Function.identity()));

        List<Note> result = new ArrayList<>();
        for (Long noteId : noteIds) {
            Note note = noteMap.get(noteId);
            if (note != null) {
                result.add(note);
            }
        }

        return result.stream()
                .map(this::buildNoteResponse)
                .toList();
    }

    @Override
    public void updateNote(NoteUpdateRequest request) {
        Long userId = LoginUserHolder.getUserId();

        Note note = noteMapper.selectById(request.getId());
        if (note == null || Integer.valueOf(0).equals(note.getStatus())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "note not found");
        }

        if (!note.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "no permission");
        }

        note.setTitle(request.getTitle());
        note.setContent(request.getContent());

        int rows = noteMapper.updateById(note);
        if (rows != 1) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "update note failed");
        }
        cacheUtil.evict("notes:list");
    }

    @Override
    public void deleteNote(Long id) {
        Long userId = LoginUserHolder.getUserId();

        Note note = noteMapper.selectById(id);
        if (note == null || Integer.valueOf(0).equals(note.getStatus())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "note not found");
        }

        if (!note.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "no permission");
        }

        note.setStatus(0);
        noteMapper.updateById(note);
        cacheUtil.evict("notes:list");
        stringRedisTemplate.opsForZSet().remove(HOT_NOTE_KEY, String.valueOf(id));
    }

    @Override
    public String generateSummary(Long noteId) {
        Note note = noteMapper.selectById(noteId);
        if (note == null || Integer.valueOf(0).equals(note.getStatus())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "note not found");
        }

        String summary = aiSummaryService.generateSummary(note.getContent());

        note.setSummary(summary);

        int rows = noteMapper.updateById(note);
        if (rows != 1) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "generate summary failed");
        }

        return summary;
    }

    @Override
    public PageResponse<NoteResponse> pageNotes(NotePageRequest request) {
        int pageNum = request.getPageNum() == null || request.getPageNum() < 1 ? 1 : request.getPageNum();
        int pageSize = request.getPageSize() == null || request.getPageSize() < 1 ? 10 : request.getPageSize();

        if (pageSize > 50) {
            pageSize = 50;
        }

        LambdaQueryWrapper<Note> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Note::getStatus, NoteStatus.APPROVED);

        String keyword = request.getKeyword();
        if (keyword != null && !keyword.isBlank()) {
            queryWrapper.and(wrapper -> wrapper
                    .like(Note::getTitle, keyword)
                    .or()
                    .like(Note::getContent, keyword));
        }

        queryWrapper.orderByDesc(Note::getCreatedAt);

        Page<Note> page = new Page<>(pageNum, pageSize);
        Page<Note> resultPage = noteMapper.selectPage(page, queryWrapper);

        List<NoteResponse> records = resultPage.getRecords().stream()
                .map(this::buildNoteResponse)
                .toList();

        PageResponse<NoteResponse> response = new PageResponse<>();
        response.setTotal(resultPage.getTotal());
        response.setPageNum(resultPage.getCurrent());
        response.setPageSize(resultPage.getSize());
        response.setRecords(records);

        return response;
    }

    @Override
    public List<NoteResponse> recommendNotes(Long noteId, int limit) {
        // 1. 获取当前笔记的标签
        List<Tag> tags = tagService.listTagsByNoteId(noteId);
        if (tags.isEmpty()) {
            return List.of();
        }

        List<Long> tagIds = tags.stream().map(Tag::getId).toList();

        // 2. 查找有相同标签的其他笔记
        LambdaQueryWrapper<NoteTag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(NoteTag::getTagId, tagIds);
        queryWrapper.ne(NoteTag::getNoteId, noteId); // 排除当前笔记

        List<NoteTag> noteTags = noteTagMapper.selectList(queryWrapper);

        // 3. 按标签匹配数量排序，取前 limit 个
        Map<Long, Long> noteCount = noteTags.stream()
                .collect(Collectors.groupingBy(NoteTag::getNoteId, Collectors.counting()));

        List<Long> recommendIds = noteCount.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .toList();

        // 4. 查询笔记详情
        List<Note> notes = noteMapper.selectBatchIds(recommendIds);
        return notes.stream()
                .filter(n -> Integer.valueOf(1).equals(n.getStatus()))
                .map(this::buildNoteResponse)
                .toList();
    }

    @Override
    public String askNote(Long noteId, String question) {
        // 1. 查询笔记内容
        Note note = noteMapper.selectById(noteId);
        if (note == null || Integer.valueOf(0).equals(note.getStatus())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "note not found");
        }

        // 2. 构建 AI 请求：把笔记内容作为上下文
        AiSummaryRequest request = new AiSummaryRequest();
        request.setModel(model);
        request.setTemperature(0.7);

        AiSummaryRequest.Message systemMsg = new AiSummaryRequest.Message();
        systemMsg.setRole("system");
        systemMsg.setContent("你是一个笔记问答助手。用户会提供一篇笔记内容，然后问你关于这篇笔记的问题。请基于笔记内容回答，如果问题超出笔记范围，请说明'笔记中没有相关信息'。回答要简洁准确。");

        AiSummaryRequest.Message userMsg = new AiSummaryRequest.Message();
        userMsg.setRole("user");
        userMsg.setContent("笔记内容：\n" + note.getContent() + "\n\n问题：" + question);

        request.setMessages(List.of(systemMsg, userMsg));

        // 3. 调用 AI API
        AiSummaryResponse response = aiRestClient.post()
                .uri("/v1/chat/completions")
                .body(request)
                .retrieve()
                .body(AiSummaryResponse.class);

        if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
            return response.getChoices().get(0).getMessage().getContent();
        }

        return "抱歉，暂时无法回答这个问题";
    }

    @Override
    public void approveNote(Long noteId) {
        Note note = noteMapper.selectById(noteId);
        if (note == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "笔记不存在");
        }
        if (!Integer.valueOf(0).equals(note.getStatus())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "该笔记不是待审核状态");
        }

        note.setStatus(1);
        noteMapper.updateById(note);

        cacheUtil.evict("notes:list");
    }

    @Override
    public void rejectNote(Long noteId) {
        Note note = noteMapper.selectById(noteId);
        if (note == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "笔记不存在");
        }
        if (!Integer.valueOf(0).equals(note.getStatus())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "该笔记不是待审核状态");
        }

        note.setStatus(2);
        noteMapper.updateById(note);
    }

    @Override
    public PageResponse<NoteResponse> pagePendingNotes(NotePageRequest request) {
        int pageNum = request.getPageNum() == null || request.getPageNum() < 1 ? 1 : request.getPageNum();
        int pageSize = request.getPageSize() == null || request.getPageSize() < 1 ? 10 : request.getPageSize();
        if (pageSize > 50) {
            pageSize = 50;
        }

        LambdaQueryWrapper<Note> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Note::getStatus, 0);
        queryWrapper.orderByDesc(Note::getCreatedAt);

        String keyword = request.getKeyword();
        if (keyword != null && !keyword.isBlank()) {
            queryWrapper.and(wrapper -> wrapper
                    .like(Note::getTitle, keyword)
                    .or()
                    .like(Note::getContent, keyword));
        }

        Page<Note> page = new Page<>(pageNum, pageSize);
        Page<Note> resultPage = noteMapper.selectPage(page, queryWrapper);

        List<NoteResponse> records = resultPage.getRecords().stream()
                .map(this::buildNoteResponse)
                .toList();

        PageResponse<NoteResponse> response = new PageResponse<>();
        response.setTotal(resultPage.getTotal());
        response.setPageNum(resultPage.getCurrent());
        response.setPageSize(resultPage.getSize());
        response.setRecords(records);

        return response;
    }

    @Override
    public DashboardResponse getDashboard() {
        DashboardResponse response = new DashboardResponse();

        response.setTotalUsers(userMapper.selectCount(null));

        LambdaQueryWrapper<Note> approvedWrapper = new LambdaQueryWrapper<>();
        approvedWrapper.eq(Note::getStatus, NoteStatus.APPROVED);
        response.setTotalNotes(noteMapper.selectCount(approvedWrapper));

        LambdaQueryWrapper<Note> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.eq(Note::getStatus, NoteStatus.APPROVED);
        todayWrapper.ge(Note::getCreatedAt, LocalDateTime.now().withHour(0).withMinute(0).withSecond(0));
        response.setTodayNotes(noteMapper.selectCount(todayWrapper));

        response.setTotalComments(commentMapper.selectCount(null));

        LambdaQueryWrapper<Note> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(Note::getStatus, 0);
        response.setPendingNotes(noteMapper.selectCount(pendingWrapper));

        List<Note> allNotes = noteMapper.selectList(null);
        long totalViews = allNotes.stream()
                .mapToLong(n -> n.getViewCount() == null ? 0L : n.getViewCount())
                .sum();
        response.setTotalViews(totalViews);

        return response;
    }
}
