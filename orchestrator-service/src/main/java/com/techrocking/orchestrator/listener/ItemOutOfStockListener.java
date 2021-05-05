package com.techrocking.orchestrator.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techrocking.orchestrator.kafka.message.ItemEvent;
import com.techrocking.orchestrator.rest.util.OrderRequest;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.kafka.client.consumer.KafkaConsumer;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

@Component
public class ItemOutOfStockListener extends AbstractVerticle {

    private KafkaConsumer<String, String> consumer;

	private static final Logger logger = LoggerFactory.getLogger(ItemOutOfStockListener.class);

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        configureConsumer();
        this.consumer.handler(record-> {
            try {
                ItemEvent itemEvent = Json.decodeValue(record.value(), ItemEvent.class);

                if (ItemEvent.Action.ITEMOUTOFSTOCK.equals(itemEvent.getAction())) {
                    logger.info("ItemOutOfStock event received for item id: " + itemEvent.getItemId());
                    logger.info("Going to call order service to compensate order for id " + itemEvent.getOrderId());

                    OrderRequest orderRequest = new OrderRequest();
                    orderRequest.setOrderId(itemEvent.getOrderId());
                    orderRequest.setProductCode(itemEvent.getProductCode());
                    KafkaProducer<String, JsonObject> producer = KafkaProducer.create(vertx, getKafkaConfig());
                    ObjectMapper mapper = new ObjectMapper();
                    String value = mapper.writeValueAsString(orderRequest);
                    KafkaProducerRecord<String, JsonObject> itemFetchedRecord = KafkaProducerRecord.create("order-not-placed-topic", new JsonObject(value));
                    itemFetchedRecord.headers().add(new KafkaHeaderImpl("key", UUID.randomUUID().toString()));
                    itemFetchedRecord.headers().add(new KafkaHeaderImpl("label", "ItemOutOfStockListener"));
                    producer.send(itemFetchedRecord, handler -> {
                        if (handler.succeeded()) {
                            System.err.println("item fetched record has been sent");
                        }
                    });

                }
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
    }

    private Properties getKafkaConfig(){
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
        consumer.subscribe("item-out-of-stock-topic", ar -> {
            if (ar.succeeded()) {
                System.out.println("Consumer subscribed");
            }
        });
        return config;
    }
}
