package com.techrocking.orchestrator.config;

import com.techrocking.orchestrator.listener.*;
import io.vertx.core.Vertx;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class InitializerConfig {

    private final ItemFetchedEventListener itemFetchedEventListener;
    private final OrderPlacedEventListener orderPlacedEventListener;
    private final DeliveryReceivedEventListener deliveryReceivedEventListener;
    private final DeliveryFailedEventListener deliveryFailedEventListener;
    private final ItemOutOfStockListener itemOutOfStockListener;

    public InitializerConfig(ItemFetchedEventListener itemFetchedEventListener, OrderPlacedEventListener orderPlacedEventListener, DeliveryReceivedEventListener deliveryReceivedEventListener, DeliveryFailedEventListener deliveryFailedEventListener, ItemOutOfStockListener itemOutOfStockListener) {
        this.itemFetchedEventListener = itemFetchedEventListener;
        this.orderPlacedEventListener = orderPlacedEventListener;
        this.deliveryReceivedEventListener = deliveryReceivedEventListener;
        this.deliveryFailedEventListener = deliveryFailedEventListener;
        this.itemOutOfStockListener = itemOutOfStockListener;
    }

    @PostConstruct
    public void init() {
        Vertx.vertx().deployVerticle(itemFetchedEventListener);
        Vertx.vertx().deployVerticle(orderPlacedEventListener);
        Vertx.vertx().deployVerticle(itemOutOfStockListener);
        Vertx.vertx().deployVerticle(deliveryReceivedEventListener);
        Vertx.vertx().deployVerticle(deliveryFailedEventListener);
//        Vertx.vertx().deployVerticle(paymentFailedEventListener);
    }
}
