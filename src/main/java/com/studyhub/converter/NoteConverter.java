package com.studyhub.converter;

import com.studyhub.common.MarkdownUtil;
import com.studyhub.dto.NoteResponse;
import com.studyhub.dto.UserBriefResponse;
import com.studyhub.entity.Note;

public class NoteConverter {

    public static NoteResponse toResponse(Note note, UserBriefResponse author) {
        if (note == null) {
            return null;
        }

        NoteResponse response = new NoteResponse();
        response.setId(note.getId());
        response.setTitle(note.getTitle());
        response.setContent(note.getContent());
        response.setContentHtml(MarkdownUtil.renderToHtml(note.getContent()));
        response.setSummary(note.getSummary());
        response.setViewCount(note.getViewCount());
        response.setLikeCount(note.getLikeCount());
        response.setFavoriteCount(note.getFavoriteCount());
        response.setCreatedAt(note.getCreatedAt());
        response.setUpdatedAt(note.getUpdatedAt());
        response.setAuthor(author);
        return response;
    }
}
