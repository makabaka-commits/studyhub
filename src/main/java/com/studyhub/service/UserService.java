package com.studyhub.service;

import com.studyhub.dto.*;

import java.util.List;

public interface UserService {

    void register(UserRegisterRequest request);

    UserLoginResponse login(UserLoginRequest request);

    CurrentUserResponse getCurrentUser();

    void updateProfile(UserUpdateRequest request);

    void updatePassword(PasswordUpdateRequest request);

    List<UserBriefResponse> listAllUsers();

    void banUser(Long userId);

    void unbanUser(Long userId);
}
