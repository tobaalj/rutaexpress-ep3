package cl.duoc.rutaexpress.ms_rutaexpress_catalog.config;

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
 * Topología RabbitMQ del microservicio de catálogo.
 *
 *   [catalog.exchange (topic)] --catalog.#--> [audit.queue] --(rechazo)--> [dlx] --> [audit.dlq]
 */
@Configuration
@EnableConfigurationProperties(MessagingProperties.class)
public class RabbitMQConfig {

    @Bean
    public TopicExchange catalogExchange(MessagingProperties props) {
        return new TopicExchange(props.exchange(), true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange(MessagingProperties props) {
        return new DirectExchange(props.deadLetterExchange(), true, false);
    }

    @Bean
    public Queue auditQueue(MessagingProperties props) {
        return QueueBuilder.durable(props.queues().audit())
                .deadLetterExchange(props.deadLetterExchange())
                .deadLetterRoutingKey(props.routingKeys().auditDead())
                .build();
    }

    @Bean
    public Queue auditDeadLetterQueue(MessagingProperties props) {
        return QueueBuilder.durable(props.deadLetterQueues().audit()).build();
    }

    @Bean
    public Binding auditBinding(@Qualifier("auditQueue") Queue queue,
                                @Qualifier("catalogExchange") TopicExchange exchange,
                                MessagingProperties props) {
        return BindingBuilder.bind(queue).to(exchange).with(props.routingKeys().auditBinding());
    }

    @Bean
    public Binding auditDeadLetterBinding(@Qualifier("auditDeadLetterQueue") Queue queue,
                                          @Qualifier("deadLetterExchange") DirectExchange dlx,
                                          MessagingProperties props) {
        return BindingBuilder.bind(queue).to(dlx).with(props.routingKeys().auditDead());
    }

    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
