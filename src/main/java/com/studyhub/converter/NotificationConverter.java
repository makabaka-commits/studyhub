package com.studyhub.converter;

import com.studyhub.dto.NotificationResponse;
import com.studyhub.dto.UserBriefResponse;
import com.studyhub.entity.Notification;

public class NotificationConverter {

    public static NotificationResponse toResponse(Notification notification,
                                                  UserBriefResponse sender,
                                                  String noteTitle) {
        if (notification == null) {
            return null;
        }

        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setNoteId(notification.getNoteId());
        response.setNoteTitle(noteTitle);
        response.setType(notification.getType());
        response.setContent(notification.getContent());
        response.setReadStatus(notification.getReadStatus());
        response.setCreatedAt(notification.getCreatedAt());
        response.setSender(sender);
        return response;
    }
}