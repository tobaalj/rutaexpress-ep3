package cl.duoc.rutaexpress.ms_rutaexpress_shipments.messaging;

import cl.duoc.rutaexpress.ms_rutaexpress_shipments.config.MessagingProperties;
import cl.duoc.rutaexpress.ms_rutaexpress_shipments.config.RabbitMQConfig;
import org.junit.jupiter.api.Test;
<<<<<<< HEAD
=======
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
>>>>>>> d39a0dbc18aebda18ac1db90e44f7b6a6d865fab

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RabbitMQConfigTest {

    private final RabbitMQConfig config = new RabbitMQConfig();
    private final MessagingProperties props = MessagingTestSupport.props();

    @Test
<<<<<<< HEAD
    void commandExchangesUseConfiguredNamesAndTypes() {
        assertEquals("cmd.direct", config.directExchange(props).getName());
        assertEquals("direct", config.directExchange(props).getType());
        assertTrue(config.directExchange(props).isDurable());
        assertEquals("cmd.topic", config.topicExchange(props).getName());
        assertEquals("topic", config.topicExchange(props).getType());
    }

    @Test
    void emailStatusesAreConfigurable() {
        assertEquals(3, props.emailStatuses().size());
        assertTrue(props.emailStatuses().contains("EN_RUTA"));
=======
    void mainQueuesAreDurableAndPointToDeadLetterExchange() {
        Queue q = config.notificationsQueue(props);

        assertEquals("notif.q", q.getName());
        assertTrue(q.isDurable());
        assertEquals("dlx", q.getArguments().get("x-dead-letter-exchange"));
        assertEquals("notif.dead", q.getArguments().get("x-dead-letter-routing-key"));
    }

    @Test
    void bindingsUseConfiguredRoutingKeys() {
        Binding b = config.ticketsBinding(config.ticketsQueue(props), config.shipmentsExchange(props), props);

        assertEquals("tickets.q", b.getDestination());
        assertEquals("ex", b.getExchange());
        assertEquals("shipment.created", b.getRoutingKey());
    }

    @Test
    void deadLetterQueueIsBoundToDlx() {
        Binding b = config.notificationsDeadLetterBinding(
                config.notificationsDeadLetterQueue(props), config.deadLetterExchange(props), props);

        assertEquals("notif.dlq", b.getDestination());
        assertEquals("dlx", b.getExchange());
        assertEquals("notif.dead", b.getRoutingKey());
>>>>>>> d39a0dbc18aebda18ac1db90e44f7b6a6d865fab
    }
}
