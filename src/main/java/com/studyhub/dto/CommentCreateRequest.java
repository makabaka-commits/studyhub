package com.studyhub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CommentCreateRequest {

    @NotNull(message = "noteId cannot be null")
    private Long noteId;


    @NotBlank(message = "content cannot be blank")
    @Size(max = 1000, message = "content length cannot exceed 1000")
    private String content;

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

    // 必须生成 getter 和 setter
}