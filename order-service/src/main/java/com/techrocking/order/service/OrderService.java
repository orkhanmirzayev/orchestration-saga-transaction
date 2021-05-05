package com.techrocking.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.techrocking.order.constants.OrderStatus;
import com.techrocking.order.entity.Customer;
import com.techrocking.order.entity.Order;
import com.techrocking.order.entity.OrderItem;
import com.techrocking.order.kafka.source.OrderNotProcessedEventSource;
import com.techrocking.order.kafka.source.OrderPlacedEventSource;
import com.techrocking.order.payload.PlaceOrderRequest;
import com.techrocking.order.payload.PlaceOrderResponse;
import com.techrocking.order.repository.OrderItemRepository;
import com.techrocking.order.repository.OrderRepository;
import com.techrocking.order.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory
            .getLogger(OrderService.class);

    private final OrderRepository orderRepository;

    private final OrderPlacedEventSource orderPlacedEventSource;
    private final OrderNotProcessedEventSource orderNotProcessedEventSource;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderPlacedEventSource orderPlacedEventSource, OrderNotProcessedEventSource orderNotProcessedEventSource, OrderRepository orderRepository, OrderItemRepository orderItemRepository, ProductRepository productRepository) {
        this.orderPlacedEventSource = orderPlacedEventSource;
        this.orderNotProcessedEventSource = orderNotProcessedEventSource;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public PlaceOrderResponse createOrder(PlaceOrderRequest request, Customer customer) throws JsonProcessingException {
        Order order = new Order();
        order.setItemName("Mollers Omega 3");
        order.setDestination("Baku");
        order.setStatus(OrderStatus.PENDING);
        order.setCustomer(customer);

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setCode("73KBN");
        orderItem.setProduct(productRepository.findById(request.getProductId()).get());
        orderItem.setQuantity(request.getQuantity());

        orderItemRepository.save(orderItem);

        PlaceOrderResponse response = new PlaceOrderResponse();
        response.setMessage("order placed successfully");
        response.setOrderId(order.getId());
        response.setProductCode("73KBN");
        response.setQuantity(request.getQuantity());

        logger.info("going to place orderPlacedEvent for order :" + order.getId());
        orderPlacedEventSource.publishOrderEvent(response);
        return response;
    }

    public void compensateOrder(com.techrocking.order.payload.OrderRequest orderRequest) throws JsonProcessingException {
        /*delete order record for given order id from database*/
        System.err.println("Order service compansate order is working...");
        System.err.println("Order Id: " + orderRequest.getOrderId());
        Order order = orderRepository.findById(orderRequest.getOrderId()).get();
        System.err.println("Success after fetching order");
        order.setStatus(OrderStatus.FAILED);
		orderRepository.save(order);
        System.err.println("Order failed and updated...");
//		orderItemRepository.findAll().forEach(o->{
//			if(o.getCode().equals(orderRequest.getProductCode()) && o.getOrder().getId().intValue() == orderRequest.getOrderId().intValue() &&  order.getStatus() == OrderStatus.PENDING){
//				System.err.println("Orderitem found...");
//				order.setStatus(OrderStatus.FAILED);
//				o.setOrder(order);
//				orderItemRepository.save(o);
//			}
//		});

        /*publish OoderNotProcessedEvent*/
//		orderNotProcessedEventSource.publishOrderNotProcessedEvent(orderId);
    }

    public void completeOrder(com.techrocking.order.payload.OrderRequest orderRequest) {
        System.err.println("completeOrder will get executed");
        Order order = orderRepository.findById(orderRequest.getOrderId()).get();
        System.err.println("Success after fetching order: "+order);
        order.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);
        System.err.println("Order status updated");
//		System.err.println("Order failed and updated...");
//		orderItemRepository.findAll().forEach(o->{
//			if(o.getCode().equals(orderRequest.getProductCode()) && o.getOrder().getId().intValue() == orderRequest.getOrderId().intValue() && order.getStatus() == OrderStatus.PENDING){
//				order.setStatus(OrderStatus.COMPLETED);
//				o.setOrder(order);
//				orderItemRepository.save(o);
//			}
//		});


    }
}
