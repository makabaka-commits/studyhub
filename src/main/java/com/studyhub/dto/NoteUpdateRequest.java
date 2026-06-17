package com.studyhub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class NoteUpdateRequest {

    @NotNull(message = "id cannot be null")
    private Long id;

    @NotBlank(message = "title cannot be blank")
    @Size(max = 100, message = "title length cannot exceed 100")
    private String title;

    @NotBlank(message = "content cannot be blank")
    private String content;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
