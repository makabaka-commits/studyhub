package com.studyhub.service;

import com.studyhub.dto.CommentCreateRequest;
import com.studyhub.dto.CommentResponse;
import com.studyhub.dto.PageRequest;
import com.studyhub.dto.PageResponse;

import java.util.List;

public interface CommentService {

    Long createComment(CommentCreateRequest request);

    List<CommentResponse> listCommentsByNoteId(Long noteId);

    PageResponse<CommentResponse> pageCommentsByNoteId(Long noteId, PageRequest request);

    void deleteComment(Long id);
}
