package com.techrocking.orchestrator.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techrocking.orchestrator.constants.OrderStatus;
import com.techrocking.orchestrator.entity.OrderOrchData;
import com.techrocking.orchestrator.kafka.message.OrderEvent;
import com.techrocking.orchestrator.rest.util.OrderRequest;
import com.techrocking.orchestrator.service.MessageInfoService;
import com.techrocking.orchestrator.service.OrderOrchService;
import com.techrocking.orchestrator.util.MessageInfoUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import io.vertx.kafka.client.producer.KafkaHeader;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import io.vertx.kafka.client.producer.impl.KafkaHeaderImpl;
import io.vertx.kafka.client.serialization.JsonObjectSerializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class OrderPlacedEventListener extends AbstractVerticle {

    private final OrderOrchService orderOrchService;

    private final MessageInfoUtil messageUtil;
    private final MessageInfoService messageInfoService;
    private static final Logger logger = LoggerFactory.getLogger(OrderPlacedEventListener.class);

    private KafkaConsumer<String, String> consumer;
    private KafkaConsumer<String, String> orderCompletedConsumer;

    public OrderPlacedEventListener(OrderOrchService orderOrchService, MessageInfoService messageInfoService, MessageInfoUtil messageUtil) {
        this.orderOrchService = orderOrchService;
        this.messageInfoService = messageInfoService;
        this.messageUtil = messageUtil;
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        configureConsumer();
        this.consumer.handler(record -> {
            System.out.println("Processing key=" + record.key() + ",value=" + record.value() +
                    ",partition=" + record.partition() + ",offset=" + record.offset());
            OrderEvent orderEvent = Json.decodeValue(record.value(), OrderEvent.class);
            if (OrderEvent.OrderAction.ORDERPLACED.equals(orderEvent.getAction())) {
                logger.info("Received an OrderPlacedEvent for order id: " + orderEvent.getOrderId());
                logger.info("Going to call inventory service for order id : " + orderEvent.getOrderId());
                try {
                    Optional<KafkaHeader> keyHeader = record.headers().stream().filter(h -> h.key().equals("key")).findFirst();
                    if (keyHeader.isPresent()) {
                        String uuid = keyHeader.get().value().toString();
                        System.err.println("UUID is: "+uuid);
                        if (!messageInfoService.isMessageFound(uuid)) {
                            messageUtil.createInfo("OrderPlacedEventListener", uuid);

                            OrderOrchData orderOrchData = new OrderOrchData();
                            orderOrchData.setOrderId(orderEvent.getOrderId());
                            orderOrchData.setStatus(OrderStatus.PENDING);
                            orderOrchService.createOrderOrchData(orderOrchData);
                            System.err.println("orderOrchData has been created");

                            OrderRequest orderRequest = new OrderRequest();
                            orderRequest.setOrderId(orderEvent.getOrderId());
                            orderRequest.setProductCode(orderEvent.getProductCode());
                            orderRequest.setQuantity(orderEvent.getQuantity());
                            KafkaProducer<String, JsonObject> producer = KafkaProducer.create(vertx, getKafkaConfig());
                            ObjectMapper mapper = new ObjectMapper();
                            String value = mapper.writeValueAsString(orderRequest);
                            String inventoryKey = UUID.randomUUID().toString();
                            KafkaProducerRecord<String, JsonObject> inventoryRecord = KafkaProducerRecord.create("inventory-topic", new JsonObject(value));
                            inventoryRecord.headers().add(new KafkaHeaderImpl("key", inventoryKey));
                            inventoryRecord.headers().add(new KafkaHeaderImpl("label", "OrderPlacedEventListener"));
                            producer.send(inventoryRecord, handler -> {
                                if (handler.succeeded()) {
                                    System.err.println("inventory record has been sent by OrderPlacedEventListener");
                                }
                            });

                            String deliveryKey = UUID.randomUUID().toString();
                            KafkaProducerRecord<String, JsonObject> deliveryRecord = KafkaProducerRecord.create("delivery-topic", new JsonObject(value));
                            deliveryRecord.headers().add(new KafkaHeaderImpl("key", deliveryKey));
                            deliveryRecord.headers().add(new KafkaHeaderImpl("label", "OrderPlacedEventListener"));
                            producer.send(deliveryRecord, handler -> {
                                if (handler.succeeded()) {
                                    System.err.println("delivery record has been sent by OrderPlacedEventListener");
                                }
                            });

                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
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
        this.orderCompletedConsumer = KafkaConsumer.create(vertx, config);

        consumer.subscribe("order-place-topic", ar -> {
            if (ar.succeeded()) {
                System.out.println("Consumer subscribed");
            }
        });
        return config;
    }

    private Properties getKafkaConfig() {
        Properties config = new Properties();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonObjectSerializer.class);
        config.put(ProducerConfig.ACKS_CONFIG, "1");
        return config;
    }


}
