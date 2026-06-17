package com.studyhub.mq;

import com.rabbitmq.client.Channel;
import com.studyhub.config.RabbitConfig;
import com.studyhub.dto.NotificationMessage;
import com.studyhub.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 延迟通知消费者
 * 处理延迟 5 分钟后的聚合通知
 */
@Component
public class DelayNotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(DelayNotificationConsumer.class);

    private final NotificationService notificationService;

    public DelayNotificationConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_DLX)
    public void handleDelayNotification(NotificationMessage message, Channel channel,
                                        org.springframework.amqp.core.Message amqpMessage) {
        try {
            log.debug("收到延迟通知消息: type={}, receiverId={}", message.getType(), message.getReceiverId());

            notificationService.createNotification(
                    message.getReceiverId(),
                    message.getSenderId(),
                    message.getNoteId(),
                    message.getType(),
                    message.getContent()
            );

            channel.basicAck(amqpMessage.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("延迟通知处理失败", e);
            try {
                channel.basicNack(amqpMessage.getMessageProperties().getDeliveryTag(), false, false);
            } catch (Exception ex) {
                log.error("消息确认失败", ex);
            }
        }
    }
}