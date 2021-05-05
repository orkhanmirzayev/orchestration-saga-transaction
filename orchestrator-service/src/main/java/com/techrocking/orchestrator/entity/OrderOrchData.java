package com.techrocking.orchestrator.entity;

import com.techrocking.orchestrator.constants.OrderStatus;

import javax.persistence.*;

@Entity
@Table(name = "order_data")
public class OrderOrchData {

    @Id
    @GeneratedValue
    private Long id;

    @Column(name="order_id")
    private Long orderId;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "OrderOrchData{" +
                "id=" + id +
                ", orderId=" + orderId +
                ", status=" + status +
                '}';
    }
}
