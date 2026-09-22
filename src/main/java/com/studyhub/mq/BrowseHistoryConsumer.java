package com.studyhub.mq;

import com.rabbitmq.client.Channel;
import com.studyhub.config.RabbitConfig;
import com.studyhub.dto.BrowseHistoryMessage;
import com.studyhub.service.BrowseHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 浏览历史消息消费者
 * 从 RabbitMQ 取消息，异步写入浏览记录
 */
@Component
public class BrowseHistoryConsumer {

    private static final Logger log = LoggerFactory.getLogger(BrowseHistoryConsumer.class);

    private final BrowseHistoryService browseHistoryService;
    private final FailedMessageHandler failedMessageHandler;

    public BrowseHistoryConsumer(BrowseHistoryService browseHistoryService,
                                 FailedMessageHandler failedMessageHandler) {
        this.browseHistoryService = browseHistoryService;
        this.failedMessageHandler = failedMessageHandler;
    }

    /**
     * 监听浏览历史队列
     */
    @RabbitListener(queues = RabbitConfig.QUEUE_BROWSE_HISTORY)
    public void handleBrowseHistory(BrowseHistoryMessage message, Channel channel,
                                    org.springframework.amqp.core.Message amqpMessage) {
        try {
            log.debug("收到浏览历史消息: userId={}, noteId={}", message.getUserId(), message.getNoteId());

            // 调用 Service 写入数据库
            browseHistoryService.recordBrowse(message.getUserId(), message.getNoteId());

        } catch (Exception e) {
            log.error("浏览历史消息处理失败", e);
            failedMessageHandler.park(amqpMessage, channel, RabbitConfig.QUEUE_BROWSE_HISTORY_FAILED);
            return;
        }
        try {
            channel.basicAck(amqpMessage.getMessageProperties().getDeliveryTag(), false);
            log.debug("浏览历史消息处理完成");
        } catch (Exception e) {
            log.error("浏览历史消息确认失败，等待连接恢复后重投", e);
        }
    }
}
