# kafka-learning-project

Proyecto de aprendizaje para practicar **Apache Kafka con Spring Boot**, simulando un sistema de eventos de pedidos de una tienda online.

## Objetivo

Entender los fundamentos de Kafka (productores, consumidores, consumer groups, topics) a través de un ejemplo canónico y simple, antes de evolucionar hacia un proyecto más funcional.

## Arquitectura

Un `order-service` publica eventos `OrderCreatedEvent` en un topic de Kafka. Dos consumidores independientes (`notification-service` e `inventory-service`) escuchan ese mismo evento, cada uno con su propio `group-id`, para reaccionar de forma desacoplada.

```mermaid
flowchart LR
    Client([Cliente]) -->|POST /orders| OS[order-service<br/>puerto 8081]
    OS -->|publica<br/>OrderCreatedEvent| Topic{{"order-events<br/>(topic)"}}
    Topic -->|consumer group:<br/>notification-group| NS[notification-service<br/>puerto 8082]
    Topic -->|consumer group:<br/>inventory-group| IS[inventory-service<br/>puerto 8083]
    OS -.persiste.-> DB[(H2)]

    NS -->|envía notificación| Notify[/Notificación al cliente/]
    IS -->|descuenta stock| Stock[(Inventario)]
```

- **`order-service`** (productor): dueño del dominio "pedidos". Crea el pedido y publica el evento.
- **`notification-service`** (consumidor): decide qué notificar al cliente cuando se crea un pedido.
- **`inventory-service`** (consumidor): descuenta stock cuando llega un evento de pedido creado.

### Topic

```
order-events
```

## Estructura del repo

Este es un **monorepo**, pero **no** un proyecto Maven multi-módulo. Cada servicio es un proyecto Spring Boot completamente independiente (su propio `pom.xml`, `mvnw`, `application.properties`), generado tal cual desde [start.spring.io](https://start.spring.io). Lo único que comparten es el espacio en el repositorio, no el build ni clases Java — la comunicación entre ellos es exclusivamente a través de eventos de Kafka.

```
kafka-learning-project/
├── docker-compose.yml
├── README.md
├── order-service/
│   ├── pom.xml
│   └── src/main/resources/application.properties
├── notification-service/
│   ├── pom.xml
│   └── src/main/resources/application.properties
└── inventory-service/
    ├── pom.xml
    └── src/main/resources/application.properties
```

## Stack

- Java 17
- Spring Boot 3.5.13
- Spring for Apache Kafka (`spring-kafka`)
- Lombok
- H2 (solo en `order-service`, para persistir pedidos)
- Docker / Docker Compose (Kafka en modo **KRaft**, sin Zookeeper)
- Kafka UI (para inspeccionar topics y mensajes)

GroupId común: `com.ciberaccion`

## Servicios y puertos

| Servicio              | Rol        | Puerto | group-id            |
|-----------------------|------------|--------|----------------------|
| `order-service`       | Productor  | 8081   | —                    |
| `notification-service`| Consumidor | 8082   | `notification-group` |
| `inventory-service`   | Consumidor | 8083   | `inventory-group`    |

## Levantar el entorno

### 1. Infraestructura (Kafka + Kafka UI)

Desde la raíz del repo:

```bash
docker-compose up -d
```

Verifica que el broker esté sano:

```bash
docker-compose ps
```

Kafka UI queda disponible en `http://localhost:8085` (se dejó el 8080 libre a propósito).

### 2. Cada servicio

Desde la carpeta de cada servicio:

```bash
./mvnw spring-boot:run
```

Los tres se conectan a Kafka en `localhost:9092`. Los topics se crean automáticamente al primer uso (`auto-create-topics` habilitado).

## Estado actual

- [x] Estructura del monorepo con los tres servicios independientes
- [x] `pom.xml` corregido a Spring Boot 3.5.13 en los tres servicios
- [x] `docker-compose.yml` con Kafka (KRaft) + Kafka UI
- [x] Puertos y `application.properties` corregidos
- [x] Modelo de dominio (`Order`, `OrderStatus`) y evento (`OrderCreatedEvent`) separados
- [x] `OrderRepository`, `OrderEventMapper`, `OrderService` en `order-service`
- [x] Productor Kafka configurado (`KafkaProducerConfig`) y probado
- [x] Endpoint REST (`OrderController`) para crear/consultar pedidos
- [x] Consumer en `notification-service` (`OrderNotificationListener`)
- [x] Consumer en `inventory-service` (`OrderInventoryListener` + `StockService` en memoria)
- [x] Flujo completo probado end-to-end: order-service → Kafka → notification-service + inventory-service
- [x] Script `docs/restart-all.sh` para reiniciar todo el entorno de un solo comando
- [x] H2 console en `order-service` funcionando (`localhost:8081/h2-console`, JDBC URL `jdbc:h2:mem:orderdb`)
- [x] Manejo de errores: retry con backoff exponencial + Dead Letter Topic (`order-events.DLT`) en `inventory-service`
- [ ] `inventory-service`: mover el stock de memoria a persistencia real (JPA/H2)
- [ ] Múltiples particiones + consumer scaling (varias instancias del mismo servicio compartiendo particiones)
- [ ] Tests de integración con Testcontainers (Kafka real en un contenedor efímero para los tests)

## Pendientes conocidos

## Notas de diseño

- Se optó por monorepo con proyectos independientes (no multi-módulo Maven) para preservar el desacoplamiento real entre servicios — evita la tentación de compartir clases Java directamente en vez de comunicarse por eventos, que es justo lo que se busca practicar.
- Cada consumidor usa un `group-id` distinto para que ambos reciban el mismo evento de forma independiente (broadcast entre consumer groups).

## Bitácora de aprendizaje

Notas puntuales de lo que fue saliendo durante la práctica — dudas reales, bugs reales, y lo que dejaron enseñando. Pensada para releerla en el futuro sin tener que repetir el proceso.

### Fundamentos de Kafka

- **Modelo de dominio vs. evento no son lo mismo.** `Order` (entidad JPA, interna a `order-service`) y `OrderCreatedEvent` (el contrato que viaja por Kafka) se mantienen como clases separadas a propósito: evolucionan a ritmos distintos, y el evento es un contrato del que dependen otros servicios — cambiarlo a la ligera rompe consumidores.
- **`KafkaTemplate` vs `@KafkaListener`**: ambos leen su config base de `application.properties`, pero el `KafkaTemplate` se declaró explícito con `@Configuration` (en vez de dejar que Spring Boot lo autoconfigure) para tener los tipos genéricos exactos (`String`, `OrderCreatedEvent`) en vez del `KafkaTemplate<Object, Object>` genérico por defecto.
- **No hay particiones ni paralelismo real.** El topic `order-events` quedó con 1 sola partición (default de `KAFKA_AUTO_CREATE_TOPICS_ENABLE`). Con 1 partición, un mismo consumer group no puede paralelizar consumo entre varias instancias — tema pendiente en el checklist.
- **El consumer hace *polling*, Kafka no hace *push*.** Internamente hay un hilo dedicado llamando `consumer.poll()` en loop contra el broker. Por eso el consumo se siente "casi instantáneo" — el polling es muy frecuente, no porque Kafka empuje mensajes activamente.
- **Offset commit ≠ idempotencia — son dos garantías distintas.** El commit del offset es lo que le dice a Kafka "el group-id X ya procesó hasta acá" (evita reprocesar en el *consumer*). La idempotencia del productor (`ProducerId set to N with epoch 0`, visible en los logs) evita que un mismo mensaje se duplique si el *productor* reintenta un envío por timeout de red. Resuelven problemas distintos y son complementarios.
- **`__consumer_offsets`** es el topic interno donde Kafka guarda esos offsets commiteados por cada group-id; por eso Kafka UI no deja purgarlo ni borrarlo manualmente — es infraestructura del cluster, no datos de la app.

### Bugs reales y cómo se rastrearon

- **`docker-compose down` no borra los volúmenes.** Un mensaje viejo de pruebas anteriores seguía apareciendo después de "reiniciar todo" — la causa era que el volumen `kafka-data` sobrevive al `down`. Se resolvió usando `docker-compose down -v`.
- **Mapeo de puertos apuntando al listener interno equivocado.** `order-service` fallaba con `UnknownHostException: kafka` al publicar — el `docker-compose.yml` mapeaba `9092:9092` (el listener interno, que se anuncia como `kafka:9092`) en vez de `9092:29092` (el listener `PLAINTEXT_HOST`, pensado para acceso externo). Dos listeners con propósitos distintos, y el mapeo de puertos tiene que respetar cuál es cuál.
- **H2 console devolvía 404 con la configuración aparentemente correcta.** El diagnóstico fue largo: se descartó falta de reinicio, build desactualizado, archivos duplicados, variables de entorno, `.mvn/jvm.config`... hasta que un `CommandLineRunner` recorriendo las `PropertySource` reveló la causa real: la propiedad `spring.h2.console.enabled=true` tenía **espacios en blanco al final** en el archivo de properties, y `@ConditionalOnBooleanProperty` la comparaba como string exacto — `"true  "` no matcheaba `"true"`. Lección: cuando la config "se ve bien" pero la condición dice que no matchea, hay que mirar el valor byte a byte, no solo a simple vista.
- **`inventory-service` no consumía nada, sin logs ni errores.** La causa: el archivo `OrderInventoryListener.java` existía en el proyecto, pero la clase estaba vacía — nunca se le agregó el método con `@KafkaListener`. Sin ese método, no había bean de listener, no había conexión a Kafka, y por eso no aparecía ni siquiera el log de `partitions assigned`.
- **Loop infinito en el mecanismo de retry + DLT.** Al forzar un fallo de stock insuficiente, el error handler agotaba los reintentos pero fallaba también al intentar publicar en el Dead Letter Topic, y ese segundo fallo hacía que el mensaje nunca se marcara como "recuperado" — volvía a reintentar desde cero indefinidamente. La causa raíz: faltaba el archivo `KafkaProducerConfig.java` en `inventory-service` (nunca se copió al proyecto), así que Spring Boot autoconfiguraba su propio `KafkaTemplate` genérico con `StringSerializer` por defecto — y ese no podía serializar un objeto `OrderCreatedEvent`, solo strings.
- **Purgar un topic mientras un mensaje está en medio de sus reintentos lo puede perder sin que llegue al DLT.** Se observó en vivo: al purgar `order-events` con un mensaje todavía reintentando, su offset dejó de existir y el consumer simplemente lo saltó al hacer `resetting offset`. Lección práctica: purgar con los servicios detenidos si hay algo en proceso de retry.

### Patrones y decisiones de diseño

- **Retry con backoff exponencial + Dead Letter Topic**, usando `DefaultErrorHandler` + `ExponentialBackOff` + `DeadLetterPublishingRecoverer` de Spring Kafka. Reintentos espaciados (1s, 2s, 4s...) para no bombardear el sistema si el fallo es transitorio; después de agotar el tiempo, el mensaje se publica en `order-events.DLT` en vez de bloquear la partición para siempre.
- **Excepción de dominio real en vez de un gatillo artificial para probar el DLT.** La primera versión usaba un producto especial (`"FORZAR_ERROR"`) como truco para forzar el fallo. Se reemplazó por una `InsufficientStockException` real, lanzada cuando el stock no alcanza — más honesto y más representativo de cómo fallaría en un caso real.
- **`inventory-service` necesitó su propio productor Kafka**, aunque conceptualmente "solo consume" — porque publicar en el DLT es, técnicamente, producir un mensaje. Un consumer que hace DLT recovery necesita un `ProducerFactory`/`KafkaTemplate` propio.

### Herramientas y detalles del día a día

- `spring.h2.console.enabled`, revisar espacios en blanco invisibles al final de las líneas en `.properties`.
- `docker-compose down -v` para reset completo; purge de topics en Kafka UI para limpieza rápida sin tocar Docker.
- `logging.level.org.apache.kafka=WARN` para silenciar el volcado completo de `ProducerConfig`/`ConsumerConfig` que Kafka loggea en `INFO` en cada arranque.
- Errores `NOT_COORDINATOR` justo después de un `docker-compose up` recién hecho son transitorios — el topic interno `__consumer_offsets` tarda un momento en estabilizar su coordinador tras un arranque limpio; el cliente los reintenta solo.
- Diferencia entre `debug=true` (activa el `CONDITIONS EVALUATION REPORT` de autoconfiguración) y un `CommandLineRunner` con `ConfigurableEnvironment`/`PropertySource` (para rastrear exactamente qué fuente de configuración está ganando cuando hay dudas de precedencia).
