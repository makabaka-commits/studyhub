package com.studyhub.service;

public interface InteractionService {

    void likeNote(Long noteId);

    void unlikeNote(Long noteId);

    void favoriteNote(Long noteId);

    void unfavoriteNote(Long noteId);
}