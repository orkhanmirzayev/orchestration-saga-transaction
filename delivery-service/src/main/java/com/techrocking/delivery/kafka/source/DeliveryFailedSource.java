package com.techrocking.delivery.kafka.source;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techrocking.delivery.kafka.message.DeliveryEvent;
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
public class DeliveryFailedSource {

	private Properties getKafkaConfig() {
		Properties config = new Properties();
		config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonObjectSerializer.class);
		config.put(ProducerConfig.ACKS_CONFIG, "1");
		return config;
	}

	public void publishPaymentFailedEvent(Long orderId) {

		DeliveryEvent message = new DeliveryEvent();
		message.setOrderId(orderId);
		message.setAction(DeliveryEvent.DeliveryAction.DELIVERY_NOT_RECEIVED);
		ObjectMapper mapper = new ObjectMapper();
		try {
			String value = mapper.writeValueAsString(message);
			KafkaProducer<String, JsonObject> producer = KafkaProducer.create(Vertx.vertx(), getKafkaConfig());
			KafkaProducerRecord<String, JsonObject> record = KafkaProducerRecord.create("delivery-not-received-topic",new JsonObject(value));
			producer.send(record, done -> {
				if (done.succeeded()) {
					System.out.println("PaymentReceivedSource NOT OK");
				} else {
					Throwable t = done.cause();
				}
			});
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

	}

}
