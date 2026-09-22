package com.studyhub.service.impl;

import com.studyhub.dto.HealthResponse;
import com.studyhub.service.HealthService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;

@Service
public class HealthServiceImpl implements HealthService {

    private final DataSource dataSource;
    private final StringRedisTemplate stringRedisTemplate;
    private final RabbitTemplate rabbitTemplate;

    public HealthServiceImpl(DataSource dataSource, StringRedisTemplate stringRedisTemplate,
                             RabbitTemplate rabbitTemplate) {
        this.dataSource = dataSource;
        this.stringRedisTemplate = stringRedisTemplate;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public HealthResponse checkHealth() {
        HealthResponse response = new HealthResponse();
        response.setStatus("UP");
        response.setTimestamp(System.currentTimeMillis());

        // 检查数据库
        try (Connection conn = dataSource.getConnection()) {
            response.setDatabase(conn.isValid(3) ? "UP" : "DOWN");
        } catch (Exception e) {
            response.setDatabase("DOWN");
            response.setStatus("DEGRADED");
        }

        // 检查 Redis
        try {
            stringRedisTemplate.opsForValue().get("health-check");
            response.setRedis("UP");
        } catch (Exception e) {
            response.setRedis("DOWN");
            response.setStatus("DEGRADED");
        }

        try {
            Boolean connected = rabbitTemplate.execute(channel -> channel.isOpen());
            response.setRabbitmq(Boolean.TRUE.equals(connected) ? "UP" : "DOWN");
            if (!Boolean.TRUE.equals(connected)) {
                response.setStatus("DEGRADED");
            }
        } catch (Exception e) {
            response.setRabbitmq("DOWN");
            response.setStatus("DEGRADED");
        }

        return response;
    }
}
