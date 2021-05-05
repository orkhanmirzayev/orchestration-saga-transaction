package com.techrocking.orchestrator.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techrocking.orchestrator.constants.OrderStatus;
import com.techrocking.orchestrator.entity.OrderOrchData;
import com.techrocking.orchestrator.kafka.message.ItemEvent;
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
public class ItemFetchedEventListener extends AbstractVerticle {

    private KafkaConsumer<String, String> consumer;

    private final MessageInfoUtil messageInfoUtil;

    private final MessageInfoService messageInfoService;

    private static final Logger logger = LoggerFactory.getLogger(ItemFetchedEventListener.class);

    private final OrderOrchService orderOrchService;

    public ItemFetchedEventListener(OrderOrchService orderOrchService, MessageInfoService messageInfoService, MessageInfoUtil messageInfoUtil) {
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
            ItemEvent itemFetchedMessage = Json.decodeValue(record.value(), ItemEvent.class);
            if (ItemEvent.Action.ITEMFETCHED.equals(itemFetchedMessage.getAction())) {
                logger.info("Received an ItemFetchedEvent for ITEM id: " + itemFetchedMessage.getItemId());

                if (itemFetchedMessage.getOrderId() != null) {
                    logger.info("Going to call payment service for order id : " + itemFetchedMessage.getOrderId());
                    try {
                        Optional<KafkaHeader> keyHeader = record.headers().stream().filter(h -> h.key().equals("key")).findFirst();
                        Optional<KafkaHeader> labelHeader = record.headers().stream().filter(h -> h.key().equals("label")).findFirst();
                        if (keyHeader.isPresent()) {
                            String uuid = keyHeader.get().value().toString();
                            String label = labelHeader.get().value().toString();
                            if (!messageInfoService.isMessageFound(uuid)) {
                                System.err.println("UUID: " + uuid + " not exists");
                                messageInfoUtil.createInfo(label, uuid);
                                System.err.println("message info created");
                                OrderOrchData ordOrchData = orderOrchService.findOrdOrchData(itemFetchedMessage.getOrderId());
                                System.err.println("ordOrchData is: " + ordOrchData);
                                if (ordOrchData.getStatus() == OrderStatus.PENDING) {
                                    ordOrchData.setStatus(OrderStatus.FIRST_CONFIRMED);
                                    orderOrchService.updateOrchData(ordOrchData);
                                    System.err.println(ordOrchData + "  is getting updated to FIRST_CONFIRMED");
                                } else if (ordOrchData.getStatus() == OrderStatus.FIRST_CONFIRMED) {
                                    System.err.println(ordOrchData + " status is FIRST_CONFIRMED");
                                    orderOrchService.removeOrchData(ordOrchData);
                                    System.err.println(ordOrchData + " has been removedD");
                                    OrderRequest orderRequest = new OrderRequest();
                                    orderRequest.setOrderId(itemFetchedMessage.getOrderId());
                                    orderRequest.setProductCode(itemFetchedMessage.getProductCode());
                                    KafkaProducer<String, JsonObject> producer = KafkaProducer.create(vertx, getKafkaConfig());
                                    ObjectMapper mapper = new ObjectMapper();
                                    String value = mapper.writeValueAsString(orderRequest);
                                    String itemKey = UUID.randomUUID().toString();
                                    KafkaProducerRecord<String, JsonObject> itemFetchedRecord = KafkaProducerRecord.create("confirmation-topic", new JsonObject(value));
                                    itemFetchedRecord.headers().add(new KafkaHeaderImpl("key", itemKey));
                                    itemFetchedRecord.headers().add(new KafkaHeaderImpl("label", "ItemFetchedEventListener"));
                                    producer.send(itemFetchedRecord, handler -> {
                                        if (handler.succeeded()) {
                                            System.err.println("item fetched record has been sent by ItemFetchedEventListener");
                                        }
                                    });
                                }


                            }
                        }
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }

                }
            }

        });
    }

    private Properties getKafkaConfig() {
        Properties config = new Properties();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonObjectSerializer.class);
        config.put(ProducerConfig.ACKS_CONFIG, "1");
        return config;
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
        consumer.subscribe("item-fetch-topic", ar -> {
            if (ar.succeeded()) {
                System.out.println("Consumer subscribed");
            }
        });
        return config;
    }


}
