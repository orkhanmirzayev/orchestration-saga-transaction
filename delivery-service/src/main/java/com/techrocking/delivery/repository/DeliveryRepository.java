package com.techrocking.delivery.repository;

import com.techrocking.delivery.model.DeliveryTicket;
import org.springframework.data.repository.CrudRepository;

public interface DeliveryRepository extends CrudRepository<DeliveryTicket,Long> {


    DeliveryTicket findByOrderId(Long orderId);
}
