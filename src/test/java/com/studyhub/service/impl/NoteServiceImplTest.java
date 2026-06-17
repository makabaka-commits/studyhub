package com.studyhub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.studyhub.common.LoginUserHolder;
import com.studyhub.dto.*;
import com.studyhub.entity.Note;
import com.studyhub.entity.User;
import com.studyhub.exception.BusinessException;
import com.studyhub.mapper.NoteMapper;
import com.studyhub.mapper.UserMapper;
import com.studyhub.service.AiSummaryService;
import com.studyhub.service.BrowseHistoryService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoteServiceImplTest {

    @Mock
    private NoteMapper noteMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private AiSummaryService aiSummaryService;

    @Mock
    private BrowseHistoryService browseHistoryService;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    @InjectMocks
    private NoteServiceImpl noteService;

    private Note testNote;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setNickname("Test");

        testNote = new Note();
        testNote.setId(1L);
        testNote.setUserId(1L);
        testNote.setTitle("Test Note");
        testNote.setContent("This is test content");
        testNote.setStatus(1);
        testNote.setViewCount(0);
        testNote.setLikeCount(0);
        testNote.setFavoriteCount(0);

        when(stringRedisTemplate.opsForZSet()).thenReturn(zSetOperations);
    }

    @AfterEach
    void tearDown() {
        LoginUserHolder.clear();
    }

    @Test
    void createNote_shouldSucceed() {
        LoginUserHolder.setUserId(1L);

        NoteCreateRequest request = new NoteCreateRequest();
        request.setTitle("New Note");
        request.setContent("New content");

        Long noteId = noteService.createNote(request);

        assertNotNull(noteId);
        verify(noteMapper).insert(any(Note.class));
    }

    @Test
    void getNoteById_shouldSucceed() {
        when(noteMapper.selectById(1L)).thenReturn(testNote);
        when(userMapper.selectById(1L)).thenReturn(testUser);

        NoteResponse response = noteService.getNoteById(1L);

        assertNotNull(response);
        assertEquals("Test Note", response.getTitle());
        verify(noteMapper).updateById(any(Note.class));
    }

    @Test
    void getNoteById_shouldThrow_whenNoteNotFound() {
        when(noteMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> noteService.getNoteById(999L));
    }

    @Test
    void deleteNote_shouldSucceed_whenOwner() {
        LoginUserHolder.setUserId(1L);
        when(noteMapper.selectById(1L)).thenReturn(testNote);

        assertDoesNotThrow(() -> noteService.deleteNote(1L));

        verify(noteMapper).updateById(any(Note.class));
    }

    @Test
    void deleteNote_shouldThrow_whenNotOwner() {
        LoginUserHolder.setUserId(2L);
        when(noteMapper.selectById(1L)).thenReturn(testNote);

        assertThrows(BusinessException.class, () -> noteService.deleteNote(1L));
    }

    @Test
    void pageNotes_shouldReturnResults() {
        NotePageRequest request = new NotePageRequest();
        request.setPageNum(1);
        request.setPageSize(10);
        request.setKeyword("test");

        Page<Note> pageResult = new Page<>(1, 10, 1);
        pageResult.setRecords(List.of(testNote));

        when(noteMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(pageResult);
        when(userMapper.selectById(1L)).thenReturn(testUser);

        PageResponse<NoteResponse> response = noteService.pageNotes(request);

        assertNotNull(response);
        assertEquals(1, response.getTotal());
        assertEquals(1, response.getRecords().size());
        assertEquals("Test Note", response.getRecords().get(0).getTitle());
    }
}
