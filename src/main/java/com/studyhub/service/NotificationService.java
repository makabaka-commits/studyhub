package com.studyhub.service;

import com.studyhub.dto.NotificationResponse;
import com.studyhub.dto.PageRequest;
import com.studyhub.dto.PageResponse;

import java.util.List;

public interface NotificationService {

    void createNotification(Long receiverId, Long senderId, Long noteId, String type, String content);

    List<NotificationResponse> listMyNotifications();

    PageResponse<NotificationResponse> pageMyNotifications(PageRequest request);

    Long countUnread();


    void markAsRead(Long notificationId);

    void markAllAsRead();

    void pushNotification(Long receiverId, NotificationResponse notification);
}
