
package com.callrecorder.bean;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class CallDetailsListResponseBean implements Serializable {

    @SerializedName("status")
    @Expose
    private boolean status;

    @SerializedName("message")
    @Expose
    private String message;

    @SerializedName("userCallDetailsList")
    @Expose
    private List<UserCallDetailsList> userCallDetailsList = null;
    private final static long serialVersionUID = 5550601852752616606L;

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public List<UserCallDetailsList> getUserCallDetailsList() {
        return userCallDetailsList;
    }

    public void setUserCallDetailsList(List<UserCallDetailsList> userCallDetailsList) {
        this.userCallDetailsList = userCallDetailsList;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
