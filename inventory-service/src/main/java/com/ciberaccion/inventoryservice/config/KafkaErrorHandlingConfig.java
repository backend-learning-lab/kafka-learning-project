package com.ciberaccion.inventoryservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;
import org.apache.kafka.common.TopicPartition;

@Configuration
public class KafkaErrorHandlingConfig {

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<Object, Object> dltKafkaTemplate) {
 
        // A dónde mandar el mensaje que no se pudo procesar: mismo nombre de topic + ".DLT",
        // conservando la misma partición de origen.
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                dltKafkaTemplate,
                (record, exception) -> new TopicPartition(record.topic() + ".DLT", record.partition())
        );
 
        // Backoff exponencial: 1er reintento a 1s, luego 2s, 4s... con tope de 10s totales.
        ExponentialBackOff backOff = new ExponentialBackOff(1_000L, 2.0);
        backOff.setMaxElapsedTime(10_000L);
 
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);
 
        errorHandler.setRetryListeners((record, ex, deliveryAttempt) ->
                System.out.printf(
                        "[RETRY] Intento #%d para el mensaje con key=%s (motivo: %s)%n",
                        deliveryAttempt, record.key(), ex.getMessage())
        );
 
        return errorHandler;
    }    

}
