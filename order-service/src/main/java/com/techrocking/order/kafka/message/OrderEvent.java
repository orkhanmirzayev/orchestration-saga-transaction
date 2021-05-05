package com.techrocking.order.kafka.message;

public class OrderEvent {
	
	private Long orderId;
	private String productCode;
	private int quantity;
	private OrderAction action;
	
	public static enum OrderAction {
		ORDERPLACED,
		ORDERNOTPLACED
	}
	
	public Long getOrderId() {
		return orderId;
	}
	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}
	public OrderAction getAction() {
		return action;
	}
	public void setAction(OrderAction action) {
		this.action = action;
	}

	public String getProductCode() {
		return productCode;
	}

	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
}
