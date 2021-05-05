package com.techrocking.inventory.kafka.source;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techrocking.inventory.config.KafkaConfig;
import com.techrocking.inventory.controller.OrderRequest;
import com.techrocking.inventory.kafka.message.ItemEvent;
import com.techrocking.inventory.util.MessageInfoUtil;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import io.vertx.kafka.client.producer.impl.KafkaHeaderImpl;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ItemFetchedEventSource {


	private final MessageInfoUtil messageInfoUtil;

	private final KafkaConfig kafkaConfig;

	public ItemFetchedEventSource(MessageInfoUtil messageInfoUtil, KafkaConfig kafkaConfig) {
		this.messageInfoUtil = messageInfoUtil;
		this.kafkaConfig = kafkaConfig;
	}


	public void publishItemFetchedEvent(OrderRequest orderRequest) throws JsonProcessingException {
		KafkaProducer<String, JsonObject> producer = KafkaProducer.create(Vertx.vertx(), kafkaConfig.getKafkaConfig());
		ItemEvent message = new ItemEvent();
		message.setProductCode(orderRequest.getProductCode());
		message.setOrderId(orderRequest.getOrderId());
		message.setAction(ItemEvent.Action.ITEMFETCHED);
		ObjectMapper mapper = new ObjectMapper();
		String value = mapper.writeValueAsString(message);
		String key = UUID.randomUUID().toString();
		KafkaProducerRecord<String, JsonObject> record = KafkaProducerRecord.create("item-fetch-topic",new JsonObject(value));
		record.headers().add(new KafkaHeaderImpl("key", key));
		record.headers().add(new KafkaHeaderImpl("label", "ItemFetchedEventSource"));

		producer.send(record, done -> {
			if (done.succeeded()) {
				System.out.println("");
			} else {
				Throwable t = done.cause();
			}
		});

	}


}
