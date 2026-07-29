package com.ciberaccion.orderservice.config;

import com.ciberaccion.orderservice.event.OrderCreatedEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, OrderCreatedEvent> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // Evita que el productor mande el nombre de clase completo (com.ciberaccion.orderservice.event.OrderCreatedEvent)
        // en los headers del mensaje. Como cada servicio tiene su propia copia de OrderCreatedEvent
        // en un paquete distinto, los consumidores no podrían resolver esa clase.
        // En su lugar, cada consumidor define explícitamente a qué clase deserializar (siguiente paso).
        configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}