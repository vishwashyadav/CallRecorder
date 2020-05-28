package com.callrecorder;

public class CallRecordTable {


    public String TableName="CallRecordTable";

    //Table Column Names
    public static String CallID="CallID";
    public static String CloudCallID="CloudCallID";
    public static String InComing="IsInComing";
    public static String IsOutgoing="IsOutgoing";
    public  static String CalledOn="CalledOn";
    public static String ToPhoneNumber="ToPhoneNumber";
    public static String Duration="Duration";
    public static String FileName="FileName";

    public String CreateTableQuery()
    {
        return  null;
    }
}
