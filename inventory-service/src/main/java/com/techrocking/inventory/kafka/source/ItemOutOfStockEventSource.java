package com.techrocking.inventory.kafka.source;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techrocking.inventory.controller.OrderRequest;
import com.techrocking.inventory.kafka.message.ItemEvent;
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
public class ItemOutOfStockEventSource {

    private Properties getKafkaConfig() {
        Properties config = new Properties();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonObjectSerializer.class);
        config.put(ProducerConfig.ACKS_CONFIG, "1");
        return config;
    }

	public void publishItemOutOfStockEvent(OrderRequest orderRequest) throws JsonProcessingException {
        KafkaProducer<String, JsonObject> producer = KafkaProducer.create(Vertx.vertx(), getKafkaConfig());
		ItemEvent message = new ItemEvent();
		message.setOrderId(orderRequest.getOrderId());
		message.setAction(ItemEvent.Action.ITEMOUTOFSTOCK);
		message.setProductCode(orderRequest.getProductCode());
        ObjectMapper mapper = new ObjectMapper();
        String value = mapper.writeValueAsString(message);
        KafkaProducerRecord<String, JsonObject> record = KafkaProducerRecord.create("item-out-of-stock-topic",new JsonObject(value));
        producer.send(record, done -> {
            if (done.succeeded()) {
                System.out.println("ItemOutOfStockEventSource OK");
            } else {
                Throwable t = done.cause();
            }
        });


	}

}
