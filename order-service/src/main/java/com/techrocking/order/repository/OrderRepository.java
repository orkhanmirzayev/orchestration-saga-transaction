package com.techrocking.order.repository;

import com.techrocking.order.entity.Order;
import org.springframework.data.repository.CrudRepository;

public interface OrderRepository extends CrudRepository<Order,Long> {

}
