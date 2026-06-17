package com.studyhub.dto;

import jakarta.validation.constraints.NotNull;

public class NoteTagBindRequest {

    @NotNull(message = "noteId cannot be null")
    private Long noteId;

    @NotNull(message = "tagId cannot be null")
    private Long tagId;

    public Long getNoteId() {
        return noteId;
    }

    public Long getTagId() {
        return tagId;
    }

    public void setNoteId(Long noteId) {
        this.noteId = noteId;
    }

    public void setTagId(Long tagId) {
        this.tagId = tagId;
    }
}