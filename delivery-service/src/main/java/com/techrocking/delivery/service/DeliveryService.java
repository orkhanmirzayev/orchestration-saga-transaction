package com.techrocking.delivery.service;

import com.techrocking.delivery.constants.DeliveryStatus;
import com.techrocking.delivery.controller.OrderRequest;
import com.techrocking.delivery.kafka.source.DeliveryFailedSource;
import com.techrocking.delivery.kafka.source.DeliveryReceivedSource;
import com.techrocking.delivery.model.DeliveryTicket;
import com.techrocking.delivery.repository.DeliveryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DeliveryService {

    private static final Logger logger = LoggerFactory.getLogger(DeliveryService.class);

    private final DeliveryReceivedSource deliveryReceivedSource;

    private final DeliveryFailedSource deliveryFailedSource;

    private final DeliveryRepository deliveryRepository;

    public DeliveryService(DeliveryFailedSource deliveryFailedSource, DeliveryReceivedSource deliveryReceivedSource, DeliveryRepository deliveryRepository) {
        this.deliveryFailedSource = deliveryFailedSource;
        this.deliveryReceivedSource = deliveryReceivedSource;
        this.deliveryRepository = deliveryRepository;
    }
    public void findDeliveryTicketByOrderIdAndStatus(DeliveryTicket deliveryTicket) {
        deliveryRepository.save(deliveryTicket);
    }
    public void updateDeliveryTicketStatus(DeliveryTicket deliveryTicket) {
        DeliveryTicket deliveryTicketUpdate = deliveryRepository.findByOrderId(deliveryTicket.getOrderId());
        System.err.println("deliveryTicketUpdate: "+deliveryTicketUpdate);
        if(deliveryTicketUpdate.getDeliveryStatus() == DeliveryStatus.PENDING){
            System.err.println("deliveryTicketUpdate status will get updated to COMPLETED");
            deliveryTicketUpdate.setDeliveryStatus(deliveryTicket.getDeliveryStatus());
            System.err.println("deliveryTicketUpdate latest: "+deliveryTicketUpdate);
            deliveryRepository.save(deliveryTicketUpdate);
//            deliveryReceivedSource.publishOrderCompletedEvent(orderRequest.getOrderId());
        }

        //todo :: publish update success event to orchestration
    }

    public void createDeliveryTicket(OrderRequest orderRequest) {
        DeliveryTicket deliveryTicket = new DeliveryTicket();
        deliveryTicket.setDeliveryStatus(DeliveryStatus.PENDING);
        deliveryTicket.setDestination("destination");
        deliveryTicket.setOrderId(orderRequest.getOrderId());
        deliveryRepository.save(deliveryTicket);

        deliveryReceivedSource.publishDeliverySuccessEvent(orderRequest.getOrderId());
    }
}
