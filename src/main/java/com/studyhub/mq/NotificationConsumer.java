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
 * 通知消息消费者
 * 从 RabbitMQ 取消息，异步写入通知到数据库
 */
@Component
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    private final NotificationService notificationService;
    private final FailedMessageHandler failedMessageHandler;

    public NotificationConsumer(NotificationService notificationService,
                                FailedMessageHandler failedMessageHandler) {
        this.notificationService = notificationService;
        this.failedMessageHandler = failedMessageHandler;
    }

    /**
     * 监听通知队列
     *
     * @param message 通知消息
     * @param channel RabbitMQ 通道（用于手动确认）
     * @param amqpMessage 原始消息（包含投递标签）
     */
    @RabbitListener(queues = RabbitConfig.QUEUE_NOTIFICATION)
    public void handleNotification(NotificationMessage message, Channel channel,
                                   org.springframework.amqp.core.Message amqpMessage) {
        try {
            log.debug("收到通知消息: type={}, receiverId={}", message.getType(), message.getReceiverId());

            // 调用 Service 写入数据库
            notificationService.createNotification(
                    message.getReceiverId(),
                    message.getSenderId(),
                    message.getNoteId(),
                    message.getType(),
                    message.getContent()
            );

        } catch (Exception e) {
            log.error("通知消息处理失败", e);
            failedMessageHandler.park(amqpMessage, channel, RabbitConfig.QUEUE_NOTIFICATION_FAILED);
            return;
        }
        try {
            channel.basicAck(amqpMessage.getMessageProperties().getDeliveryTag(), false);
            log.debug("通知消息处理完成: type={}", message.getType());
        } catch (Exception e) {
            log.error("通知消息确认失败，等待连接恢复后重投", e);
        }
    }
}
