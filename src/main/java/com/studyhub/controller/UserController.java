package com.studyhub.controller;

import com.studyhub.common.ErrorCode;
import com.studyhub.common.Result;
import com.studyhub.entity.User;
import com.studyhub.exception.BusinessException;
import com.studyhub.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户测试接口")
@RestController
public class UserController {

    private final UserMapper userMapper;

    public UserController(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Operation(summary = "根据 ID 查询用户测试接口")
    @GetMapping("/users/test/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        User user = userMapper.selectById(id);

        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "user not found");
        }

        return Result.success(user);
    }
}
