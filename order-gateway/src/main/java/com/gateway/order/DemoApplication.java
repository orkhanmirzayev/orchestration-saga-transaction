package com.gateway.order;

import com.gateway.order.start.Ordergateway;
import io.vertx.core.Vertx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Autowired
    private Ordergateway ordergateway;

    @PostConstruct
    public void init() {
        Vertx.vertx().deployVerticle(ordergateway);
    }
}
