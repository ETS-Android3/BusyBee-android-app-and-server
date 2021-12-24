package com.zuas.bzb.model.dto;

import java.util.Date;

public class UserOrderDTO {
    private int orderId;
    private double latitude;
    private double longitude;
    private String text;
    private String orderdate;
    private boolean status;

    public UserOrderDTO() {
    }

    public UserOrderDTO(int orderid, double latitude, double longitude, String text, String orderdate, boolean status) {
        this.orderId = orderid;
        this.latitude = latitude;
        this.longitude = longitude;
        this.text = text;
        this.orderdate = orderdate;
        this.status = status;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public int getOrder_id() {
        return orderId;
    }

    public void setOrder_id(int orderid) {
        this.orderId = orderid;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
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
