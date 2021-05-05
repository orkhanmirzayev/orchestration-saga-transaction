package com.techrocking.inventory.config;

import com.techrocking.inventory.controller.InventoryVerticle;
import io.vertx.core.Vertx;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class InitializerConfig {

    private final InventoryVerticle inventoryVerticle;

    public InitializerConfig(InventoryVerticle inventoryVerticle) {
        this.inventoryVerticle = inventoryVerticle;
    }

    @PostConstruct
    public void init(){

        Vertx.vertx().deployVerticle(inventoryVerticle);
    }
}
