package com.techrocking.orchestrator.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techrocking.orchestrator.constants.OrderStatus;
import com.techrocking.orchestrator.entity.OrderOrchData;
import com.techrocking.orchestrator.kafka.message.DeliveryEvent;
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
public class DeliveryReceivedEventListener extends AbstractVerticle {

    private KafkaConsumer<String, String> consumer;

    private static final Logger logger = LoggerFactory.getLogger(DeliveryReceivedEventListener.class);

    private final OrderOrchService orderOrchService;

    private final MessageInfoService messageInfoService;

    private final MessageInfoUtil messageInfoUtil;

    public DeliveryReceivedEventListener(OrderOrchService orderOrchService, MessageInfoService messageInfoService, MessageInfoUtil messageInfoUtil) {
        this.orderOrchService = orderOrchService;
        this.messageInfoService = messageInfoService;
        this.messageInfoUtil = messageInfoUtil;
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        configureConsumer();
        this.consumer.handler(record -> {
            System.out.println("Processing key=" + record.key() + ",value=" + record.value() +
                    ",partition=" + record.partition() + ",offset=" + record.offset());
            DeliveryEvent deliveryReceivedMessage = Json.decodeValue(record.value(), DeliveryEvent.class);
            if (DeliveryEvent.DeliveryAction.DELIVERY_RECEIVED.equals(deliveryReceivedMessage.getAction())) {
                logger.info("Received an Delivery event for order id: " + deliveryReceivedMessage.getOrderId());

                Optional<KafkaHeader> keyHeader = record.headers().stream().filter(h -> h.key().equals("key")).findFirst();
                Optional<KafkaHeader> labelHeader = record.headers().stream().filter(h -> h.key().equals("label")).findFirst();
                if (keyHeader.isPresent()) {
                    String uuid = keyHeader.get().value().toString();
                    if (!messageInfoService.isMessageFound(uuid)) {
                        System.err.println("UUID: "+uuid+" not exists");
                        messageInfoUtil.createInfo(labelHeader.get().value().toString(),uuid);
                        System.err.println("message info created");
                        try {
                            OrderOrchData ordOrchData = orderOrchService.findOrdOrchData(deliveryReceivedMessage.getOrderId());
                            System.err.println("ordOrchData is: "+ordOrchData);
                            if (ordOrchData.getStatus() == OrderStatus.PENDING) {
                                ordOrchData.setStatus(OrderStatus.FIRST_CONFIRMED);
                                orderOrchService.updateOrchData(ordOrchData);
                                System.err.println(ordOrchData+"  is getting updated to FIRST_CONFIRMED");
                            } else if (ordOrchData.getStatus() == OrderStatus.FIRST_CONFIRMED) {
                                //remove orch data
                                System.err.println(ordOrchData+" status is FIRST_CONFIRMED");
                                orderOrchService.removeOrchData(ordOrchData);
                                System.err.println(ordOrchData+" has been removedD");
                                OrderRequest orderRequest = new OrderRequest();
                                orderRequest.setOrderId(deliveryReceivedMessage.getOrderId());
                                KafkaProducer<String, JsonObject> producer = KafkaProducer.create(vertx, getKafkaConfig());
                                ObjectMapper mapper = new ObjectMapper();
                                String value = mapper.writeValueAsString(orderRequest);
                                String inventoryKey = UUID.randomUUID().toString();
                                KafkaProducerRecord<String, JsonObject> inventoryRecord = KafkaProducerRecord.create("confirmation-topic", new JsonObject(value));
                                inventoryRecord.headers().add(new KafkaHeaderImpl("key",inventoryKey));
                                inventoryRecord.headers().add(new KafkaHeaderImpl("label","DeliveryReceivedEventListener"));
                                producer.send(inventoryRecord, handler -> {
                                    if (handler.succeeded()) {
                                        System.err.println("confirmation record has been sent by DeliveryReceivedEventListener");
                                    }
                                });
                            }


                        } catch (Exception e) {
                            e.printStackTrace();
                        }
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
        consumer.subscribe("delivery-received-topic", ar -> {
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
