package com.techrocking.delivery.kafka.source;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techrocking.delivery.config.KafkaConfig;
import com.techrocking.delivery.kafka.message.DeliveryEvent;
import com.techrocking.delivery.util.MessageInfoUtil;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import io.vertx.kafka.client.producer.impl.KafkaHeaderImpl;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DeliveryReceivedSource {

    private final KafkaConfig kafkaConfig;

    private final MessageInfoUtil messageInfoUtil;

    public DeliveryReceivedSource(KafkaConfig kafkaConfig, MessageInfoUtil messageInfoUtil) {
        this.kafkaConfig = kafkaConfig;
        this.messageInfoUtil = messageInfoUtil;
    }

    public void publishDeliverySuccessEvent(Long orderId) {

        String key = UUID.randomUUID().toString();
        DeliveryEvent message = new DeliveryEvent();
        message.setOrderId(orderId);
        message.setAction(DeliveryEvent.DeliveryAction.DELIVERY_RECEIVED);
        ObjectMapper mapper = new ObjectMapper();
        try {
            String value = mapper.writeValueAsString(message);
            KafkaProducer<String, JsonObject> producer = KafkaProducer.create(Vertx.vertx(), kafkaConfig.getKafkaConfig());
            KafkaProducerRecord<String, JsonObject> record = KafkaProducerRecord.create("delivery-received-topic", new JsonObject(value));
            record.headers().add(new KafkaHeaderImpl("key", key));
            record.headers().add(new KafkaHeaderImpl("label", "DeliveryReceivedSource"));
            producer.send(record, done -> {
                if (done.succeeded()) {
                    System.out.println("");
                } else {
                    Throwable t = done.cause();
                }
            });
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

}
