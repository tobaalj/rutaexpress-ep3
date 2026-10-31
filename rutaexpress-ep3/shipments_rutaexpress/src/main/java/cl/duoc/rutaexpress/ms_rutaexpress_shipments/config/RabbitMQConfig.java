package cl.duoc.rutaexpress.ms_rutaexpress_shipments.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración RabbitMQ del microservicio de envíos (productor).
 *
 * Declara (de forma idempotente) los exchanges de comandos; las colas, bindings y DLQs
 * pertenecen a ms-rutaexpress-notify, que es quien consume:
 *
 *   shipments --email.send / warehouse.ticket / label.gen--> [cmd.direct] --> q.cmd.email / q.cmd.warehouse / q.cmd.label
 *                                                            [cmd.topic ] --> (mismos consumidores, por patrón)
 */
@Configuration
@EnableConfigurationProperties(MessagingProperties.class)
public class RabbitMQConfig {

    @Bean
    public DirectExchange directExchange(MessagingProperties props) {
        return new DirectExchange(props.exchanges().direct(), true, false);
    }

    @Bean
    public TopicExchange topicExchange(MessagingProperties props) {
        return new TopicExchange(props.exchanges().topic(), true, false);
    }

    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
