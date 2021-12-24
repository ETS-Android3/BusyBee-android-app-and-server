package com.zubov.android.bzb.model.DTO;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OrderResponseDTO {
    @SerializedName("order_id")
    @Expose
    private int orderID;

    public OrderResponseDTO() {
    }

    public OrderResponseDTO(int orderID) {
        this.orderID = orderID;
    }

    public int getOrderID() {
        return orderID;
    }

    public void setOrderID(int orderID) {
        this.orderID = orderID;
    }


}
