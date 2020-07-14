package com.callrecorder;

import kotlin.jvm.internal.PropertyReference0Impl;

/**
 * Created by VS00481543 on 01-11-2017.
 */

public class CallDetails {

    private int serial;
    private String num;
    //  private String name;
    private String time;
    private String date;
    private  String filePath;
    private String flag;
    public String CallType;
    public String ExternalID;
    public String Duration;
    public  boolean IsCurrentCall;
    public CallDetails() {

    }

    public CallDetails(int serial, String num, String time, String date) {
        this.serial = serial;
        this.num = num;
        //this.name=name;
        this.time = time;
        this.date = date;
    }

    public int getSerial() {
        return serial;
    }
    public String getFilePath() {
        return filePath;
    }
    public void setFilePath(String path) {
        filePath=path;
    }

    public void setSerial(int serial) {
        this.serial = serial;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

   /* public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name=name;
    }*/

    public String getTime1() {
        return time;
    }

    public void setTime1(String time) {
        this.time = time;
    }

    public String getDate1() {
        return date;
    }

    public void setDate1(String date) {
        this.date = date;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }
}
