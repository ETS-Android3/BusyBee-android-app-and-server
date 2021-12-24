package com.zuas.bzb.model.dto;

public class OrderResponseDTO {
    private int order_id;

    public OrderResponseDTO() {
    }

    public OrderResponseDTO(int order_id) {
        this.order_id = order_id;
    }

    public int getOrder_id() {
        return order_id;
    }

    public void setOrder_id(int order_id) {
        this.order_id = order_id;
    }
}
