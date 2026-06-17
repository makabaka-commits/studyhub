package com.studyhub.converter;

import com.studyhub.dto.CommentResponse;
import com.studyhub.dto.UserBriefResponse;
import com.studyhub.entity.Comment;

public class CommentConverter {

    public static CommentResponse toResponse(Comment comment, UserBriefResponse author) {
        if (comment == null) {
            return null;
        }

        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setNoteId(comment.getNoteId());
        response.setContent(comment.getContent());
        response.setCreatedAt(comment.getCreatedAt());
        response.setAuthor(author);
        return response;
    }
}
