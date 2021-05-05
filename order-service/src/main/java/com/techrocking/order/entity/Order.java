package com.techrocking.order.entity;

import com.techrocking.order.constants.OrderStatus;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/*this is db entity and should be mapped to db table*/
@Entity(name = "order")
@Table(name = "orders")
public class Order implements Serializable {

	@Id
	@GeneratedValue
	private Long id;

	@Column(name = "item_name")
	private String itemName;

	@Column(name = "destination")
	private String destination;

	@Column(name = "order_time")
	@Temporal(TemporalType.TIMESTAMP)
	private Date orderTime;

	@Enumerated(EnumType.STRING)
	private OrderStatus status;

	@ManyToOne
	@JoinColumn(name = "customer_id",nullable = false)
	private Customer customer;

//	private OrderItem orderItem;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public Date getOrderTime() {
		return orderTime;
	}

	public void setOrderTime(Date orderTime) {
		this.orderTime = orderTime;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public void setStatus(OrderStatus status) {
		this.status = status;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}
//
//	public OrderItem getOrderItem() {
//		return orderItem;
//	}
//
//	public void setOrderItem(OrderItem orderItem) {
//		this.orderItem = orderItem;
//	}


	@Override
	public String toString() {
		return "Order{" +
				"id=" + id +
				", itemName='" + itemName + '\'' +
				", destination='" + destination + '\'' +
				", orderTime=" + orderTime +
				", status=" + status +
				", customer=" + customer +
				'}';
	}
}
