package com.studyhub.service.impl;

import com.studyhub.dto.HealthResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HealthServiceImplTest {

    @Mock private DataSource dataSource;
    @Mock private Connection connection;
    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;
    @Mock private RabbitTemplate rabbitTemplate;
    @InjectMocks private HealthServiceImpl healthService;

    @Test
    void healthIncludesRabbitmqStatus() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(3)).thenReturn(true);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(rabbitTemplate.execute(any())).thenReturn(true);

        HealthResponse response = healthService.checkHealth();

        assertEquals("UP", response.getStatus());
        assertEquals("UP", response.getRabbitmq());
    }

    @Test
    void rabbitmqFailureDegradesHealth() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(3)).thenReturn(true);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(rabbitTemplate.execute(any())).thenThrow(new AmqpException("connection refused"));

        HealthResponse response = healthService.checkHealth();

        assertEquals("DEGRADED", response.getStatus());
        assertEquals("DOWN", response.getRabbitmq());
    }
}
