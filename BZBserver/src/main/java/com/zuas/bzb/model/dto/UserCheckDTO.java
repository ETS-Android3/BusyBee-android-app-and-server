package com.zuas.bzb.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserCheckDTO {

    private String email;

    //@JsonInclude(JsonInclude.Include.NON_NULL)
    private String password;

    private int userID;

    private String userToken;

    public UserCheckDTO() {}

    public UserCheckDTO(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public UserCheckDTO(int userID, String userToken) {
        this.userID = userID;
        this.userToken = userToken;
    }

    public UserCheckDTO(String email, String password, int userID, String userToken) {
        this.email = email;
        this.password = password;
        this.userID = userID;
        this.userToken = userToken;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
