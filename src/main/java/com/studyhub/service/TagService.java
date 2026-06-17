package com.studyhub.service;

import com.studyhub.dto.NoteResponse;
import com.studyhub.dto.NoteTagBindRequest;
import com.studyhub.dto.PageRequest;
import com.studyhub.dto.PageResponse;
import com.studyhub.dto.TagCreateRequest;
import com.studyhub.entity.Tag;

import java.util.List;

public interface TagService {

    Long createTag(TagCreateRequest request);

    List<Tag> listTags();

    void bindTagToNote(NoteTagBindRequest request);

    List<Tag> listTagsByNoteId(Long noteId);

    List<NoteResponse> listNotesByTagId(Long tagId);

    PageResponse<NoteResponse> pageNotesByTagId(Long tagId, PageRequest request);

    void unbindTagFromNote(NoteTagBindRequest request);

    void deleteTag(Long tagId);
}
