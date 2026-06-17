package com.studyhub.dto;

import java.time.LocalDateTime;

public class CommentResponse {

    private Long id;

    private Long noteId;

    private String content;

    private LocalDateTime createdAt;

    private UserBriefResponse author;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getNoteId() {
        return noteId;
    }

    public void setNoteId(Long noteId) {
        this.noteId = noteId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public UserBriefResponse getAuthor() {
        return author;
    }

    public void setAuthor(UserBriefResponse author) {
        this.author = author;
    }
}
