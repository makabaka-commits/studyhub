package com.studyhub.service.impl;

import com.studyhub.common.LoginUserHolder;
import com.studyhub.entity.Note;
import com.studyhub.entity.NoteLike;
import com.studyhub.entity.NoteStatus;
import com.studyhub.entity.User;
import com.studyhub.exception.BusinessException;
import com.studyhub.mapper.FavoriteMapper;
import com.studyhub.mapper.NoteLikeMapper;
import com.studyhub.mapper.NoteMapper;
import com.studyhub.mapper.UserMapper;
import com.studyhub.mq.NotificationProducer;
import com.studyhub.service.NotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InteractionServiceImplTest {

    @Mock private NoteLikeMapper noteLikeMapper;
    @Mock private FavoriteMapper favoriteMapper;
    @Mock private NoteMapper noteMapper;
    @Mock private UserMapper userMapper;
    @Mock private NotificationService notificationService;
    @Mock private NotificationProducer notificationProducer;

    @InjectMocks private InteractionServiceImpl interactionService;

    private Note note;

    @BeforeEach
    void setUp() {
        LoginUserHolder.setUserId(2L);
        note = new Note();
        note.setId(10L);
        note.setUserId(1L);
        note.setTitle("并发测试笔记");
        note.setStatus(NoteStatus.APPROVED);

        User actor = new User();
        actor.setId(2L);
        actor.setNickname("actor");
        actor.setStatus(1);
        lenient().when(noteMapper.selectById(10L)).thenReturn(note);
        lenient().when(userMapper.selectById(2L)).thenReturn(actor);
    }

    @AfterEach
    void tearDown() {
        LoginUserHolder.clear();
    }

    @Test
    void likeNote_shouldInsertAndIncrementAtomically() {
        when(noteLikeMapper.selectOne(any())).thenReturn(null);
        when(noteLikeMapper.insert(any(NoteLike.class))).thenReturn(1);
        when(noteMapper.incrementLikeCount(10L)).thenReturn(1);

        interactionService.likeNote(10L);

        verify(noteLikeMapper).insert(any(NoteLike.class));
        verify(noteMapper).incrementLikeCount(10L);
        verify(notificationProducer).sendNotification(any());
        verify(notificationService).pushNotification(eq(1L), any());
    }

    @Test
    void unlikeNote_shouldDeleteAndDecrementAtomically() {
        NoteLike existing = new NoteLike();
        existing.setId(5L);
        when(noteLikeMapper.selectOne(any())).thenReturn(existing);
        when(noteLikeMapper.deleteById(5L)).thenReturn(1);
        when(noteMapper.decrementLikeCount(10L)).thenReturn(1);

        interactionService.unlikeNote(10L);

        verify(noteLikeMapper).deleteById(5L);
        verify(noteMapper).decrementLikeCount(10L);
    }

    @Test
    void likeNote_shouldRejectPendingNote() {
        note.setStatus(NoteStatus.PENDING);

        assertThrows(BusinessException.class, () -> interactionService.likeNote(10L));
        verifyNoInteractions(noteLikeMapper);
    }
}
