package com.studyhub.mq;

import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitOperations;
import org.springframework.amqp.rabbit.core.ChannelCallback;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FailedMessageHandlerTest {

    @Mock private RabbitTemplate rabbitTemplate;
    @Mock private Channel consumerChannel;
    @Mock private Channel publisherChannel;
    @Mock private RabbitOperations operations;
    @InjectMocks private FailedMessageHandler failedMessageHandler;

    @Test
    void park_acknowledgesOriginalAfterParking() throws Exception {
        Message message = messageWithTag(5L);
        when(rabbitTemplate.execute(any())).thenAnswer(invocation -> {
            ChannelCallback<?> callback = invocation.getArgument(0);
            return callback.doInRabbit(publisherChannel);
        });
        when(rabbitTemplate.invoke(any())).thenAnswer(invocation -> {
            RabbitOperations.OperationsCallback<?> callback = invocation.getArgument(0);
            return callback.doInRabbit(operations);
        });

        failedMessageHandler.park(message, consumerChannel, "failed.queue");

        verify(publisherChannel).queueDeclarePassive("failed.queue");
        verify(operations).send("", "failed.queue", message);
        verify(operations).waitForConfirmsOrDie(5000);
        verify(consumerChannel).basicAck(5L, false);
        verify(consumerChannel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
    }

    @Test
    void park_requeuesOriginalWhenParkingFails() throws Exception {
        Message message = messageWithTag(6L);
        when(rabbitTemplate.invoke(any())).thenThrow(new AmqpException("publish failed"));

        failedMessageHandler.park(message, consumerChannel, "failed.queue");

        verify(consumerChannel).basicNack(6L, false, true);
        verify(consumerChannel, never()).basicAck(anyLong(), anyBoolean());
    }

    private Message messageWithTag(long deliveryTag) {
        MessageProperties properties = new MessageProperties();
        properties.setDeliveryTag(deliveryTag);
        return new Message(new byte[0], properties);
    }
}
