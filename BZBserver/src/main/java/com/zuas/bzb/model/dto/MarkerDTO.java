package com.zuas.bzb.model.dto;

public class MarkerDTO {
    private double latitude;
    private double longitude;

    public MarkerDTO() {
    }

    public MarkerDTO(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;

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
}
