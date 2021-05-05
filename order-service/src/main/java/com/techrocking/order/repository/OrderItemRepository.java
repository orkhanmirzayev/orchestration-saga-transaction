package com.techrocking.order.repository;

import com.techrocking.order.entity.OrderItem;
import org.springframework.data.repository.CrudRepository;

public interface OrderItemRepository extends CrudRepository<OrderItem,Long> {

    OrderItem findByCodeAndOrder(String code,Long orderId);
}
