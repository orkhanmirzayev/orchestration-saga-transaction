package com.techrocking.order.kafka.source;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techrocking.order.kafka.message.OrderEvent;
import com.techrocking.order.payload.PlaceOrderResponse;
import com.techrocking.order.service.MessageInfoService;
import com.techrocking.order.util.MessageInfoUtil;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import io.vertx.kafka.client.producer.impl.KafkaHeaderImpl;
import io.vertx.kafka.client.serialization.JsonObjectSerializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.stereotype.Component;

import java.util.Properties;
import java.util.UUID;

@Component
public class OrderPlacedEventSource {

	private final MessageInfoService messageInfoService;

	private final MessageInfoUtil messageInfoUtil;

	public OrderPlacedEventSource(MessageInfoService messageInfoService, MessageInfoUtil messageInfoUtil) {
		this.messageInfoService = messageInfoService;
		this.messageInfoUtil = messageInfoUtil;
	}

	public void publishOrderEvent(PlaceOrderResponse orderResponse) throws JsonProcessingException {
		KafkaProducer<String, JsonObject> producer = KafkaProducer.create(Vertx.vertx(), getKafkaConfig());
		OrderEvent message = new OrderEvent();
		message.setOrderId(orderResponse.getOrderId());
		message.setProductCode(orderResponse.getProductCode());
		message.setQuantity(orderResponse.getQuantity());
		message.setAction(OrderEvent.OrderAction.ORDERPLACED);

		ObjectMapper mapper = new ObjectMapper();
		String value = mapper.writeValueAsString(message);
		String key = UUID.randomUUID().toString();
		KafkaProducerRecord<String, JsonObject> record = KafkaProducerRecord.create("order-place-topic",new JsonObject(value));
		record.headers().add(new KafkaHeaderImpl("key", key));
		record.headers().add(new KafkaHeaderImpl("label", "OrderPlacedEventSource"));
		producer.send(record, done -> {
			if (done.succeeded()) {
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
