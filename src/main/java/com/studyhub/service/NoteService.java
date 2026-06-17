package com.studyhub.service;

import com.studyhub.dto.DashboardResponse;
import com.studyhub.dto.NoteCreateRequest;
import com.studyhub.dto.NotePageRequest;
import com.studyhub.dto.NoteResponse;
import com.studyhub.dto.NoteUpdateRequest;
import com.studyhub.dto.PageResponse;

import java.util.List;

public interface NoteService {

    Long createNote(NoteCreateRequest request);

    NoteResponse getNoteById(Long id);

    List<NoteResponse> listNotes();

    PageResponse<NoteResponse> pageNotes(NotePageRequest request);

    List<NoteResponse> listHotNotes();

    String generateSummary(Long noteId);

    void updateNote(NoteUpdateRequest request);

    void deleteNote(Long id);

    List<NoteResponse> recommendNotes(Long noteId, int limit);

    String askNote(Long noteId, String question);

    void approveNote(Long noteId);

    void rejectNote(Long noteId);

    PageResponse<NoteResponse> pagePendingNotes(NotePageRequest request);

    DashboardResponse getDashboard();
}
