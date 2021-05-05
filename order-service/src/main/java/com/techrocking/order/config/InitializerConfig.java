package com.techrocking.order.config;

import com.techrocking.order.controller.OrderVerticle;
import io.vertx.core.Vertx;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class InitializerConfig {

    private final OrderVerticle orderVerticle;

    public InitializerConfig(OrderVerticle orderVerticle) {
        this.orderVerticle = orderVerticle;
    }

    @PostConstruct
    public void init(){
        Vertx.vertx().deployVerticle(orderVerticle);
    }


}
