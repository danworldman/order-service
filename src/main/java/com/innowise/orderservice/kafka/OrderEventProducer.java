package com.innowise.orderservice.kafka;

import com.innowise.orderservice.model.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private static final String TOPIC = "order-events";
    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public void sendOrderCreatedEvent(OrderCreatedEvent event) {
        CompletableFuture<SendResult<String, OrderCreatedEvent>> future =
                kafkaTemplate.send(TOPIC, String.valueOf(event.orderId()), event);

        future.whenComplete((result, exception) -> {
            if (exception != null) {
                log.error("Failed to send CREATE_ORDER event for orderId={}", event.orderId(), exception);
            } else {
                log.info("Sent CREATE_ORDER event for orderId={}, offset={}",
                        event.orderId(), result.getRecordMetadata().offset());
            }
        });
    }
}