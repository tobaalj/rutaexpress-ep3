package cl.duoc.rutaexpress.ms_rutaexpress_shipments.messaging;

import cl.duoc.rutaexpress.ms_rutaexpress_shipments.config.MessagingProperties;
import cl.duoc.rutaexpress.ms_rutaexpress_shipments.config.RabbitMQConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RabbitMQConfigTest {

    private final RabbitMQConfig config = new RabbitMQConfig();
    private final MessagingProperties props = MessagingTestSupport.props();

    @Test
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
    }
}
