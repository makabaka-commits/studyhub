package com.studyhub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studyhub.common.ErrorCode;
import com.studyhub.converter.UserConverter;
import com.studyhub.dto.*;
import com.studyhub.entity.User;
import com.studyhub.exception.BusinessException;
import com.studyhub.mapper.UserMapper;
import com.studyhub.service.UserService;
import org.springframework.stereotype.Service;
import com.studyhub.common.JwtUtil;
import com.studyhub.common.LoginUserHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }
    @Override
    public void register(UserRegisterRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();
        String nickname = request.getNickname();

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);

        User existingUser = userMapper.selectOne(queryWrapper);
        if (existingUser != null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "username already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname);
        user.setStatus(1);
        user.setRole("USER");

        int rows = userMapper.insert(user);
        if (rows != 1) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "register failed");
        }
    }

    @Override
    public UserLoginResponse login(UserLoginRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);

        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "username or password error");
        }

        String storedPassword = user.getPassword();

        boolean passwordMatched;
        if (storedPassword != null && storedPassword.startsWith("$2")) {
            passwordMatched = passwordEncoder.matches(password, storedPassword);
        } else {
            passwordMatched = storedPassword != null && storedPassword.equals(password);

            if (passwordMatched) {
                user.setPassword(passwordEncoder.encode(password));
                userMapper.updateById(user);
            }
        }

        if (!passwordMatched) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "username or password error");
        }

        String token = JwtUtil.generateToken(user.getId(), user.getUsername());

        UserLoginResponse response = new UserLoginResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setAvatar(user.getAvatar());
        response.setToken(token);
        response.setRole(user.getRole());
        return response;
    }

    @Override
    public CurrentUserResponse getCurrentUser() {
        Long userId = LoginUserHolder.getUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "please login");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "user not found");
        }

        CurrentUserResponse response = new CurrentUserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setAvatar(user.getAvatar());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());

        return response;
    }

    @Override
    public void updateProfile(UserUpdateRequest request) {
        Long userId = LoginUserHolder.getUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "please login");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "user not found");
        }

        String nickname = request.getNickname();
        String email = request.getEmail();

        if (nickname != null && !nickname.isBlank()) {
            user.setNickname(nickname.trim());
        }
        if (email != null && !email.isBlank()) {
            user.setEmail(email.trim());
        }

        int rows = userMapper.updateById(user);
        if (rows != 1) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "update profile failed");
        }
    }

    @Override
    public void updatePassword(PasswordUpdateRequest request) {
        Long userId = LoginUserHolder.getUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "please login");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "user not found");
        }

        String oldPassword = request.getOldPassword();
        String newPassword = request.getNewPassword();

        String storedPassword = user.getPassword();
        boolean passwordMatched;
        if (storedPassword != null && storedPassword.startsWith("$2")) {
            passwordMatched = passwordEncoder.matches(oldPassword, storedPassword);
        } else {
            passwordMatched = storedPassword != null && storedPassword.equals(oldPassword);
        }

        if (!passwordMatched) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        int rows = userMapper.updateById(user);
        if (rows != 1) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "update password failed");
        }
    }
    @Override
    public List<UserBriefResponse> listAllUsers() {
        List<User> users = userMapper.selectList(null);
        return users.stream()
                .map(UserConverter::toBriefResponse)
                .toList();
    }

    @Override
    public void banUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        if ("ADMIN".equals(user.getRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "不能禁用管理员");
        }
        user.setStatus(0);
        userMapper.updateById(user);
    }

    @Override
    public void unbanUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        user.setStatus(1);
        userMapper.updateById(user);
    }
}