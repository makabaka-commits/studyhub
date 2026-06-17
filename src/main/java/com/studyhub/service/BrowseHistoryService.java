package com.studyhub.service;

import com.studyhub.dto.NoteResponse;
import com.studyhub.dto.PageRequest;
import com.studyhub.dto.PageResponse;

import java.util.List;

public interface BrowseHistoryService {

    void recordBrowse(Long noteId);

    List<NoteResponse> listMyBrowseHistory();

    PageResponse<NoteResponse> pageMyBrowseHistory(PageRequest request);

    void clearMyBrowseHistory();
}
