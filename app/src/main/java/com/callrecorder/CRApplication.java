package com.callrecorder;

import android.app.Application;
import android.content.Context;

public class CRApplication extends Application {
    public static CRApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    @Override
    public Context getApplicationContext() {
        return super.getApplicationContext();
    }

    public static CRApplication getInstance() {
        return instance;
    }
}