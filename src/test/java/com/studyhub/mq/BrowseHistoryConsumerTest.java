package com.studyhub.mq;

import com.rabbitmq.client.Channel;
import com.studyhub.dto.BrowseHistoryMessage;
import com.studyhub.service.BrowseHistoryService;
import com.studyhub.config.RabbitConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class BrowseHistoryConsumerTest {

    @Mock private BrowseHistoryService browseHistoryService;
    @Mock private Channel channel;
    @Mock private FailedMessageHandler failedMessageHandler;

    @InjectMocks private BrowseHistoryConsumer consumer;

    @Test
    void handleBrowseHistory_passesMessageUserIdAndAcknowledges() throws Exception {
        MessageProperties properties = new MessageProperties();
        properties.setDeliveryTag(3L);
        Message amqpMessage = new Message(new byte[0], properties);

        consumer.handleBrowseHistory(new BrowseHistoryMessage(7L, 11L), channel, amqpMessage);

        verify(browseHistoryService).recordBrowse(7L, 11L);
        verify(channel).basicAck(3L, false);
        verifyNoInteractions(failedMessageHandler);
    }

    @Test
    void handleBrowseHistory_parksFailedMessage() {
        MessageProperties properties = new MessageProperties();
        properties.setDeliveryTag(4L);
        Message amqpMessage = new Message(new byte[0], properties);
        doThrow(new IllegalStateException("database error"))
                .when(browseHistoryService).recordBrowse(7L, 11L);

        consumer.handleBrowseHistory(new BrowseHistoryMessage(7L, 11L), channel, amqpMessage);

        verify(failedMessageHandler).park(amqpMessage, channel, RabbitConfig.QUEUE_BROWSE_HISTORY_FAILED);
    }
}
