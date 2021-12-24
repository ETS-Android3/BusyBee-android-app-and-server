package com.zubov.android.bzb.model.DTO;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OrderToUpdateDTO {

    @SerializedName("orderId")
    @Expose
    private int orderId;
    @SerializedName("token")
    @Expose
    private String token;
    @SerializedName("text")
    @Expose
    private String text;
    @SerializedName("status")
    @Expose
    private boolean status;

    public OrderToUpdateDTO() {
    }

    public OrderToUpdateDTO(int orderId, String token, String text, boolean status) {
        this.orderId = orderId;
        this.token = token;
        this.text = text;
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

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
