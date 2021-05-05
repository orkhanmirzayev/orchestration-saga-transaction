package com.techrocking.orchestrator.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techrocking.orchestrator.kafka.message.DeliveryEvent;
import com.techrocking.orchestrator.rest.util.OrderRequest;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
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

@Component
public class DeliveryFailedEventListener extends AbstractVerticle {


    private KafkaConsumer<String, String> consumer;

    private static final Logger logger = LoggerFactory.getLogger(DeliveryFailedEventListener.class);

    @Override
    public void start(Promise<Void> startPromise) {
        configureConsumer();
        this.consumer.handler(record -> {
            System.out.println("Processing key=" + record.key() + ",value=" + record.value() +
                    ",partition=" + record.partition() + ",offset=" + record.offset());
            DeliveryEvent paymentFailedMessage = Json.decodeValue(record.value(), DeliveryEvent.class);
            if (DeliveryEvent.DeliveryAction.DELIVERY_NOT_RECEIVED.equals(paymentFailedMessage.getAction())) {
                logger.info("Received an PaymentFailedEvent for order id: " + paymentFailedMessage.getOrderId());
                try {
                    if (paymentFailedMessage.getOrderId() != null) {
                        OrderRequest orderRequest = new OrderRequest();
                        orderRequest.setOrderId(paymentFailedMessage.getOrderId());
                        KafkaProducer<String, JsonObject> producer = KafkaProducer.create(vertx, getKafkaConfig());
                        ObjectMapper mapper = new ObjectMapper();
                        String value = mapper.writeValueAsString(orderRequest);
                        KafkaProducerRecord<String, JsonObject> inventoryRecord = KafkaProducerRecord.create("delivery-not-received-topic", new JsonObject(value));
                        producer.send(inventoryRecord, handler -> {
                            if (handler.succeeded()) {
                                System.out.println("inventory record has been sent");
                            }
                        });
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
        consumer.subscribe("payment-received-topic", ar -> {
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
