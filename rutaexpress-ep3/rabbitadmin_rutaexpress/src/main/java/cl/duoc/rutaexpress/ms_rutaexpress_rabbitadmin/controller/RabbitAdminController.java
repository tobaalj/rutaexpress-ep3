package cl.duoc.rutaexpress.ms_rutaexpress_rabbitadmin.controller;

import cl.duoc.rutaexpress.ms_rutaexpress_rabbitadmin.dto.BindingRequestDTO;
import cl.duoc.rutaexpress.ms_rutaexpress_rabbitadmin.dto.ExchangeRequestDTO;
import cl.duoc.rutaexpress.ms_rutaexpress_rabbitadmin.dto.QueueInfoDTO;
import cl.duoc.rutaexpress.ms_rutaexpress_rabbitadmin.dto.QueueRequestDTO;
import cl.duoc.rutaexpress.ms_rutaexpress_rabbitadmin.service.RabbitAdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * API REST de administración de RabbitMQ.
 *
 *   POST   /api/v1/rabbit/queues                -> crear cola
 *   GET    /api/v1/rabbit/queues/{name}         -> estado de la cola
 *   DELETE /api/v1/rabbit/queues/{name}         -> eliminar cola
 *   POST   /api/v1/rabbit/exchanges             -> crear exchange
 *   DELETE /api/v1/rabbit/exchanges/{name}      -> eliminar exchange
 *   POST   /api/v1/rabbit/bindings              -> crear binding
 *   DELETE /api/v1/rabbit/bindings?queue=&exchange=&routingKey= -> eliminar binding
 */
@RestController
@RequestMapping("/api/v1/rabbit")
public class RabbitAdminController {

    private final RabbitAdminService service;

    public RabbitAdminController(RabbitAdminService service) {
        this.service = service;
    }

    @PostMapping("/queues")
    public ResponseEntity<QueueInfoDTO> createQueue(@Valid @RequestBody QueueRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createQueue(request));
    }

    @GetMapping("/queues/{name}")
    public ResponseEntity<QueueInfoDTO> getQueue(@PathVariable String name) {
        return ResponseEntity.ok(service.getQueue(name));
    }

    @DeleteMapping("/queues/{name}")
    public ResponseEntity<Void> deleteQueue(@PathVariable String name) {
        service.deleteQueue(name);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/exchanges")
    public ResponseEntity<Void> createExchange(@Valid @RequestBody ExchangeRequestDTO request) {
        service.createExchange(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/exchanges/{name}")
    public ResponseEntity<Void> deleteExchange(@PathVariable String name) {
        service.deleteExchange(name);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bindings")
    public ResponseEntity<Void> createBinding(@Valid @RequestBody BindingRequestDTO request) {
        service.createBinding(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/bindings")
    public ResponseEntity<Void> deleteBinding(@RequestParam String queue,
                                              @RequestParam String exchange,
                                              @RequestParam(defaultValue = "") String routingKey) {
        service.deleteBinding(queue, exchange, routingKey);
        return ResponseEntity.noContent().build();
    }
}
