package com.studyhub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.studyhub.common.ErrorCode;
import com.studyhub.common.LoginUserHolder;
import com.studyhub.entity.Notification;
import com.studyhub.exception.BusinessException;
import com.studyhub.mapper.NotificationMapper;
import com.studyhub.service.NotificationService;
import org.springframework.stereotype.Service;
import com.studyhub.converter.NotificationConverter;
import com.studyhub.converter.UserConverter;
import com.studyhub.dto.NotificationResponse;
import com.studyhub.dto.PageRequest;
import com.studyhub.dto.PageResponse;
import com.studyhub.dto.UserBriefResponse;
import com.studyhub.entity.Note;
import com.studyhub.entity.User;
import com.studyhub.mapper.NoteMapper;
import com.studyhub.mapper.UserMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;
    private final NoteMapper noteMapper;
    private final SimpMessagingTemplate messagingTemplate;


    public NotificationServiceImpl(NotificationMapper notificationMapper,
                                   UserMapper userMapper,
                                   NoteMapper noteMapper,
                                   SimpMessagingTemplate messagingTemplate) {
        this.notificationMapper = notificationMapper;
        this.userMapper = userMapper;
        this.noteMapper = noteMapper;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void createNotification(Long receiverId, Long senderId, Long noteId, String type, String content) {
        if (receiverId.equals(senderId)) {
            return;
        }

        Notification notification = new Notification();
        notification.setReceiverId(receiverId);
        notification.setSenderId(senderId);
        notification.setNoteId(noteId);
        notification.setType(type);
        notification.setContent(content);
        notification.setReadStatus(0);

        notificationMapper.insert(notification);
    }

    @Override
    public List<NotificationResponse> listMyNotifications() {
        Long userId = LoginUserHolder.getUserId();

        LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Notification::getReceiverId, userId);
        queryWrapper.orderByDesc(Notification::getCreatedAt);

        List<Notification> notifications = notificationMapper.selectList(queryWrapper);

        return buildNotificationResponses(notifications);
    }

    @Override
    public PageResponse<NotificationResponse> pageMyNotifications(PageRequest request) {
        Long userId = LoginUserHolder.getUserId();

        LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Notification::getReceiverId, userId);
        queryWrapper.orderByDesc(Notification::getCreatedAt);

        Page<Notification> page = new Page<>(getPageNum(request), getPageSize(request));
        Page<Notification> resultPage = notificationMapper.selectPage(page, queryWrapper);

        List<NotificationResponse> records = buildNotificationResponses(resultPage.getRecords());

        return buildPageResponse(resultPage.getTotal(), resultPage.getCurrent(), resultPage.getSize(), records);
    }

    @Override
    public void markAsRead(Long notificationId) {
        Long userId = LoginUserHolder.getUserId();

        Notification notification = notificationMapper.selectById(notificationId);
        if (notification == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "notification not found");
        }

        if (!notification.getReceiverId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "no permission");
        }

        notification.setReadStatus(1);
        notificationMapper.updateById(notification);
    }

    @Override
    public void markAllAsRead() {
        Long userId = LoginUserHolder.getUserId();

        LambdaUpdateWrapper<Notification> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Notification::getReceiverId, userId)
                .eq(Notification::getReadStatus, 0)
                .set(Notification::getReadStatus, 1);
        notificationMapper.update(null, updateWrapper);
    }

    private List<NotificationResponse> buildNotificationResponses(List<Notification> notifications) {
        if (notifications.isEmpty()) {
            return List.of();
        }
        List<Long> senderIds = notifications.stream().map(Notification::getSenderId).distinct().toList();
        Map<Long, User> senders = userMapper.selectBatchIds(senderIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        List<Long> noteIds = notifications.stream().map(Notification::getNoteId)
                .filter(id -> id != null).distinct().toList();
        Map<Long, Note> notes = noteIds.isEmpty() ? Map.of() : noteMapper.selectBatchIds(noteIds).stream()
                .collect(Collectors.toMap(Note::getId, Function.identity()));

        return notifications.stream().map(notification -> {
            UserBriefResponse sender = UserConverter.toBriefResponse(senders.get(notification.getSenderId()));
            Note note = notes.get(notification.getNoteId());
            return NotificationConverter.toResponse(notification, sender, note == null ? null : note.getTitle());
        }).toList();
    }
    @Override
    public Long countUnread() {
        Long userId = LoginUserHolder.getUserId();

        LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Notification::getReceiverId, userId);
        queryWrapper.eq(Notification::getReadStatus, 0);

        return notificationMapper.selectCount(queryWrapper);
    }

    private int getPageNum(PageRequest request) {
        return request.getPageNum() == null || request.getPageNum() < 1 ? 1 : request.getPageNum();
    }

    private int getPageSize(PageRequest request) {
        int pageSize = request.getPageSize() == null || request.getPageSize() < 1 ? 10 : request.getPageSize();
        return Math.min(pageSize, 50);
    }

    private PageResponse<NotificationResponse> buildPageResponse(Long total,
                                                                  Long pageNum,
                                                                  Long pageSize,
                                                                  List<NotificationResponse> records) {
        PageResponse<NotificationResponse> response = new PageResponse<>();
        response.setTotal(total);
        response.setPageNum(pageNum);
        response.setPageSize(pageSize);
        response.setRecords(records);
        return response;
    }
    @Override
    public void pushNotification(Long receiverId, NotificationResponse notification) {
        // convertAndSendToUser 会发送到 /user/{receiverId}/topic/notifications
        // 客户端订阅 /user/topic/notifications 即可收到（Spring 自动替换 /user 为具体用户前缀）
        messagingTemplate.convertAndSendToUser(
                String.valueOf(receiverId),
                "/topic/notifications",
                notification
        );
    }
}
