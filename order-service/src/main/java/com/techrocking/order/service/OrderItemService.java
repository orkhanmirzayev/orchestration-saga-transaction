package com.techrocking.order.service;

import com.techrocking.order.entity.OrderItem;
import com.techrocking.order.repository.OrderItemRepository;
import org.springframework.stereotype.Service;

@Service
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;

    public OrderItemService(OrderItemRepository orderItemRepository) {
        this.orderItemRepository = orderItemRepository;
    }

    public OrderItem findOrderItem(String code, Long orderId){
        return orderItemRepository.findByCodeAndOrder(code,orderId);
    }
}
