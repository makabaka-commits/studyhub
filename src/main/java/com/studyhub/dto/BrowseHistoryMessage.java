package com.studyhub.dto;

import java.io.Serializable;

/**
 * 浏览历史消息 DTO
 * 通过 RabbitMQ 异步写入的浏览记录
 */
public class BrowseHistoryMessage implements Serializable {

    private Long userId;
    private Long noteId;

    public BrowseHistoryMessage() {
    }

    public BrowseHistoryMessage(Long userId, Long noteId) {
        this.userId = userId;
        this.noteId = noteId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getNoteId() {
        return noteId;
    }

    public void setNoteId(Long noteId) {
        this.noteId = noteId;
    }
}