package com.zubov.android.bzb.model.DTO;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class UserOrderDTO {
    @SerializedName("order_id")
    @Expose
    private int orderId;
    @SerializedName("latitude")
    @Expose
    private double latitude;
    @SerializedName("longitude")
    @Expose
    private double longitude;
    @SerializedName("text")
    @Expose
    private String text;
    @SerializedName("orderdate")
    @Expose
    private String orderdate;
    @SerializedName("status")
    @Expose
    private boolean status;

    public UserOrderDTO() {
    }

    public UserOrderDTO(int orderID, double markerLatitude, double markerLongitude, String text, String date,boolean status) {
        this.orderId = orderID;
        this.latitude = markerLatitude;
        this.longitude = markerLongitude;
        this.text = text;
        this.orderdate = date;
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

    public double getMarkerLatitude() {
        return latitude;
    }

    public void setMarkerLatitude(double markerLatitude) {
        this.latitude = markerLatitude;
    }

    public double getMarkerLongitude() {
        return longitude;
    }

    public void setMarkerLongitude(double markerLongitude) {
        this.longitude = markerLongitude;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getOrderdate() {
        return orderdate;
    }

    public void setOrderdate(String orderdate) {
        this.orderdate = orderdate;
    }
}
