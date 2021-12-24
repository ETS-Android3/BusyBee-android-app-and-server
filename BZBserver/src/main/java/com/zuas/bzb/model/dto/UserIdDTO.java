package com.zuas.bzb.model.dto;

public class UserIdDTO {
    private int userId;
    private String token;

    public UserIdDTO() {
    }

    public UserIdDTO(int userId,String token) {
        this.userId = userId;
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
