package cl.duoc.rutaexpress.ms_rutaexpress_notify.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topología RabbitMQ del microservicio notify (6 colas: 3 flujos + 3 DLQ).
 *
 *   cmd.direct (direct) --email.send------> q.cmd.email     --(rechazo)--> cmd.dead.dlx --> q.cmd.email.dlq
 *   cmd.topic  (topic)  --email.*---------> q.cmd.email
 *   cmd.direct (direct) --warehouse.ticket> q.cmd.warehouse --(rechazo)--> cmd.dead.dlx --> q.cmd.warehouse.dlq
 *   cmd.topic  (topic)  --warehouse.#-----> q.cmd.warehouse
 *   cmd.direct (direct) --label.gen-------> q.cmd.label     --(rechazo)--> cmd.dead.dlx --> q.cmd.label.dlq
 *   cmd.topic  (topic)  --label.*---------> q.cmd.label
 */
@Configuration
@EnableConfigurationProperties(MessagingProperties.class)
public class RabbitMQConfig {

    // ---------- Exchanges ----------
    @Bean
    public DirectExchange directExchange(MessagingProperties props) {
        return new DirectExchange(props.exchanges().direct(), true, false);
    }

    @Bean
    public TopicExchange topicExchange(MessagingProperties props) {
        return new TopicExchange(props.exchanges().topic(), true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange(MessagingProperties props) {
        return new DirectExchange(props.exchanges().deadLetter(), true, false);
    }

    // ---------- Flujo email ----------
    @Bean
    public Queue emailQueue(MessagingProperties props) {
        return QueueBuilder.durable(props.queues().email())
                .deadLetterExchange(props.exchanges().deadLetter())
                .deadLetterRoutingKey(props.deadLetterQueues().email())
                .build();
    }

    @Bean
    public Queue emailDeadLetterQueue(MessagingProperties props) {
        return QueueBuilder.durable(props.deadLetterQueues().email()).build();
    }

    @Bean
    public Binding emailDirectBinding(@Qualifier("emailQueue") Queue queue,
                                   @Qualifier("directExchange") DirectExchange exchange,
                                   MessagingProperties props) {
        return BindingBuilder.bind(queue).to(exchange).with(props.directKeys().email());
    }

    @Bean
    public Binding emailTopicBinding(@Qualifier("emailQueue") Queue queue,
                                  @Qualifier("topicExchange") TopicExchange exchange,
                                  MessagingProperties props) {
        return BindingBuilder.bind(queue).to(exchange).with(props.topicKeys().email());
    }

    @Bean
    public Binding emailDeadLetterBinding(@Qualifier("emailDeadLetterQueue") Queue queue,
                                       @Qualifier("deadLetterExchange") DirectExchange dlx,
                                       MessagingProperties props) {
        return BindingBuilder.bind(queue).to(dlx).with(props.deadLetterQueues().email());
    }

    // ---------- Flujo warehouse ----------
    @Bean
    public Queue warehouseQueue(MessagingProperties props) {
        return QueueBuilder.durable(props.queues().warehouse())
                .deadLetterExchange(props.exchanges().deadLetter())
                .deadLetterRoutingKey(props.deadLetterQueues().warehouse())
                .build();
    }

    @Bean
    public Queue warehouseDeadLetterQueue(MessagingProperties props) {
        return QueueBuilder.durable(props.deadLetterQueues().warehouse()).build();
    }

    @Bean
    public Binding warehouseDirectBinding(@Qualifier("warehouseQueue") Queue queue,
                                   @Qualifier("directExchange") DirectExchange exchange,
                                   MessagingProperties props) {
        return BindingBuilder.bind(queue).to(exchange).with(props.directKeys().warehouse());
    }

    @Bean
    public Binding warehouseTopicBinding(@Qualifier("warehouseQueue") Queue queue,
                                  @Qualifier("topicExchange") TopicExchange exchange,
                                  MessagingProperties props) {
        return BindingBuilder.bind(queue).to(exchange).with(props.topicKeys().warehouse());
    }

    @Bean
    public Binding warehouseDeadLetterBinding(@Qualifier("warehouseDeadLetterQueue") Queue queue,
                                       @Qualifier("deadLetterExchange") DirectExchange dlx,
                                       MessagingProperties props) {
        return BindingBuilder.bind(queue).to(dlx).with(props.deadLetterQueues().warehouse());
    }

    // ---------- Flujo label ----------
    @Bean
    public Queue labelQueue(MessagingProperties props) {
        return QueueBuilder.durable(props.queues().label())
                .deadLetterExchange(props.exchanges().deadLetter())
                .deadLetterRoutingKey(props.deadLetterQueues().label())
                .build();
    }

    @Bean
    public Queue labelDeadLetterQueue(MessagingProperties props) {
        return QueueBuilder.durable(props.deadLetterQueues().label()).build();
    }

    @Bean
    public Binding labelDirectBinding(@Qualifier("labelQueue") Queue queue,
                                   @Qualifier("directExchange") DirectExchange exchange,
                                   MessagingProperties props) {
        return BindingBuilder.bind(queue).to(exchange).with(props.directKeys().label());
    }

    @Bean
    public Binding labelTopicBinding(@Qualifier("labelQueue") Queue queue,
                                  @Qualifier("topicExchange") TopicExchange exchange,
                                  MessagingProperties props) {
        return BindingBuilder.bind(queue).to(exchange).with(props.topicKeys().label());
    }

    @Bean
    public Binding labelDeadLetterBinding(@Qualifier("labelDeadLetterQueue") Queue queue,
                                       @Qualifier("deadLetterExchange") DirectExchange dlx,
                                       MessagingProperties props) {
        return BindingBuilder.bind(queue).to(dlx).with(props.deadLetterQueues().label());
    }
}
