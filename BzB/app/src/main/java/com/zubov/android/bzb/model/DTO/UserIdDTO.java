package com.zubov.android.bzb.model.DTO;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserIdDTO {
    @SerializedName("userId")
    @Expose
    private int userId;
    @SerializedName("token")
    @Expose
    private String token;

    public UserIdDTO() {
    }

    public UserIdDTO(int userId, String userToken) {
        this.userId = userId;
        this.token = userToken;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserToken() {
        return token;
    }

    public void setUserToken(String userToken) {
        this.token = userToken;
    }
}
