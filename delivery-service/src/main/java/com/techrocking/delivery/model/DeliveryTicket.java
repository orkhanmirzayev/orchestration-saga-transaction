package com.techrocking.delivery.model;

import com.techrocking.delivery.constants.DeliveryStatus;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name="delivery_ticket")
public class DeliveryTicket implements Serializable {

    @Id
    @GeneratedValue
    private Long id;

    @Column(name="order_id")
    private Long orderId;

    @Column(name="from_time")
    @Temporal(TemporalType.TIME)
    private Date fromTime;

    @Column(name="to_time")
    @Temporal(TemporalType.TIME)
    private Date toTime;

    private String destination;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus deliveryStatus;

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

    public Date getFromTime() {
        return fromTime;
    }

    public void setFromTime(Date fromTime) {
        this.fromTime = fromTime;
    }

    public Date getToTime() {
        return toTime;
    }

    public void setToTime(Date toTime) {
        this.toTime = toTime;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public DeliveryStatus getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(DeliveryStatus deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    @Override
    public String toString() {
        return "DeliveryTicket{" +
                "id=" + id +
                ", orderId=" + orderId +
                ", fromTime=" + fromTime +
                ", toTime=" + toTime +
                ", destination='" + destination + '\'' +
                ", deliveryStatus=" + deliveryStatus +
                '}';
    }
}
