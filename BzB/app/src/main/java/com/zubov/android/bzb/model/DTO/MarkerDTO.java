package com.zubov.android.bzb.model.DTO;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MarkerDTO {

    @SerializedName("latitude")
    @Expose
    private double markerLatitude;
    @SerializedName("longitude")
    @Expose
    private double markerLongitude;


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
}
