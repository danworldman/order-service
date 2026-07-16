package com.innowise.orderservice.kafka;

import com.innowise.orderservice.dao.OrderDAO;
import com.innowise.orderservice.model.entity.OrderStatus;
import com.innowise.orderservice.model.event.PaymentCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_FAILED = "FAILED";

    private final OrderDAO orderDAO;

    @KafkaListener(
            topics = "payment-events",
            groupId = "order-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentCompletedEvent(PaymentCompletedEvent event) {
        orderDAO.findById(event.orderId()).ifPresentOrElse(order -> {
            if (order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.CANCELLED) {
                return;
            }

            if (STATUS_SUCCESS.equalsIgnoreCase(event.status())) {
                order.setStatus(OrderStatus.PAID);
                orderDAO.save(order);
            } else if (STATUS_FAILED.equalsIgnoreCase(event.status())) {
                order.setStatus(OrderStatus.CANCELLED);
                orderDAO.save(order);
            } else {
                log.warn("Unknown payment status '{}' for orderId={}, order left unchanged",
                        event.status(), event.orderId());
            }
        }, () -> log.warn("Received PAYMENT_COMPLETED event for unknown orderId={}", event.orderId()));
    }
}