package com.techrocking.delivery.controller;

import com.techrocking.delivery.constants.DeliveryStatus;
import com.techrocking.delivery.model.DeliveryTicket;
import com.techrocking.delivery.service.DeliveryService;
import com.techrocking.delivery.service.MessageInfoService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.Json;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import io.vertx.kafka.client.producer.KafkaHeader;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class DeliveryVerticle extends AbstractVerticle {

	private final DeliveryService deliveryService;
	private KafkaConsumer<String, String> consumer;
	private KafkaConsumer<String, String> deliveryConsumer;

	private final MessageInfoService messageInfoService;

	public DeliveryVerticle(DeliveryService deliveryService, MessageInfoService messageInfoService) {
		this.deliveryService = deliveryService;
		this.messageInfoService = messageInfoService;
	}

	@Override
	public void start(Promise<Void> startPromise) throws Exception {
		configureConsumer();
		this.consumer.handler(rc->{
			Optional<KafkaHeader> keyHeader = rc.headers().stream().filter(h -> h.key().equals("key")).findFirst();
			if (keyHeader.isPresent()) {
				String uuid = keyHeader.get().value().toString();
				if (!messageInfoService.isMessageFound(uuid)) {
					com.techrocking.delivery.controller.OrderRequest orderRequest = Json.decodeValue(rc.value(), com.techrocking.delivery.controller.OrderRequest.class);
					deliveryService.createDeliveryTicket(orderRequest);
				}
			}
		});

		this.deliveryConsumer.handler(rc->{
			Optional<KafkaHeader> keyHeader = rc.headers().stream().filter(h -> h.key().equals("key")).findFirst();
			if (keyHeader.isPresent()) {
				String uuid = keyHeader.get().value().toString();
				if (!messageInfoService.isMessageFound(uuid)) {
					System.err.println("Delivery consumer received a request");
					com.techrocking.delivery.controller.OrderRequest orderRequest = Json.decodeValue(rc.value(), com.techrocking.delivery.controller.OrderRequest.class);
					DeliveryTicket deliveryTicket = new DeliveryTicket();
					deliveryTicket.setOrderId(orderRequest.getOrderId());
					deliveryTicket.setDeliveryStatus(DeliveryStatus.COMPLETED);
					deliveryService.updateDeliveryTicketStatus(deliveryTicket);
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
		config.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, "60000");
		config.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "180000");
		config.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "120000");

		this.consumer = KafkaConsumer.create(vertx, config);
		this.deliveryConsumer = KafkaConsumer.create(vertx, config);

		consumer.subscribe("delivery-topic", ar -> {
			if (ar.succeeded()) {
				System.out.println("Consumer subscribed");
			}
		});
		deliveryConsumer.subscribe("confirmation-topic", ar -> {
			if (ar.succeeded()) {
				System.out.println("Consumer subscribed");
			}
		});


		return config;
	}

}
