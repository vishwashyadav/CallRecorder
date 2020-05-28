
package com.callrecorder.bean;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class UserCallDetailsList implements Serializable {

    @SerializedName("id")
    @Expose
    private long id;
    @SerializedName("contactName")
    @Expose
    private String contactName;
    @SerializedName("toNum")
    @Expose
    private String toNum;
    @SerializedName("callDuration")
    @Expose
    private int callDuration;
    @SerializedName("callDate")
    @Expose
    private String callDate;
    @SerializedName("callType")
    @Expose
    private String callType;
    @SerializedName("callStatus")
    @Expose
    private String callStatus;
    @SerializedName("url")
    @Expose
    private String url;
    private final static long serialVersionUID = -7798949486461803778L;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getContactName() {
        return contactName;
    }
    public int getCallDuration() {
        return callDuration;
    }
    public String getCallDate() {
        return callDate;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getToNum() {
        return toNum;
    }

    public void setToNum(String toNum) {
        this.toNum = toNum;
    }

    public String getCallType() {
        return callType;
    }

    public void setCallType(String callType) {
        this.callType = callType;
    }

    public String getCallStatus() {
        return callStatus;
    }

    public void setCallStatus(String callStatus) {
        this.callStatus = callStatus;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
