package com.techrocking.orchestrator.service;

import com.techrocking.orchestrator.entity.OrderOrchData;
import com.techrocking.orchestrator.repository.OrderOrchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderOrchService {

    private final OrderOrchRepository orderOrchRepository;

    public OrderOrchService(OrderOrchRepository orderOrchRepository) {
        this.orderOrchRepository = orderOrchRepository;
    }

    @Transactional
    public void createOrderOrchData(OrderOrchData orderOrchData){
        orderOrchRepository.save(orderOrchData);
    }

    @Transactional(readOnly = true)
    public OrderOrchData findOrdOrchData(Long orderId){
        return orderOrchRepository.findOrchDataByOrderId(orderId);
    }

    @Transactional
    public void updateOrchData(OrderOrchData orderOrchData){
        orderOrchRepository.save(orderOrchData);
    }

    @Transactional
    public void removeOrchData(OrderOrchData orderOrchData){
        orderOrchRepository.delete(orderOrchData);
    }

}
