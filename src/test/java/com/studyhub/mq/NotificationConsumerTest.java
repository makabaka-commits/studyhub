package com.studyhub.mq;

import com.rabbitmq.client.Channel;
import com.studyhub.config.RabbitConfig;
import com.studyhub.dto.NotificationMessage;
import com.studyhub.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationConsumerTest {

    @Mock private NotificationService notificationService;
    @Mock private FailedMessageHandler failedMessageHandler;
    @Mock private Channel channel;
    @InjectMocks private NotificationConsumer consumer;

    @Test
    void failedNotificationGoesToParkingQueue() throws Exception {
        NotificationMessage notification = new NotificationMessage(1L, 2L, 3L, "LIKE", "test");
        MessageProperties properties = new MessageProperties();
        properties.setDeliveryTag(8L);
        Message raw = new Message(new byte[0], properties);
        doThrow(new IllegalStateException("database error"))
                .when(notificationService).createNotification(any(), any(), any(), any(), any());

        consumer.handleNotification(notification, channel, raw);

        verify(failedMessageHandler).park(raw, channel, RabbitConfig.QUEUE_NOTIFICATION_FAILED);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }
}
