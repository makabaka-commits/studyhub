package com.studyhub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class NoteAskRequest {
    @NotNull(message = "noteId cannot be null")
    private Long noteId;

    @NotBlank(message = "question cannot be blank")
    private String question;

    public Long getNoteId() { return noteId; }
    public void setNoteId(Long noteId) { this.noteId = noteId; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
}
