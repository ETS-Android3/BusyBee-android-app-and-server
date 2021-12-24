package com.zubov.android.bzb.model.DTO;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OrderDTO {
    @SerializedName("email")
    @Expose
    private String userEmail;
    @SerializedName("token")
    @Expose
    private String userToken;
    @SerializedName("latitude")
    @Expose
    private double markerLatitude;
    @SerializedName("longitude")
    @Expose
    private double markerLongitude;
    @SerializedName("text")
    @Expose
    private String text;

    public OrderDTO(String userEmail, String userToken, double markerLatitude, double markerLongitude, String text) {
        this.userEmail = userEmail;
        this.userToken = userToken;
        this.markerLatitude = markerLatitude;
        this.markerLongitude = markerLongitude;
        this.text = text;
    }

    public OrderDTO() {
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }

    public double getMarkerLatitude() {
        return markerLatitude;
    }

    public void setMarkerLatitude(double markerLatitude) {
        this.markerLatitude = markerLatitude;
    }

    public double getMarkerLongitude() {
        return markerLongitude;
    }

    public void setMarkerLongitude(double markerLongitude) {
        this.markerLongitude = markerLongitude;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
