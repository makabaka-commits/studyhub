package com.studyhub.controller;

import com.studyhub.common.Result;
import com.studyhub.dto.PasswordUpdateRequest;
import com.studyhub.dto.UserRegisterRequest;
import com.studyhub.dto.UserUpdateRequest;
import com.studyhub.service.UserService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import com.studyhub.dto.UserLoginRequest;
import com.studyhub.dto.UserLoginResponse;
import com.studyhub.dto.CurrentUserResponse;
import com.studyhub.common.annotation.RateLimit;

@Tag(name = "认证接口")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<Void> register(@RequestBody @Valid UserRegisterRequest request) {
        userService.register(request);
        return Result.success();
    }
    @RateLimit(key = "'register:' + #request.username", window = 3600, limit = 1, message = "该用户名已注册")


    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<UserLoginResponse> login(@RequestBody @Valid UserLoginRequest request) {
        UserLoginResponse response = userService.login(request);
        return Result.success(response);
    }
    @RateLimit(key = "'login:' + #request.username", window = 60, limit = 5, message = "登录尝试太频繁")

    @Operation(summary = "获取当前登录用户")
    @GetMapping("/me")
    public Result<CurrentUserResponse> getCurrentUser() {
        CurrentUserResponse response = userService.getCurrentUser();
        return Result.success(response);
    }

    @Operation(summary = "修改个人资料")
    @PutMapping("/me/profile")
    public Result<Void> updateProfile(@RequestBody @Valid UserUpdateRequest request) {
        userService.updateProfile(request);
        return Result.success();
    }

    @Operation(summary = "修改密码")
    @PutMapping("/me/password")
    public Result<Void> updatePassword(@RequestBody @Valid PasswordUpdateRequest request) {
        userService.updatePassword(request);
        return Result.success();
    }
}
