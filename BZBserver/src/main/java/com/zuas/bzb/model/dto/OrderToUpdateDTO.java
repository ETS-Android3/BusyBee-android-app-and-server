package com.zuas.bzb.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderToUpdateDTO {
    private int orderId;
    private String token;
    private String text;
    private boolean status;

    public OrderToUpdateDTO() {
    }

    public OrderToUpdateDTO(int orderId, String token, String text, boolean status) {
        this.orderId = orderId;
        this.token = token;
        this.text = text;
        this.status = status;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
