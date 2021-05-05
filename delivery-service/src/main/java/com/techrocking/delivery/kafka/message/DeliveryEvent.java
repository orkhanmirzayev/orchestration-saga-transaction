package com.techrocking.delivery.kafka.message;

public class DeliveryEvent {
	
	private Long orderId;
	private DeliveryAction action;
	private Long deliveryTicketId;

	public enum DeliveryAction {
		DELIVERY_RECEIVED,
		DELIVERY_NOT_RECEIVED
	}

	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	public DeliveryAction getAction() {
		return action;
	}

	public void setAction(DeliveryAction action) {
		this.action = action;
	}

	public Long getDeliveryTicketId() {
		return deliveryTicketId;
	}

	public void setDeliveryTicketId(Long deliveryTicketId) {
		this.deliveryTicketId = deliveryTicketId;
	}
}
