package com.studyhub.mq;

import com.studyhub.config.RabbitConfig;
import com.studyhub.dto.BrowseHistoryMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 浏览历史消息生产者
 * 发送浏览记录到 RabbitMQ 异步处理
 */
@Component
public class BrowseHistoryProducer {

    private static final Logger log = LoggerFactory.getLogger(BrowseHistoryProducer.class);

    private final RabbitTemplate rabbitTemplate;

    public BrowseHistoryProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 发送浏览历史消息
     */
    public void sendBrowseHistory(BrowseHistoryMessage message) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitConfig.EXCHANGE_NOTIFICATION,
                    RabbitConfig.ROUTING_BROWSE_HISTORY,
                    message
            );
            log.debug("浏览历史消息已发送: userId={}, noteId={}", message.getUserId(), message.getNoteId());
        } catch (Exception e) {
            log.error("浏览历史消息发送失败", e);
        }
    }
}