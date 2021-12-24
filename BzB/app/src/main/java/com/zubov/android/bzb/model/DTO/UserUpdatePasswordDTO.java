package com.zubov.android.bzb.model.DTO;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserUpdatePasswordDTO {
    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("oldPassword")
    @Expose
    private String mOldPassword;
    @SerializedName("newPassword")
    @Expose
    private String mNewPassword;

    public UserUpdatePasswordDTO() {
    }

    public UserUpdatePasswordDTO(String myEmail, String oldPassword, String newPassword) {
        email = myEmail;
        mOldPassword = oldPassword;
        mNewPassword = newPassword;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String myemail) {
        email = myemail;
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
