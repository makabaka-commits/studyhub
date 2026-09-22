package com.studyhub.service.impl;

import com.studyhub.common.LoginUserHolder;
import com.studyhub.dto.*;
import com.studyhub.entity.User;
import com.studyhub.exception.BusinessException;
import com.studyhub.mapper.UserMapper;
import com.studyhub.common.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("$2a$10$encodedpassword");
        testUser.setNickname("Test");
        testUser.setAvatar("avatar.jpg");
        testUser.setEmail("test@example.com");
        testUser.setStatus(1);
        testUser.setRole("USER");
    }

    @AfterEach
    void tearDown() {
        LoginUserHolder.clear();
    }

    @Test
    void register_shouldSucceed_whenUsernameNotExists() {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setUsername("newuser");
        request.setPassword("password123");
        request.setNickname("NewUser");

        when(userMapper.selectOne(any())).thenReturn(null);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encoded");
        when(userMapper.insert(any(User.class))).thenReturn(1);

        assertDoesNotThrow(() -> userService.register(request));

        verify(userMapper).insert(any(User.class));
    }

    @Test
    void register_shouldThrow_whenUsernameExists() {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setUsername("existing");
        request.setPassword("password123");

        when(userMapper.selectOne(any())).thenReturn(new User());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.register(request));
        assertEquals("username already exists", exception.getMessage());
    }

    @Test
    void login_shouldSucceed_whenCredentialsCorrect() {
        UserLoginRequest request = new UserLoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        when(userMapper.selectOne(any())).thenReturn(testUser);
        when(passwordEncoder.matches("password123", testUser.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken(1L, "testuser")).thenReturn("jwt-token");

        UserLoginResponse response = userService.login(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("testuser", response.getUsername());
        assertEquals("Test", response.getNickname());
        assertNotNull(response.getToken());
    }

    @Test
    void login_shouldThrow_whenUserNotFound() {
        UserLoginRequest request = new UserLoginRequest();
        request.setUsername("nonexistent");
        request.setPassword("password123");

        when(userMapper.selectOne(any())).thenReturn(null);

        assertThrows(BusinessException.class, () -> userService.login(request));
    }

    @Test
    void login_shouldThrow_whenPasswordWrong() {
        UserLoginRequest request = new UserLoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrongpassword");

        when(userMapper.selectOne(any())).thenReturn(testUser);
        when(passwordEncoder.matches("wrongpassword", testUser.getPassword())).thenReturn(false);

        assertThrows(BusinessException.class, () -> userService.login(request));
    }

    @Test
    void login_shouldThrow_whenUserDisabled() {
        UserLoginRequest request = new UserLoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        testUser.setStatus(0);
        when(userMapper.selectOne(any())).thenReturn(testUser);

        assertThrows(BusinessException.class, () -> userService.login(request));
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void getCurrentUser_shouldSucceed_whenLoggedIn() {
        LoginUserHolder.setUserId(1L);
        when(userMapper.selectById(1L)).thenReturn(testUser);

        CurrentUserResponse response = userService.getCurrentUser();

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("testuser", response.getUsername());
        assertEquals("Test", response.getNickname());
        assertEquals("test@example.com", response.getEmail());
    }

    @Test
    void getCurrentUser_shouldThrow_whenNotLoggedIn() {
        LoginUserHolder.clear();

        assertThrows(BusinessException.class, () -> userService.getCurrentUser());
    }

    @Test
    void updateProfile_shouldSucceed() {
        LoginUserHolder.setUserId(1L);
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        UserUpdateRequest request = new UserUpdateRequest();
        request.setNickname("NewNickname");
        request.setEmail("new@example.com");

        assertDoesNotThrow(() -> userService.updateProfile(request));

        verify(userMapper).updateById(any(User.class));
    }

    @Test
    void updatePassword_shouldSucceed() {
        LoginUserHolder.setUserId(1L);
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(passwordEncoder.matches("oldPass123", testUser.getPassword())).thenReturn(true);
        when(passwordEncoder.encode("newPass456")).thenReturn("$2a$10$newencoded");
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        PasswordUpdateRequest request = new PasswordUpdateRequest();
        request.setOldPassword("oldPass123");
        request.setNewPassword("newPass456");

        assertDoesNotThrow(() -> userService.updatePassword(request));

        verify(userMapper).updateById(any(User.class));
    }
}
