package com.techrocking.delivery.model;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "message_info")
public class MessageInfo implements Serializable {

    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "key")
    private String key;

    @Column(name = "label")
    private String label;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
