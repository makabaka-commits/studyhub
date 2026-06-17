package com.studyhub.dto;

import java.time.LocalDateTime;

public class NotificationResponse {

    private Long id;

    private Long noteId;

    private String noteTitle;

    private String type;

    private String content;

    private Integer readStatus;

    private LocalDateTime createdAt;

    private UserBriefResponse sender;

    public Long getId() {
        return id;
    }

    public Long getNoteId() {
        return noteId;
    }

    public String getNoteTitle() {
        return noteTitle;
    }

    public String getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public Integer getReadStatus() {
        return readStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public UserBriefResponse getSender() {
        return sender;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNoteId(Long noteId) {
        this.noteId = noteId;
    }

    public void setNoteTitle(String noteTitle) {
        this.noteTitle = noteTitle;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setReadStatus(Integer readStatus) {
        this.readStatus = readStatus;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setSender(UserBriefResponse sender) {
        this.sender = sender;
    }
}
