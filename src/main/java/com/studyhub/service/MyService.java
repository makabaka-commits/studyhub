package com.studyhub.service;

import com.studyhub.dto.CommentResponse;
import com.studyhub.dto.NoteResponse;
import com.studyhub.dto.PageRequest;
import com.studyhub.dto.PageResponse;

import java.util.List;

public interface MyService {

    List<NoteResponse> listMyNotes();

    PageResponse<NoteResponse> pageMyNotes(PageRequest request);

    List<CommentResponse> listMyComments();

    PageResponse<CommentResponse> pageMyComments(PageRequest request);

    List<NoteResponse> listMyFavoriteNotes();

    PageResponse<NoteResponse> pageMyFavoriteNotes(PageRequest request);

    List<NoteResponse> listMyLikedNotes();

    PageResponse<NoteResponse> pageMyLikedNotes(PageRequest request);
}
