package com.studyhub.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.api.RabbitListenerErrorHandler;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置
 * 定义交换机、队列、绑定关系
 */
@Configuration
public class RabbitConfig {

    // ===== 交换机名称 =====
    public static final String EXCHANGE_NOTIFICATION = "studyhub.notification";

    // ===== 通知队列 =====
    public static final String QUEUE_NOTIFICATION = "studyhub.queue.notification";
    public static final String ROUTING_NOTIFICATION = "notification";

    // ===== 浏览历史队列 =====
    public static final String QUEUE_BROWSE_HISTORY = "studyhub.queue.browse.history";
    public static final String ROUTING_BROWSE_HISTORY = "browse.history";
    // ===== 延迟通知队列（聚合点赞） =====
    public static final String QUEUE_DELAY_NOTIFICATION = "studyhub.queue.notification.delay";
    public static final String ROUTING_DELAY_NOTIFICATION = "notification.delay";

    // ===== 死信交换机 =====
    public static final String EXCHANGE_DLX = "studyhub.dlx";
    public static final String QUEUE_DLX = "studyhub.queue.dlx";
    public static final String QUEUE_NOTIFICATION_FAILED = "studyhub.queue.notification.failed";
    public static final String QUEUE_BROWSE_HISTORY_FAILED = "studyhub.queue.browse.history.failed";

    /**
     * 声明 Direct 交换机
     */
    @Bean
    public DirectExchange notificationExchange() {
        return ExchangeBuilder.directExchange(EXCHANGE_NOTIFICATION)
                .durable(true)      // 持久化，重启后不丢失
                .build();
    }

    /**
     * 声明通知队列
     */
    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(QUEUE_NOTIFICATION)
                .build();
    }

    /**
     * 声明浏览历史队列
     */
    @Bean
    public Queue browseHistoryQueue() {
        return QueueBuilder.durable(QUEUE_BROWSE_HISTORY)
                .build();
    }

    @Bean
    public Queue notificationFailedQueue() {
        return QueueBuilder.durable(QUEUE_NOTIFICATION_FAILED).build();
    }

    @Bean
    public Queue browseHistoryFailedQueue() {
        return QueueBuilder.durable(QUEUE_BROWSE_HISTORY_FAILED).build();
    }

    /**
     * 绑定通知队列到交换机
     */
    @Bean
    public Binding notificationBinding() {
        return BindingBuilder.bind(notificationQueue())
                .to(notificationExchange())
                .with(ROUTING_NOTIFICATION);
    }

    /**
     * 绑定浏览历史队列到交换机
     */
    @Bean
    public Binding browseHistoryBinding() {
        return BindingBuilder.bind(browseHistoryQueue())
                .to(notificationExchange())
                .with(ROUTING_BROWSE_HISTORY);
    }

    /**
     * 消息转换器：Java 对象 <-> JSON
     * 这样发送对象时会自动序列化为 JSON，接收时自动反序列化
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 配置 RabbitTemplate
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        // 设置消息确认回调
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                // 消息发送到交换机失败，记录日志
                System.err.println("消息发送失败: " + cause);
            }
        });
        return template;
    }

    /**
     * 消费者异常处理器：记录日志，防止消息无限重试
     */
    @Bean
    public RabbitListenerErrorHandler rabbitListenerErrorHandler() {
        return (amqpMessage, message, exception) -> {
            System.err.println("消息处理失败: " + exception.getMessage());
            // 返回 null 表示确认消息（避免无限重试）
            return null;
        };
    }
    /**
     * 死信交换机
     */
    @Bean
    public DirectExchange dlxExchange() {
        return ExchangeBuilder.directExchange(EXCHANGE_DLX)
                .durable(true)
                .build();
    }

    /**
     * 死信队列（用于处理过期的延迟消息）
     */
    @Bean
    public Queue dlxQueue() {
        return QueueBuilder.durable(QUEUE_DLX)
                .build();
    }

    /**
     * 绑定死信队列到死信交换机
     */
    @Bean
    public Binding dlxBinding() {
        return BindingBuilder.bind(dlxQueue())
                .to(dlxExchange())
                .with(ROUTING_DELAY_NOTIFICATION);
    }

    /**
     * 延迟通知队列
     * 消息在此队列中等待 5 分钟，过期后转到死信交换机
     */
    @Bean
    public Queue delayNotificationQueue() {
        return QueueBuilder.durable(QUEUE_DELAY_NOTIFICATION)
                // 消息过期后转发到死信交换机
                .deadLetterExchange(EXCHANGE_DLX)
                // 转发时使用的 routing key
                .deadLetterRoutingKey(ROUTING_DELAY_NOTIFICATION)
                // 消息 TTL：5 分钟（毫秒）
                .ttl(5 * 60 * 1000)
                .build();
    }

    /**
     * 绑定延迟队列到通知交换机
     */
    @Bean
    public Binding delayNotificationBinding() {
        return BindingBuilder.bind(delayNotificationQueue())
                .to(notificationExchange())
                .with(ROUTING_DELAY_NOTIFICATION);
    }
}
