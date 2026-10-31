package cl.duoc.rutaexpress.ms_rutaexpress_rabbitadmin.service;

import cl.duoc.rutaexpress.ms_rutaexpress_rabbitadmin.config.RabbitAdminProperties;
import cl.duoc.rutaexpress.ms_rutaexpress_rabbitadmin.dto.BindingRequestDTO;
import cl.duoc.rutaexpress.ms_rutaexpress_rabbitadmin.dto.ExchangeRequestDTO;
import cl.duoc.rutaexpress.ms_rutaexpress_rabbitadmin.dto.QueueInfoDTO;
import cl.duoc.rutaexpress.ms_rutaexpress_rabbitadmin.dto.QueueRequestDTO;
import cl.duoc.rutaexpress.ms_rutaexpress_rabbitadmin.exception.InvalidRabbitRequestException;
import cl.duoc.rutaexpress.ms_rutaexpress_rabbitadmin.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.HeadersExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Toda la lógica de administración de RabbitMQ (colas, exchanges y bindings) está aquí.
 * Los controladores solo llaman a estos métodos y no conocen la librería de RabbitMQ.
 */
@Service
public class RabbitAdminService {

    private static final Logger log = LoggerFactory.getLogger(RabbitAdminService.class);

    private final AmqpAdmin amqpAdmin;
    private final RabbitAdminProperties properties;

    public RabbitAdminService(AmqpAdmin amqpAdmin, RabbitAdminProperties properties) {
        this.amqpAdmin = amqpAdmin;
        this.properties = properties;
    }

    // ---------------- Colas ----------------

    public QueueInfoDTO createQueue(QueueRequestDTO request) {
        validateName(request.getName(), "cola");
        if (request.getDeadLetterRoutingKey() != null && !request.getDeadLetterRoutingKey().isBlank()
                && (request.getDeadLetterExchange() == null || request.getDeadLetterExchange().isBlank())) {
            throw new InvalidRabbitRequestException(
                    "deadLetterRoutingKey requiere indicar también deadLetterExchange");
        }

        Map<String, Object> args = new HashMap<>();
        if (request.getDeadLetterExchange() != null && !request.getDeadLetterExchange().isBlank()) {
            args.put("x-dead-letter-exchange", request.getDeadLetterExchange());
        }
        if (request.getDeadLetterRoutingKey() != null && !request.getDeadLetterRoutingKey().isBlank()) {
            args.put("x-dead-letter-routing-key", request.getDeadLetterRoutingKey());
        }
        if (request.getMessageTtlMs() != null) {
            args.put("x-message-ttl", request.getMessageTtlMs());
        }

        Queue queue = new Queue(request.getName(), request.isDurable(), request.isExclusive(),
                request.isAutoDelete(), args);
        amqpAdmin.declareQueue(queue);
        log.info("Cola '{}' creada", request.getName());
        return new QueueInfoDTO(request.getName(), request.isDurable(), 0, 0);
    }

    public QueueInfoDTO getQueue(String name) {
        QueueInformation info = amqpAdmin.getQueueInfo(name);
        if (info == null) {
            throw new ResourceNotFoundException("La cola '" + name + "' no existe");
        }
        return new QueueInfoDTO(info.getName(), true, info.getMessageCount(), info.getConsumerCount());
    }

    public void deleteQueue(String name) {
        validateName(name, "cola");
        if (amqpAdmin.getQueueInfo(name) == null) {
            throw new ResourceNotFoundException("La cola '" + name + "' no existe");
        }
        amqpAdmin.deleteQueue(name);
        log.info("Cola '{}' eliminada", name);
    }

    // ---------------- Exchanges ----------------

    public void createExchange(ExchangeRequestDTO request) {
        validateName(request.getName(), "exchange");
        Exchange exchange = switch (request.getType()) {
            case DIRECT -> new DirectExchange(request.getName(), request.isDurable(), request.isAutoDelete());
            case TOPIC -> new TopicExchange(request.getName(), request.isDurable(), request.isAutoDelete());
            case FANOUT -> new FanoutExchange(request.getName(), request.isDurable(), request.isAutoDelete());
            case HEADERS -> new HeadersExchange(request.getName(), request.isDurable(), request.isAutoDelete());
        };
        amqpAdmin.declareExchange(exchange);
        log.info("Exchange '{}' ({}) creado", request.getName(), request.getType());
    }

    public void deleteExchange(String name) {
        validateName(name, "exchange");
        // RabbitMQ no permite consultar si un exchange existe vía AmqpAdmin: deleteExchange devuelve si pudo borrarlo.
        if (!amqpAdmin.deleteExchange(name)) {
            throw new ResourceNotFoundException("El exchange '" + name + "' no existe o no se pudo eliminar");
        }
        log.info("Exchange '{}' eliminado", name);
    }

    // ---------------- Bindings ----------------

    public void createBinding(BindingRequestDTO request) {
        amqpAdmin.declareBinding(toBinding(request.getQueue(), request.getExchange(), request.getRoutingKey()));
        log.info("Binding creado: {} -> {} (routingKey='{}')",
                request.getExchange(), request.getQueue(), request.getRoutingKey());
    }

    public void deleteBinding(String queue, String exchange, String routingKey) {
        if (queue == null || queue.isBlank() || exchange == null || exchange.isBlank()) {
            throw new InvalidRabbitRequestException("Los parámetros 'queue' y 'exchange' son obligatorios");
        }
        amqpAdmin.removeBinding(toBinding(queue, exchange, routingKey == null ? "" : routingKey));
        log.info("Binding eliminado: {} -> {} (routingKey='{}')", exchange, queue, routingKey);
    }

    private Binding toBinding(String queue, String exchange, String routingKey) {
        return new Binding(queue, Binding.DestinationType.QUEUE, exchange, routingKey, null);
    }

    // ---------------- Validaciones ----------------

    private void validateName(String name, String resource) {
        if (name == null || name.isBlank()) {
            throw new InvalidRabbitRequestException("El nombre de la " + resource + " no puede estar vacío");
        }
        for (String prefix : properties.reservedPrefixes()) {
            if (name.startsWith(prefix)) {
                throw new InvalidRabbitRequestException(
                        "El nombre '" + name + "' usa el prefijo reservado '" + prefix + "'");
            }
        }
    }
}
