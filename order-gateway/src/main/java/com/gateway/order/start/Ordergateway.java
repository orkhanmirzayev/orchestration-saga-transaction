package com.gateway.order.start;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.order.request.PlaceOrderRequest;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
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
public class Ordergateway extends AbstractVerticle {

    private Router router() {
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.post("/order").handler(rc -> {
            PlaceOrderRequest request = Json.decodeValue(rc.getBodyAsString(), PlaceOrderRequest.class);
            KafkaProducer<String, JsonObject> producer = KafkaProducer.create(vertx, getKafkaConfig());
            ObjectMapper mapper = new ObjectMapper();
            String value = null;
            try {
                value = mapper.writeValueAsString(request);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            String key = UUID.randomUUID().toString();
            KafkaProducerRecord<String, JsonObject> receiveOrderRecord = KafkaProducerRecord.create("receive-order-topic", new JsonObject(value));
            receiveOrderRecord.headers().add(new KafkaHeaderImpl("key",key));
            receiveOrderRecord.headers().add(new KafkaHeaderImpl("label","Ordergateway"));
            producer.send(receiveOrderRecord, handler -> {
                if (handler.succeeded()) {
                    System.out.println("order record has been sent from gateway");
                }
            });
            rc.response().end("ok");
//            KafkaProducerRecord<String, JsonObject> deliveryRecord = KafkaProducerRecord.create("delivery-topic", new JsonObject(value));
//            producer.send(deliveryRecord, handler -> {
//                if (handler.succeeded()) {
//                    System.out.println("order record has been sent from gateway");
//                }
//            });
//            rc.response().end("ok");


        });

        return router;
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        vertx.createHttpServer().requestHandler(router()).listen(8087);

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
