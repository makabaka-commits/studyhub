package com.studyhub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studyhub.common.ErrorCode;
import com.studyhub.dto.NotificationMessage;
import com.studyhub.dto.NotificationResponse;
import com.studyhub.dto.UserBriefResponse;
import com.studyhub.entity.Favorite;
import com.studyhub.entity.Note;
import com.studyhub.entity.NoteLike;
import com.studyhub.entity.NoteStatus;
import com.studyhub.entity.User;
import com.studyhub.exception.BusinessException;
import com.studyhub.mapper.FavoriteMapper;
import com.studyhub.mapper.NoteLikeMapper;
import com.studyhub.mapper.NoteMapper;
import com.studyhub.mapper.UserMapper;
import com.studyhub.service.InteractionService;
import org.springframework.stereotype.Service;
import com.studyhub.common.LoginUserHolder;
import com.studyhub.service.NotificationService;
import com.studyhub.converter.UserConverter;
import com.studyhub.mq.NotificationProducer;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InteractionServiceImpl implements InteractionService {

    private final NoteLikeMapper noteLikeMapper;
    private final FavoriteMapper favoriteMapper;
    private final NoteMapper noteMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;
    private final NotificationProducer notificationProducer;

    public InteractionServiceImpl(NoteLikeMapper noteLikeMapper,
                                  FavoriteMapper favoriteMapper,
                                  NoteMapper noteMapper,
                                  UserMapper userMapper,
                                  NotificationService notificationService,
                                  NotificationProducer notificationProducer
    ) {
        this.noteLikeMapper = noteLikeMapper;
        this.favoriteMapper = favoriteMapper;
        this.noteMapper = noteMapper;
        this.userMapper = userMapper;
        this.notificationService = notificationService;
        this.notificationProducer = notificationProducer;
    }

    @Override
    @Transactional
    public void likeNote(Long noteId) {
        Long userId = LoginUserHolder.getUserId();
        Note note = checkNote(noteId);
        checkUser(userId);

        LambdaQueryWrapper<NoteLike> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NoteLike::getNoteId, noteId);
        queryWrapper.eq(NoteLike::getUserId, userId);

        NoteLike existing = noteLikeMapper.selectOne(queryWrapper);
        if (existing != null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "already liked");
        }

        NoteLike noteLike = new NoteLike();
        noteLike.setNoteId(noteId);
        noteLike.setUserId(userId);
        if (noteLikeMapper.insert(noteLike) != 1 || noteMapper.incrementLikeCount(noteId) != 1) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "like note failed");
        }
        notifyNoteOwner(note, userId, "LIKE", "有人点赞了你的笔记：" + note.getTitle());
    }

    @Override
    @Transactional
    public void unlikeNote(Long noteId) {
        Long userId = LoginUserHolder.getUserId();
        Note note = checkNote(noteId);
        checkUser(userId);

        LambdaQueryWrapper<NoteLike> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NoteLike::getNoteId, noteId);
        queryWrapper.eq(NoteLike::getUserId, userId);

        NoteLike existing = noteLikeMapper.selectOne(queryWrapper);
        if (existing == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "not liked yet");
        }

        if (noteLikeMapper.deleteById(existing.getId()) != 1 || noteMapper.decrementLikeCount(noteId) != 1) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "unlike note failed");
        }
    }

    @Override
    @Transactional
    public void favoriteNote(Long noteId) {
        Long userId = LoginUserHolder.getUserId();
        Note note = checkNote(noteId);
        checkUser(userId);

        LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Favorite::getNoteId, noteId);
        queryWrapper.eq(Favorite::getUserId, userId);

        Favorite existing = favoriteMapper.selectOne(queryWrapper);
        if (existing != null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "already favorited");
        }

        Favorite favorite = new Favorite();
        favorite.setNoteId(noteId);
        favorite.setUserId(userId);
        if (favoriteMapper.insert(favorite) != 1 || noteMapper.incrementFavoriteCount(noteId) != 1) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "favorite note failed");
        }
        notifyNoteOwner(note, userId, "FAVORITE", "有人收藏了你的笔记：" + note.getTitle());
    }

    @Override
    @Transactional
    public void unfavoriteNote(Long noteId) {
        Long userId = LoginUserHolder.getUserId();
        Note note = checkNote(noteId);
        checkUser(userId);

        LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Favorite::getNoteId, noteId);
        queryWrapper.eq(Favorite::getUserId, userId);

        Favorite existing = favoriteMapper.selectOne(queryWrapper);
        if (existing == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "not favorited yet");
        }

        if (favoriteMapper.deleteById(existing.getId()) != 1 || noteMapper.decrementFavoriteCount(noteId) != 1) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "unfavorite note failed");
        }
    }

    private void notifyNoteOwner(Note note, Long senderId, String type, String content) {
        if (note.getUserId().equals(senderId)) {
            return;
        }
        notificationProducer.sendNotification(new NotificationMessage(
                note.getUserId(), senderId, note.getId(), type, content));
        pushNotification(note, senderId, type, content);
    }

    /**
     * 构建通知响应并通过 WebSocket 推送
     */
    private void pushNotification(Note note, Long senderId, String type, String content) {
        // 不给自己推送
        Long currentUserId = LoginUserHolder.getUserId();
        if (note.getUserId().equals(currentUserId)) {
            return;
        }

        User sender = userMapper.selectById(senderId);
        UserBriefResponse senderResponse = UserConverter.toBriefResponse(sender);

        NotificationResponse notificationResponse = new NotificationResponse();
        notificationResponse.setType(type);
        notificationResponse.setContent(content);
        notificationResponse.setNoteId(note.getId());
        notificationResponse.setNoteTitle(note.getTitle());
        notificationResponse.setSender(senderResponse);
        notificationResponse.setReadStatus(0);

        notificationService.pushNotification(note.getUserId(), notificationResponse);
    }

    private Note checkNote(Long noteId) {
        Note note = noteMapper.selectById(noteId);
        if (note == null || !NoteStatus.APPROVED.equals(note.getStatus())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "note not found");
        }
        return note;
    }

    private void checkUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || !Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "user not found");
        }
    }
}
