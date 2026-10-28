package cl.duoc.rutaexpress.ms_rutaexpress_shipments.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topología RabbitMQ del microservicio de envíos.
 *
 * Rutas de mensajería:
 *   [shipments.exchange (topic)] --shipment.#------> [notifications.queue] --(rechazo)--> [dlx] --> [notifications.dlq]
 *   [shipments.exchange (topic)] --shipment.created-> [tickets.queue]       --(rechazo)--> [dlx] --> [tickets.dlq]
 */
@Configuration
@EnableConfigurationProperties(MessagingProperties.class)
public class RabbitMQConfig {

    // ---------- Exchanges ----------
    @Bean
    public TopicExchange shipmentsExchange(MessagingProperties props) {
        return new TopicExchange(props.exchange(), true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange(MessagingProperties props) {
        return new DirectExchange(props.deadLetterExchange(), true, false);
    }

    // ---------- Colas principales (con DLX configurado) ----------
    @Bean
    public Queue notificationsQueue(MessagingProperties props) {
        return QueueBuilder.durable(props.queues().notifications())
                .deadLetterExchange(props.deadLetterExchange())
                .deadLetterRoutingKey(props.routingKeys().notificationsDead())
                .build();
    }

    @Bean
    public Queue ticketsQueue(MessagingProperties props) {
        return QueueBuilder.durable(props.queues().tickets())
                .deadLetterExchange(props.deadLetterExchange())
                .deadLetterRoutingKey(props.routingKeys().ticketsDead())
                .build();
    }

    // ---------- Dead Letter Queues ----------
    @Bean
    public Queue notificationsDeadLetterQueue(MessagingProperties props) {
        return QueueBuilder.durable(props.deadLetterQueues().notifications()).build();
    }

    @Bean
    public Queue ticketsDeadLetterQueue(MessagingProperties props) {
        return QueueBuilder.durable(props.deadLetterQueues().tickets()).build();
    }

    // ---------- Bindings ----------
    @Bean
    public Binding notificationsBinding(@Qualifier("notificationsQueue") Queue queue,
                                        @Qualifier("shipmentsExchange") TopicExchange exchange,
                                        MessagingProperties props) {
        return BindingBuilder.bind(queue).to(exchange).with(props.routingKeys().notificationsBinding());
    }

    @Bean
    public Binding ticketsBinding(@Qualifier("ticketsQueue") Queue queue,
                                  @Qualifier("shipmentsExchange") TopicExchange exchange,
                                  MessagingProperties props) {
        return BindingBuilder.bind(queue).to(exchange).with(props.routingKeys().ticketsBinding());
    }

    @Bean
    public Binding notificationsDeadLetterBinding(@Qualifier("notificationsDeadLetterQueue") Queue queue,
                                                  @Qualifier("deadLetterExchange") DirectExchange dlx,
                                                  MessagingProperties props) {
        return BindingBuilder.bind(queue).to(dlx).with(props.routingKeys().notificationsDead());
    }

    @Bean
    public Binding ticketsDeadLetterBinding(@Qualifier("ticketsDeadLetterQueue") Queue queue,
                                            @Qualifier("deadLetterExchange") DirectExchange dlx,
                                            MessagingProperties props) {
        return BindingBuilder.bind(queue).to(dlx).with(props.routingKeys().ticketsDead());
    }

    // ---------- Serialización JSON ----------
    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
