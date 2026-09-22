package com.studyhub.config;

import com.studyhub.common.JwtUtil;
import com.studyhub.common.LoginUserHolder;
import com.studyhub.exception.UnauthorizedException;
import io.jsonwebtoken.JwtException;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;

    public WebSocketAuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        // 从 URL 参数中获取 token，例如: ws://localhost:8080/ws?token=xxx
        if (request instanceof ServletServerHttpRequest servletRequest) {
            String token = servletRequest.getServletRequest().getParameter("token");
            if (token == null || token.isBlank()) {
                throw new UnauthorizedException("please login");
            }
            try {
                Long userId = jwtUtil.getUserId(token);
                // 把 userId 存入 WebSocket 会话属性中，后续可以在处理器中获取
                attributes.put("userId", userId);
            } catch (JwtException | IllegalArgumentException e) {
                throw new UnauthorizedException("invalid token");
            }
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // 握手完成后不做额外处理
    }
}
