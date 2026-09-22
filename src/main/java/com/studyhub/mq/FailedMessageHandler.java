package com.studyhub.mq;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 将处理失败的原始消息保留在独立队列，避免直接拒绝造成消息丢失。
 */
@Component
public class FailedMessageHandler {

    private static final Logger log = LoggerFactory.getLogger(FailedMessageHandler.class);
    private final RabbitTemplate rabbitTemplate;

    public FailedMessageHandler(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void park(Message message, Channel consumerChannel, String failureQueue) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            // 被动检查不修改队列；如果暂存队列不存在，保留原消息等待重投。
            rabbitTemplate.execute(channel -> {
                channel.queueDeclarePassive(failureQueue);
                return null;
            });
            message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            rabbitTemplate.invoke(operations -> {
                operations.send("", failureQueue, message);
                operations.waitForConfirmsOrDie(5000);
                return null;
            });
            consumerChannel.basicAck(deliveryTag, false);
            log.error("消息处理失败，已暂存到队列 {}", failureQueue);
        } catch (Exception e) {
            log.error("暂存失败消息未成功，重新入队原消息: {}", failureQueue, e);
            try {
                consumerChannel.basicNack(deliveryTag, false, true);
            } catch (Exception nackException) {
                log.error("原消息重新入队失败，等待连接恢复后重投", nackException);
            }
        }
    }
}
