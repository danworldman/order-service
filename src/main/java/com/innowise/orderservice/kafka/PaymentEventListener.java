package com.innowise.orderservice.kafka;

import com.innowise.orderservice.dao.OrderDAO;
import com.innowise.orderservice.model.event.PaymentCompletedEvent;
import com.innowise.orderservice.model.entity.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final OrderDAO orderDAO;

    @KafkaListener(
            topics = "payment-events",
            groupId = "order-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentCompletedEvent(PaymentCompletedEvent event) {
        orderDAO.findById(event.orderId())
                .ifPresent(order -> {
                    if ("SUCCESS".equalsIgnoreCase(event.status())) {
                        order.setStatus(OrderStatus.PAID);
                    } else {
                        order.setStatus(OrderStatus.CANCELLED);
                    }
                    orderDAO.save(order);
                });
    }
}