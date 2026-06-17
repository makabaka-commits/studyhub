package com.studyhub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

        return notifications.stream()
                .map(this::buildNotificationResponse)
                .toList();
    }

    @Override
    public PageResponse<NotificationResponse> pageMyNotifications(PageRequest request) {
        Long userId = LoginUserHolder.getUserId();

        LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Notification::getReceiverId, userId);
        queryWrapper.orderByDesc(Notification::getCreatedAt);

        Page<Notification> page = new Page<>(getPageNum(request), getPageSize(request));
        Page<Notification> resultPage = notificationMapper.selectPage(page, queryWrapper);

        List<NotificationResponse> records = resultPage.getRecords().stream()
                .map(this::buildNotificationResponse)
                .toList();

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

        LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Notification::getReceiverId, userId);
        queryWrapper.eq(Notification::getReadStatus, 0);

        List<Notification> notifications = notificationMapper.selectList(queryWrapper);
        for (Notification notification : notifications) {
            notification.setReadStatus(1);
            notificationMapper.updateById(notification);
        }
    }
    private NotificationResponse buildNotificationResponse(Notification notification) {
        User sender = userMapper.selectById(notification.getSenderId());
        UserBriefResponse senderResponse = UserConverter.toBriefResponse(sender);

        String noteTitle = null;
        if (notification.getNoteId() != null) {
            Note note = noteMapper.selectById(notification.getNoteId());
            if (note != null) {
                noteTitle = note.getTitle();
            }
        }

        return NotificationConverter.toResponse(notification, senderResponse, noteTitle);
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
