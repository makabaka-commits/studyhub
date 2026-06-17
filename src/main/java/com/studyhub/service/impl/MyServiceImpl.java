package com.studyhub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.studyhub.common.LoginUserHolder;
import com.studyhub.converter.CommentConverter;
import com.studyhub.converter.NoteConverter;
import com.studyhub.converter.UserConverter;
import com.studyhub.dto.CommentResponse;
import com.studyhub.dto.NoteResponse;
import com.studyhub.dto.PageRequest;
import com.studyhub.dto.PageResponse;
import com.studyhub.dto.UserBriefResponse;
import com.studyhub.entity.Comment;
import com.studyhub.entity.Favorite;
import com.studyhub.entity.Note;
import com.studyhub.entity.NoteLike;
import com.studyhub.entity.NoteStatus;
import com.studyhub.entity.User;
import com.studyhub.mapper.CommentMapper;
import com.studyhub.mapper.FavoriteMapper;
import com.studyhub.mapper.NoteLikeMapper;
import com.studyhub.mapper.NoteMapper;
import com.studyhub.mapper.UserMapper;
import com.studyhub.service.MyService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MyServiceImpl implements MyService {

    private final NoteMapper noteMapper;
    private final CommentMapper commentMapper;
    private final FavoriteMapper favoriteMapper;
    private final NoteLikeMapper noteLikeMapper;
    private final UserMapper userMapper;

    public MyServiceImpl(NoteMapper noteMapper,
                         CommentMapper commentMapper,
                         FavoriteMapper favoriteMapper,
                         NoteLikeMapper noteLikeMapper,
                         UserMapper userMapper) {
        this.noteMapper = noteMapper;
        this.commentMapper = commentMapper;
        this.favoriteMapper = favoriteMapper;
        this.noteLikeMapper = noteLikeMapper;
        this.userMapper = userMapper;
    }

    @Override
    public List<NoteResponse> listMyNotes() {
        Long userId = LoginUserHolder.getUserId();

        LambdaQueryWrapper<Note> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Note::getUserId, userId);
        queryWrapper.eq(Note::getStatus, NoteStatus.APPROVED);
        queryWrapper.orderByDesc(Note::getCreatedAt);

        List<Note> notes = noteMapper.selectList(queryWrapper);

        return notes.stream()
                .map(this::buildNoteResponse)
                .toList();
    }

    @Override
    public PageResponse<NoteResponse> pageMyNotes(PageRequest request) {
        Long userId = LoginUserHolder.getUserId();

        LambdaQueryWrapper<Note> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Note::getUserId, userId);
        queryWrapper.eq(Note::getStatus, NoteStatus.APPROVED);
        queryWrapper.orderByDesc(Note::getCreatedAt);

        Page<Note> page = new Page<>(getPageNum(request), getPageSize(request));
        Page<Note> resultPage = noteMapper.selectPage(page, queryWrapper);

        List<NoteResponse> records = resultPage.getRecords().stream()
                .map(this::buildNoteResponse)
                .toList();

        return buildPageResponse(resultPage.getTotal(), resultPage.getCurrent(), resultPage.getSize(), records);
    }

    @Override
    public List<CommentResponse> listMyComments() {
        Long userId = LoginUserHolder.getUserId();

        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getUserId, userId);
        queryWrapper.eq(Comment::getStatus, 1);
        queryWrapper.orderByDesc(Comment::getCreatedAt);

        List<Comment> comments = commentMapper.selectList(queryWrapper);

        return comments.stream()
                .map(this::buildCommentResponse)
                .toList();
    }

    @Override
    public PageResponse<CommentResponse> pageMyComments(PageRequest request) {
        Long userId = LoginUserHolder.getUserId();

        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getUserId, userId);
        queryWrapper.eq(Comment::getStatus, 1);
        queryWrapper.orderByDesc(Comment::getCreatedAt);

        Page<Comment> page = new Page<>(getPageNum(request), getPageSize(request));
        Page<Comment> resultPage = commentMapper.selectPage(page, queryWrapper);

        List<CommentResponse> records = resultPage.getRecords().stream()
                .map(this::buildCommentResponse)
                .toList();

        PageResponse<CommentResponse> response = new PageResponse<>();
        response.setTotal(resultPage.getTotal());
        response.setPageNum(resultPage.getCurrent());
        response.setPageSize(resultPage.getSize());
        response.setRecords(records);
        return response;
    }

    @Override
    public List<NoteResponse> listMyFavoriteNotes() {
        Long userId = LoginUserHolder.getUserId();

        LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Favorite::getUserId, userId);
        queryWrapper.orderByDesc(Favorite::getCreatedAt);

        List<Favorite> favorites = favoriteMapper.selectList(queryWrapper);

        List<Long> noteIds = favorites.stream()
                .map(Favorite::getNoteId)
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
    public PageResponse<NoteResponse> pageMyFavoriteNotes(PageRequest request) {
        Long userId = LoginUserHolder.getUserId();

        LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Favorite::getUserId, userId);
        queryWrapper.orderByDesc(Favorite::getCreatedAt);

        Page<Favorite> page = new Page<>(getPageNum(request), getPageSize(request));
        Page<Favorite> resultPage = favoriteMapper.selectPage(page, queryWrapper);

        List<NoteResponse> records = buildNoteResponsesByIds(resultPage.getRecords().stream()
                .map(Favorite::getNoteId)
                .toList());

        return buildPageResponse(resultPage.getTotal(), resultPage.getCurrent(), resultPage.getSize(), records);
    }

    @Override
    public List<NoteResponse> listMyLikedNotes() {
        Long userId = LoginUserHolder.getUserId();

        LambdaQueryWrapper<NoteLike> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NoteLike::getUserId, userId);
        queryWrapper.orderByDesc(NoteLike::getCreatedAt);

        List<NoteLike> likes = noteLikeMapper.selectList(queryWrapper);

        List<Long> noteIds = likes.stream()
                .map(NoteLike::getNoteId)
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
    public PageResponse<NoteResponse> pageMyLikedNotes(PageRequest request) {
        Long userId = LoginUserHolder.getUserId();

        LambdaQueryWrapper<NoteLike> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NoteLike::getUserId, userId);
        queryWrapper.orderByDesc(NoteLike::getCreatedAt);

        Page<NoteLike> page = new Page<>(getPageNum(request), getPageSize(request));
        Page<NoteLike> resultPage = noteLikeMapper.selectPage(page, queryWrapper);

        List<NoteResponse> records = buildNoteResponsesByIds(resultPage.getRecords().stream()
                .map(NoteLike::getNoteId)
                .toList());

        return buildPageResponse(resultPage.getTotal(), resultPage.getCurrent(), resultPage.getSize(), records);
    }

    private NoteResponse buildNoteResponse(Note note) {
        User user = userMapper.selectById(note.getUserId());
        UserBriefResponse author = UserConverter.toBriefResponse(user);
        return NoteConverter.toResponse(note, author);
    }

    private CommentResponse buildCommentResponse(Comment comment) {
        User user = userMapper.selectById(comment.getUserId());
        UserBriefResponse author = UserConverter.toBriefResponse(user);
        return CommentConverter.toResponse(comment, author);
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

    private PageResponse<NoteResponse> buildPageResponse(Long total,
                                                          Long pageNum,
                                                          Long pageSize,
                                                          List<NoteResponse> records) {
        PageResponse<NoteResponse> response = new PageResponse<>();
        response.setTotal(total);
        response.setPageNum(pageNum);
        response.setPageSize(pageSize);
        response.setRecords(records);
        return response;
    }
}
