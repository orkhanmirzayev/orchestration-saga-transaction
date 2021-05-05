package com.techrocking.order.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.techrocking.order.entity.Customer;
import com.techrocking.order.payload.PlaceOrderRequest;
import com.techrocking.order.service.CustomerService;
import com.techrocking.order.service.MessageInfoService;
import com.techrocking.order.service.OrderService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.Json;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import io.vertx.kafka.client.producer.KafkaHeader;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class OrderVerticle extends AbstractVerticle {

    private final OrderService orderService;
    private final CustomerService customerService;
    private final MessageInfoService messageInfoService;
    private KafkaConsumer<String, String> consumer;
    private KafkaConsumer<String, String> orderItemConsumer;
    private KafkaConsumer<String, String> receivedOrderConsumer;
    private KafkaConsumer<String, String> orderConfirmationConsumer;

    public OrderVerticle(OrderService orderService, CustomerService customerService, MessageInfoService messageInfoService) {
        this.orderService = orderService;
        this.customerService = customerService;
        this.messageInfoService = messageInfoService;
    }

    @Override
    public void start(Promise<Void> startPromise) {
        configureConsumer();
        this.consumer.handler(rc -> {
            Optional<KafkaHeader> keyHeader = rc.headers().stream().filter(h -> h.key().equals("key")).findFirst();
            if (keyHeader.isPresent()) {
                String uuid = keyHeader.get().value().toString();
                if (!messageInfoService.isMessageFound(uuid)) {
                    com.techrocking.order.payload.OrderRequest orderRequest = Json.decodeValue(rc.value(), com.techrocking.order.payload.OrderRequest.class);
                    try {
                        orderService.compensateOrder(orderRequest);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                }

            }
        });

        this.receivedOrderConsumer.handler(rc -> {
            Optional<KafkaHeader> keyHeader = rc.headers().stream().filter(h -> h.key().equals("key")).findFirst();
            if (keyHeader.isPresent()) {
                String uuid = keyHeader.get().value().toString();
                if (!messageInfoService.isMessageFound(uuid)) {
                    PlaceOrderRequest request = Json.decodeValue(rc.value(), PlaceOrderRequest.class);
                    Customer customer = customerService.findCustomer(request.getCustomerId());
                    try {
                        orderService.createOrder(request, customer);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        this.orderConfirmationConsumer.handler(rc -> {
            Optional<KafkaHeader> keyHeader = rc.headers().stream().filter(h -> h.key().equals("key")).findFirst();
            if (keyHeader.isPresent()) {
                String uuid = keyHeader.get().value().toString();
                if (!messageInfoService.isMessageFound(uuid)) {
                    com.techrocking.order.payload.OrderRequest orderRequest = Json.decodeValue(rc.value(), com.techrocking.order.payload.OrderRequest.class);
                    orderService.completeOrder(orderRequest);
                }
            }
        });
    }

    private Map<String, String> configureConsumer() {
        Map<String, String> config = new HashMap<>();
        config.put("bootstrap.servers", "localhost:9092");
        config.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        config.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        config.put("group.id", "order_group");
        config.put("auto.offset.reset", "earliest");
        config.put("enable.auto.commit", "false");
        config.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, "6000");
        config.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "18000");
        config.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "6000");
        this.consumer = KafkaConsumer.create(vertx, config);
        this.receivedOrderConsumer = KafkaConsumer.create(vertx, config);
        this.orderConfirmationConsumer = KafkaConsumer.create(vertx, config);

        consumer.subscribe("order-not-placed-topic", ar -> {
            if (ar.succeeded()) {
                System.out.println("Consumer subscribed");
            }
        });
        receivedOrderConsumer.subscribe("receive-order-topic", ar -> {
            if (ar.succeeded()) {
                System.out.println("receivedOrderConsumer subscribed");
            }
        });

        orderConfirmationConsumer.subscribe("confirmation-topic", ar -> {
            if (ar.succeeded()) {
                System.out.println("Consumer subscribed");
            }
        });
        return config;
    }


}
