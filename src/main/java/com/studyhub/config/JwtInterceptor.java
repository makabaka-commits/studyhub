package com.studyhub.config;

import com.studyhub.common.JwtUtil;
import com.studyhub.common.LoginUserHolder;
import com.studyhub.exception.UnauthorizedException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class JwtInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new UnauthorizedException("please login");
        }

        String token = authorization.substring(7);

        try {
            Long userId = JwtUtil.getUserId(token);
            LoginUserHolder.setUserId(userId);
        } catch (JwtException | IllegalArgumentException e) {
            throw new UnauthorizedException("invalid token");
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        LoginUserHolder.clear();
    }
}
