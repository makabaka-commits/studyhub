package com.studyhub.service.impl;

import com.studyhub.mapper.BrowseHistoryMapper;
import com.studyhub.mapper.NoteMapper;
import com.studyhub.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BrowseHistoryServiceImplTest {

    @Mock private BrowseHistoryMapper browseHistoryMapper;
    @Mock private NoteMapper noteMapper;
    @Mock private UserMapper userMapper;

    @InjectMocks private BrowseHistoryServiceImpl browseHistoryService;

    @Test
    void recordBrowse_usesUserIdFromMessageWithoutRequestContext() {
        browseHistoryService.recordBrowse(7L, 11L);

        verify(browseHistoryMapper).upsert(7L, 11L);
    }
}
