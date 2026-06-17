package com.studyhub.config;

import com.studyhub.common.JwtUtil;
import io.jsonwebtoken.JwtException;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

import java.security.Principal;

public class WebSocketUserInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // 从握手时存入的属性中获取 userId
            Object userIdObj = accessor.getSessionAttributes() != null
                    ? accessor.getSessionAttributes().get("userId") : null;

            if (userIdObj instanceof Long userId) {
                // 设置 Principal，后续可以通过 @AuthenticationPrincipal 或 Principal 参数获取
                accessor.setUser(() -> String.valueOf(userId));
            }
        }

        return message;
    }
}