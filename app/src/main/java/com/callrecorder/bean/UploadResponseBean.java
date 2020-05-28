package com.callrecorder.bean;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class UploadResponseBean implements Serializable {

    @SerializedName("status")
    @Expose
    private Boolean status;
    @SerializedName("fileUrl")
    @Expose
    private String fileUrl;
    private final static long serialVersionUID = 1415645318772929894L;

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

}