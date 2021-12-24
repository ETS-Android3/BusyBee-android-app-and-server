package com.zuas.bzb.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

//@JsonIgnoreProperties(ignoreUnknown = true)
public class UserUpdatePasswordDTO {
    private String mEmail;
    private String mOldPassword;
    private String mNewPassword;

    public UserUpdatePasswordDTO() {
    }

    public UserUpdatePasswordDTO(String email, String oldPassword, String newPassword) {
        mEmail = email;
        mOldPassword = oldPassword;
        mNewPassword = newPassword;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String myemail) {
        mEmail = myemail;
    }

    public String getOldPassword() {
        return mOldPassword;
    }

    public void setOldPassword(String oldPassword) {
        mOldPassword = oldPassword;
    }

    public String getNewPassword() {
        return mNewPassword;
    }

    public void setNewPassword(String newPassword) {
        mNewPassword = newPassword;
    }
}
