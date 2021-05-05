package com.techrocking.delivery.config;

import com.techrocking.delivery.controller.DeliveryVerticle;
import io.vertx.core.Vertx;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class InitializerConfig {

    private final DeliveryVerticle deliveryVerticle;

    public InitializerConfig(DeliveryVerticle deliveryVerticle) {
        this.deliveryVerticle = deliveryVerticle;
    }

    @PostConstruct
    public void init(){
        Vertx.vertx().deployVerticle(deliveryVerticle);
    }
}
