package cl.duoc.rutaexpress.ms_rutaexpress_notify.messaging;

import cl.duoc.rutaexpress.ms_rutaexpress_notify.config.MessagingProperties;
import cl.duoc.rutaexpress.ms_rutaexpress_notify.config.RabbitMQConfig;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RabbitMQConfigTest {

    private final RabbitMQConfig config = new RabbitMQConfig();
    private final MessagingProperties props = NotifyTestSupport.props();

    @Test
    void exchangesHaveExpectedNamesAndTypes() {
        assertEquals("cmd.direct", config.directExchange(props).getName());
        assertEquals("direct", config.directExchange(props).getType());
        assertEquals("cmd.topic", config.topicExchange(props).getName());
        assertEquals("topic", config.topicExchange(props).getType());
        assertEquals("cmd.dead.dlx", config.deadLetterExchange(props).getName());
        assertEquals("direct", config.deadLetterExchange(props).getType());
    }

    @Test
    void everyMainQueueIsDurableAndDeadLettersToItsDlq() {
        Queue[] queues = {config.emailQueue(props), config.warehouseQueue(props), config.labelQueue(props)};
        String[] dlqs = {"q.cmd.email.dlq", "q.cmd.warehouse.dlq", "q.cmd.label.dlq"};

        for (int i = 0; i < queues.length; i++) {
            assertTrue(queues[i].isDurable());
            assertEquals("cmd.dead.dlx", queues[i].getArguments().get("x-dead-letter-exchange"));
            assertEquals(dlqs[i], queues[i].getArguments().get("x-dead-letter-routing-key"));
        }
    }

    @Test
    void emailQueueHasDirectAndTopicBindings() {
        Binding direct = config.emailDirectBinding(config.emailQueue(props), config.directExchange(props), props);
        Binding topic = config.emailTopicBinding(config.emailQueue(props), config.topicExchange(props), props);

        assertEquals("q.cmd.email", direct.getDestination());
        assertEquals("cmd.direct", direct.getExchange());
        assertEquals("email.send", direct.getRoutingKey());
        assertEquals("cmd.topic", topic.getExchange());
        assertEquals("email.*", topic.getRoutingKey());
    }

    @Test
    void warehouseAndLabelBindingsUseConfiguredKeys() {
        Binding wh = config.warehouseTopicBinding(config.warehouseQueue(props), config.topicExchange(props), props);
        Binding lb = config.labelDirectBinding(config.labelQueue(props), config.directExchange(props), props);

        assertEquals("warehouse.#", wh.getRoutingKey());
        assertEquals("label.gen", lb.getRoutingKey());
    }

    @Test
    void deadLetterQueuesAreBoundToDlx() {
        Binding b = config.labelDeadLetterBinding(config.labelDeadLetterQueue(props), config.deadLetterExchange(props), props);

        assertEquals("q.cmd.label.dlq", b.getDestination());
        assertEquals("cmd.dead.dlx", b.getExchange());
        assertEquals("q.cmd.label.dlq", b.getRoutingKey());
    }
}
