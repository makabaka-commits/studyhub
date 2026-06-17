package com.studyhub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.studyhub.common.ErrorCode;
import com.studyhub.dto.*;
import com.studyhub.entity.Comment;
import com.studyhub.entity.Note;
import com.studyhub.entity.User;
import com.studyhub.exception.BusinessException;
import com.studyhub.mapper.CommentMapper;
import com.studyhub.mapper.NoteMapper;
import com.studyhub.mapper.UserMapper;
import com.studyhub.mq.NotificationProducer;
import com.studyhub.service.CommentService;
import org.springframework.stereotype.Service;
import com.studyhub.common.LoginUserHolder;
import com.studyhub.common.LoginUserHolder;
import com.studyhub.converter.CommentConverter;
import com.studyhub.converter.UserConverter;
import com.studyhub.service.NotificationService;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final NoteMapper noteMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;
    private final NotificationProducer notificationProducer;
    private CommentResponse buildCommentResponse(Comment comment) {
        User user = userMapper.selectById(comment.getUserId());
        UserBriefResponse author = UserConverter.toBriefResponse(user);
        return CommentConverter.toResponse(comment, author);
    }

    public CommentServiceImpl(CommentMapper commentMapper, NoteMapper noteMapper, UserMapper userMapper,NotificationService notificationService,NotificationProducer notificationProducer) {
        this.commentMapper = commentMapper;
        this.noteMapper = noteMapper;
        this.userMapper = userMapper;
        this.notificationService = notificationService;
        this.notificationProducer = notificationProducer;
    }

    @Override
    public Long createComment(CommentCreateRequest request) {
        Note note = noteMapper.selectById(request.getNoteId());
        if (note == null || Integer.valueOf(0).equals(note.getStatus())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "note not found");
        }

        Long userId = LoginUserHolder.getUserId();

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "user not found");
        }

        Comment comment = new Comment();
        comment.setNoteId(request.getNoteId());
        comment.setUserId(userId);
        comment.setContent(request.getContent());
        comment.setStatus(1);

        int rows = commentMapper.insert(comment);

        if (rows != 1) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "create comment failed");
        }
        notificationProducer.sendNotification(new NotificationMessage(
                note.getUserId(),
                userId,
                note.getId(),
                "COMMENT",
                "有人评论了你的笔记：" + note.getTitle()
        ));
        return comment.getId();
    }

    @Override
    public List<CommentResponse> listCommentsByNoteId(Long noteId) {
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getNoteId, noteId);
        queryWrapper.eq(Comment::getStatus, 1);
        queryWrapper.orderByDesc(Comment::getCreatedAt);

        List<Comment> comments = commentMapper.selectList(queryWrapper);
        return comments.stream()
                .map(this::buildCommentResponse)
                .toList();
    }

    @Override
    public PageResponse<CommentResponse> pageCommentsByNoteId(Long noteId, PageRequest request) {
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getNoteId, noteId);
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
    public void deleteComment(Long id) {
        Long userId = LoginUserHolder.getUserId();

        Comment comment = commentMapper.selectById(id);
        if (comment == null || Integer.valueOf(0).equals(comment.getStatus())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "comment not found");
        }

        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "no permission");
        }

        comment.setStatus(0);
        commentMapper.updateById(comment);
    }

    private int getPageNum(PageRequest request) {
        return request.getPageNum() == null || request.getPageNum() < 1 ? 1 : request.getPageNum();
    }

    private int getPageSize(PageRequest request) {
        int pageSize = request.getPageSize() == null || request.getPageSize() < 1 ? 10 : request.getPageSize();
        return Math.min(pageSize, 50);
    }
}
