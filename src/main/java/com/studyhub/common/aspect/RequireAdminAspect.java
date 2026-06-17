package com.studyhub.common.aspect;

import com.studyhub.common.ErrorCode;
import com.studyhub.common.LoginUserHolder;
import com.studyhub.common.annotation.RequireAdmin;
import com.studyhub.entity.User;
import com.studyhub.exception.BusinessException;
import com.studyhub.mapper.UserMapper;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/**
 * 管理员权限切面
 * 在带有 @RequireAdmin 注解的方法执行前检查用户角色
 */
@Aspect
@Component
public class RequireAdminAspect {

    private final UserMapper userMapper;

    public RequireAdminAspect(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Before("@annotation(requireAdmin)")
    public void checkAdmin(RequireAdmin requireAdmin) {
        Long userId = LoginUserHolder.getUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "请先登录");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }

        // 检查角色是否为 ADMIN
        if (!"ADMIN".equals(user.getRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, requireAdmin.message());
        }
    }
}