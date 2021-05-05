package com.techrocking.order.kafka.source;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techrocking.order.kafka.message.OrderEvent;
import com.techrocking.order.util.MessageInfoUtil;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import io.vertx.kafka.client.serialization.JsonObjectSerializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class OrderNotProcessedEventSource {

    private final MessageInfoUtil messageInfoUtil;

    public OrderNotProcessedEventSource(MessageInfoUtil messageInfoUtil) {
        this.messageInfoUtil = messageInfoUtil;
    }


    public void publishOrderNotProcessedEvent(Long orderId) throws JsonProcessingException {
		OrderEvent message = new OrderEvent();
		message.setOrderId(orderId);
		message.setAction(OrderEvent.OrderAction.ORDERNOTPLACED);
        KafkaProducer<String, JsonObject> producer = KafkaProducer.create(Vertx.vertx(), getKafkaConfig());
        ObjectMapper mapper = new ObjectMapper();
        String value = mapper.writeValueAsString(message);
        KafkaProducerRecord<String, JsonObject> record = KafkaProducerRecord.create("order-not-processed-topic",new JsonObject(value));
        producer.send(record, done -> {
            if (done.succeeded()) {
//                messageInfoUtil.createInfo("OrderNotProcessedEventSource");
                System.out.println("message created");
            } else {
                Throwable t = done.cause();
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


}
