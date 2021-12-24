package com.zubov.android.bzb.model.DTO;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserResponseInfoDTO {

    @SerializedName("userID")
    @Expose
    private int userId;
    @SerializedName("userToken")
    @Expose
    private String token;

    public UserResponseInfoDTO() {
    }

    public UserResponseInfoDTO(int userID, String userToken) {
        this.userId = userID;
        this.token = userToken;
    }

    public int getUserID() {
        return userId;
    }

    public void setUserID(int userID) {
        this.userId = userID;
    }

    public String getUserToken() {
        return token;
    }

    public void setUserToken(String userToken) {
        this.token = userToken;
    }
}
