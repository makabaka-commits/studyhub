package com.studyhub.mq;

import com.studyhub.config.RabbitConfig;
import com.studyhub.dto.NotificationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 通知消息生产者
 * 发送通知消息到 RabbitMQ
 */
@Component
public class NotificationProducer {

    private static final Logger log = LoggerFactory.getLogger(NotificationProducer.class);

    private final RabbitTemplate rabbitTemplate;

    public NotificationProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 发送通知消息
     */
    public void sendNotification(NotificationMessage message) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitConfig.EXCHANGE_NOTIFICATION,
                    RabbitConfig.ROUTING_NOTIFICATION,
                    message
            );
            log.debug("通知消息已发送: type={}, receiverId={}", message.getType(), message.getReceiverId());
        } catch (Exception e) {
            log.error("通知消息发送失败", e);
        }
    }
}