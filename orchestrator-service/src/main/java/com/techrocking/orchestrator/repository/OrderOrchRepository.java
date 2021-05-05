package com.techrocking.orchestrator.repository;

import com.techrocking.orchestrator.entity.OrderOrchData;
import org.springframework.data.repository.CrudRepository;

public interface OrderOrchRepository extends CrudRepository<OrderOrchData, Long> {

    OrderOrchData findOrchDataByOrderId(Long orderId);
}
