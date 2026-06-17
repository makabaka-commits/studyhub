package com.studyhub.dto;

import java.io.Serializable;

/**
 * 通知消息 DTO
 * 通过 RabbitMQ 异步发送的通知数据
 */
public class NotificationMessage implements Serializable {

    private Long receiverId;
    private Long senderId;
    private Long noteId;
    private String type;
    private String content;

    public NotificationMessage() {
    }

    public NotificationMessage(Long receiverId, Long senderId, Long noteId, String type, String content) {
        this.receiverId = receiverId;
        this.senderId = senderId;
        this.noteId = noteId;
        this.type = type;
        this.content = content;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public Long getNoteId() {
        return noteId;
    }

    public void setNoteId(Long noteId) {
        this.noteId = noteId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}