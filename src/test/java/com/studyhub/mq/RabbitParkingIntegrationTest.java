package com.studyhub.mq;

import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RabbitParkingIntegrationTest {

    @Test
    @EnabledIfEnvironmentVariable(named = "STUDYHUB_RABBIT_INTEGRATION", matches = "true")
    void failedMessageIsStoredInRabbitBeforeOriginalIsAcknowledged() throws Exception {
        CachingConnectionFactory factory = new CachingConnectionFactory("localhost");
        factory.setUsername("guest");
        factory.setPassword("guest");
        factory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);
        RabbitAdmin admin = new RabbitAdmin(factory);
        RabbitTemplate template = new RabbitTemplate(factory);
        String queueName = "studyhub.test.failed." + UUID.randomUUID();

        try {
            admin.declareQueue(new Queue(queueName, true, false, true));
            MessageProperties properties = new MessageProperties();
            properties.setDeliveryTag(1L);
            Message original = new Message("failed-message".getBytes(StandardCharsets.UTF_8), properties);
            Channel consumerChannel = mock(Channel.class);

            new FailedMessageHandler(template).park(original, consumerChannel, queueName);

            verify(consumerChannel).basicAck(1L, false);
            Message parked = template.receive(queueName, 3000);
            assertNotNull(parked);
            assertArrayEquals(original.getBody(), parked.getBody());
        } finally {
            admin.deleteQueue(queueName);
            factory.destroy();
        }
    }
}
