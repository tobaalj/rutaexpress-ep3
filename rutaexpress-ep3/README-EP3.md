# RutaExpress – EP3 (RabbitMQ)

## Estructura (un repo de GitHub por carpeta)
| Carpeta | Puerto | Rol |
|---|---|---|
| `bff_rutaexpress` | 8080 | Puerta de entrada del frontend. Valida JWT y lo reenvía. Sin RabbitMQ. |
| `shipments_rutaexpress` | 8081 | **Productor**: publica comandos a `cmd.direct` al crear un envío o cambiar su estado. |
| `catalogo_rutaexpress` | 8082 | Productor (servicio creado) y consumidor de auditoría, con su DLQ. |
| `rabbitadmin_rutaexpress` | 8083 | Administrador REST: crea/elimina colas, exchanges y bindings. |
| `ms-rutaexpress-notify` (NUEVO) | 8084 | **Consumidor** de los 3 flujos: email, bodega y etiqueta PDF, con 3 DLQ. |

## Cómo ejecutar
1. `docker compose up -d` (RabbitMQ + panel en http://localhost:15672, guest/guest).
2. **Arranca primero `ms-rutaexpress-notify`**: él declara las colas y bindings. Si envíos publica antes, los mensajes se pierden porque aún no hay cola enlazada.
3. Luego los demás: `./mvnw spring-boot:run` en cada carpeta (con `AZURE_TENANT_ID` definido).

## Topología de notify (6 colas: 3 flujos + 3 DLQ)
| Cola principal | Propósito | DLQ | Binding direct | Binding topic |
|---|---|---|---|---|
| `q.cmd.email` | Email/push al destinatario (aceptado, en ruta, entregado) | `q.cmd.email.dlq` | `email.send` | `email.*` |
| `q.cmd.warehouse` | Ticket de picking / etiqueta de bodega | `q.cmd.warehouse.dlq` | `warehouse.ticket` | `warehouse.#` |
| `q.cmd.label` | Generación de PDF (guía de despacho o etiqueta) | `q.cmd.label.dlq` | `label.gen` | `label.*` |

Exchanges: `cmd.direct` (direct), `cmd.topic` (topic) y `cmd.dead.dlx` (direct, para las DLQ).
Cada cola principal tiene `x-dead-letter-exchange = cmd.dead.dlx` y como routing key el nombre de su DLQ.

## Qué publica envíos
- Envío creado → `warehouse.ticket` y `label.gen` (a `cmd.direct`).
- Cambio de estado → `email.send`, solo si el estado es ACEPTADO, EN_RUTA o ENTREGADO (lista en `app.messaging.email-statuses`).
- Si RabbitMQ está caído, solo se registra el error: guardar el envío no falla.
- Se publica a `cmd.direct`; `cmd.topic` queda disponible para publicar por patrón (p. ej. `warehouse.#`) sin duplicar entregas.

## Manejo de errores en notify (ACK manual)
- OK → `ack`. JSON malformado o dato inválido (email sin `@`, sin tracking, sin dirección) → `nack(requeue=false)` → DLQ.
- Error recuperable → hasta `app.messaging.max-retries` (3) reintentos con cabecera `x-retry-count`; agotados → DLQ.
- `DeadLetterConsumer` registra en logs (ERROR) todo mensaje que llega a una DLQ.

## Cómo demostrar el flujo (presentación)
1. **Flujo feliz:** panel RabbitMQ → *Exchanges → cmd.direct → Publish message*, routing key `email.send`, payload
   `{"eventType":"SHIPMENT_STATUS_CHANGED","shipmentId":1,"trackingNumber":"TRK-1","senderEmail":"a@b.cl","recipientAddress":"Calle 1","status":"ENTREGADO"}`
   → log `EMAIL enviado a a@b.cl…` en notify.
2. **DLQ por dato inválido:** mismo mensaje con `"senderEmail":"sin-arroba"` → `q.cmd.email.dlq` y log `[DLQ q.cmd.email.dlq]`.
3. **DLQ por JSON roto:** payload `esto no es json` → va a la DLQ de la cola que lo reciba.
4. **Por patrón:** exchange `cmd.topic`, routing key `warehouse.reprint` → llega a `q.cmd.warehouse` (binding `warehouse.#`).
5. **Flujo real:** con JWT válido, `POST /api/shipments` crea el envío → notify genera ticket y etiqueta; `PUT /api/shipments/{id}/status?status=EN_RUTA` → email.

## API del administrador (requiere `Authorization: Bearer <JWT>`)
```
POST   /api/v1/rabbit/queues            {"name":"demo.queue","deadLetterExchange":"cmd.dead.dlx","deadLetterRoutingKey":"demo.dead","messageTtlMs":60000}
GET    /api/v1/rabbit/queues/demo.queue
DELETE /api/v1/rabbit/queues/demo.queue
POST   /api/v1/rabbit/exchanges         {"name":"demo.ex","type":"TOPIC"}
DELETE /api/v1/rabbit/exchanges/demo.ex
POST   /api/v1/rabbit/bindings          {"queue":"demo.queue","exchange":"demo.ex","routingKey":"demo.#"}
DELETE /api/v1/rabbit/bindings?queue=demo.queue&exchange=demo.ex&routingKey=demo.#
```
Errores: 400 (validación / nombre reservado `amq.`), 404 (no existe), 409 (conflicto), 503 (RabbitMQ caído).

## Correcciones al código existente
- `SecurityConfig` (shipments y catalog): los matchers no tenían `/**`; ahora protegen todas las rutas.
- Catalog: no validaba JWT y el `issuer-uri` estaba en una clave incorrecta; ahora es resource server.
- BFF: `SecurityConfig,java` → `.java`; rutas `/api/v1/...` inexistentes en los microservicios; DTOs distintos; ahora valida y reenvía el JWT.
- Catalog: la carpeta `controller` tenía un carácter invisible en el nombre.

## Pendiente
- **Frontend**: el repo actual es React con Basic Auth. La EP3 pide Angular con login IDaaS (MSAL) y JWT en las llamadas.
- **BD cloud**: shipments/catalog siguen en H2 en memoria.
- Los consumidores de notify simulan el email, el ticket y el PDF con logs; falta la integración real si se exige.
