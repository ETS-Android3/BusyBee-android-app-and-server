package com.zuas.bzb.model.dto;

public class OrderDTO {
    private String email;
    private String token;
    private double latitude;
    private double longitude;
    private String text;

    public OrderDTO() {
    }

    public OrderDTO(String email, String token, double latitude, double longitude, String text) {
        this.email = email;
        this.token = token;
        this.latitude = latitude;
        this.longitude = longitude;
        this.text = text;
    }

    public OrderDTO(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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
}
