# RutaExpress – EP3 (RabbitMQ)

## Repositorios (un repo de GitHub por carpeta)
| Carpeta | Puerto | Rol en RabbitMQ |
|---|---|---|
| `shipments_rutaexpress` | 8081 | Productor (envío creado / estado cambiado). Consumidores: notificaciones y tickets. DLQ. |
| `catalogo_rutaexpress` | 8082 | Productor (servicio creado). Consumidor: auditoría. DLQ. |
| `rabbitadmin_rutaexpress` (NUEVO) | 8083 | Administrador: crea/elimina colas, exchanges y bindings por REST. |
| `bff_rutaexpress` | 8080 | Sin RabbitMQ. Corregido para que funcione con los microservicios. |

## Cómo ejecutar
1. `docker compose up -d` (RabbitMQ + panel en http://localhost:15672, guest/guest).
2. En cada microservicio: `./mvnw spring-boot:run` (con `AZURE_TENANT_ID` definido).
3. Al iniciar, shipments y catalog declaran solos sus exchanges, colas, bindings y DLQs (ver pestaña *Queues* del panel).

## Topología
- `rutaexpress.shipments.exchange` (topic) → `…notifications.queue` (`shipment.#`) y `…tickets.queue` (`shipment.created`)
- `rutaexpress.catalog.exchange` (topic) → `…audit.queue` (`catalog.#`)
- Cada cola principal tiene `x-dead-letter-exchange` → `…dlx` (direct) → su DLQ (`….dlq`).

## Manejo de errores (ACK manual)
- OK → `ack`. JSON malformado o dato inválido → `nack(requeue=false)` → DLQ.
- Error recuperable → hasta `app.messaging.max-retries` (3) reintentos con cabecera `x-retry-count`; agotados → DLQ.
- `DeadLetterConsumer` registra en logs (nivel ERROR) cada mensaje que llega a una DLQ.

## Cómo demostrar la DLQ (para la presentación)
En el panel de RabbitMQ: *Exchanges → rutaexpress.shipments.exchange → Publish message*, routing key `shipment.created`, payload
`{"eventType":"SHIPMENT_CREATED","shipmentId":1,"trackingNumber":"TRK-1","senderEmail":"sin-arroba","recipientAddress":"Calle 1","status":"PENDING"}`
→ la notificación falla (email inválido), pasa a `notifications.dlq` y aparece el log `[DLQ …]`. El ticket sí se genera.
Con payload `esto no es json` → ambas colas lo mandan a su DLQ.

## API del administrador (todas requieren `Authorization: Bearer <JWT>`)
```
POST   /api/v1/rabbit/queues            {"name":"demo.queue","deadLetterExchange":"demo.dlx","deadLetterRoutingKey":"demo.dead","messageTtlMs":60000}
GET    /api/v1/rabbit/queues/demo.queue
DELETE /api/v1/rabbit/queues/demo.queue
POST   /api/v1/rabbit/exchanges         {"name":"demo.ex","type":"TOPIC"}
DELETE /api/v1/rabbit/exchanges/demo.ex
POST   /api/v1/rabbit/bindings          {"queue":"demo.queue","exchange":"demo.ex","routingKey":"demo.#"}
DELETE /api/v1/rabbit/bindings?queue=demo.queue&exchange=demo.ex&routingKey=demo.#
```
Errores: 400 (validación / nombre reservado `amq.`), 404 (no existe), 409 (conflicto), 503 (RabbitMQ caído).

## Correcciones al código existente
- `SecurityConfig` (shipments y catalog): los matchers eran `"/api/shipments/"`, `"/h2-console/"` sin `/**`; ahora protegen todas las rutas.
- Catalog: no validaba JWT (`permitAll`) y la propiedad `issuer-uri` estaba en una clave incorrecta; ahora es resource server.
- BFF: archivo `SecurityConfig,java` (coma) → `.java`; rutas `/api/v1/...` no existían en los microservicios (son `/api/...`);
  DTOs distintos a los de los microservicios; ahora valida el JWT y lo reenvía a los microservicios.
- Catalog: carpeta `controller` tenía un carácter invisible en el nombre.

## Pendiente (no incluido aquí)
- **Frontend**: el repo actual es React con login básico (Basic Auth). La EP3 pide Angular con login IDaaS (MSAL) y JWT en las llamadas.
- **BD cloud**: shipments/catalog siguen en H2 en memoria (shipments ya trae el driver Oracle, falta configurar la conexión).
