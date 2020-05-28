package com.callrecorder.login;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class LoginResponseBean implements Serializable {

    @SerializedName("status")
    @Expose
    private Boolean status;
    @SerializedName("userId")
    @Expose
    private String userId;

    @SerializedName("message")
    @Expose
    private String message;
    private final static long serialVersionUID = 4155453953169813111L;

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}