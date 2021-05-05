package com.techrocking.inventory.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.techrocking.inventory.service.InventoryService;
import com.techrocking.inventory.service.MessageInfoService;
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
public class InventoryVerticle extends AbstractVerticle {

    private final InventoryService inventoryService;
    private KafkaConsumer<String, String> consumer;
    private final MessageInfoService messageInfoService;

    public InventoryVerticle(InventoryService inventoryService, MessageInfoService messageInfoService) {
        this.inventoryService = inventoryService;
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
                    OrderRequest orderRequest = Json.decodeValue(rc.value(), OrderRequest.class);
                    try {
                        inventoryService.fetchItem(orderRequest);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    private Map<String, String> configureConsumer() {
        Map<String, String> config = new HashMap<>();
        config.put("bootstrap.servers", "localhost:9092");
        config.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        config.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        config.put("group.id", "my_group");
        config.put("auto.offset.reset", "earliest");
        config.put("enable.auto.commit", "false");
        config.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, "6000");
        config.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "18000");
        config.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "6000");
        this.consumer = KafkaConsumer.create(vertx, config);
        consumer.subscribe("inventory-topic", ar -> {
            if (ar.succeeded()) {
                System.out.println("Consumer subscribed");
            }
        });
        return config;
    }
}
