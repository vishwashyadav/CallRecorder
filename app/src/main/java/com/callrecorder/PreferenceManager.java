package com.callrecorder;

import android.content.Context;
import android.content.SharedPreferences;


public class PreferenceManager {

    public static final String APP_VERSION = "app_version";
    private static PreferenceManager mInstance = null;
    private final String PREFERENCE_NAME = "credr.connect";

    private SharedPreferences mPreference;
    public static final String USER_ID = "USER_ID";
    public static final String USER_NAME = "USER_NAME";
    public static final String USER_MOBILE_NUMBER = "USER_MOBILE_NUMBER";
    public static final String LOGIN_STATUS = "LOGIN_STATUS";
    public static final String switchOn = "switchOn";
    public static final String numOfCalls = "numOfCalls";
    public static final String pauseStateVLC = "pauseStateVLC";


    private PreferenceManager(Context c) {
        mPreference = c.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
    }


    public static PreferenceManager instance(Context context) {
        if (mInstance == null)
            mInstance = new PreferenceManager(context);

        return mInstance;
    }


    public void set(String key, long value) {
        Logger.Companion.e(key, "==>>> " + value);
        mPreference.edit().putLong(key, value)
                .apply();
    }

    public void set(String key, int value) {
        Logger.Companion.e(key, "==>>> " + value);
        mPreference.edit().putInt(key, value)
                .apply();
    }

    public void set(String key, String value) {
        Logger.Companion.e(key, "==>>> " + value);
        mPreference.edit().putString(key, value)
                .apply();
    }

    public void set(String key, boolean value) {
//        Logger.Companion.e(key,"==>>> "+value);
        mPreference.edit().putBoolean(key, value)
                .apply();
    }

    public String get(String key, String defaultValue) {

        return mPreference.getString(key, defaultValue);
    }

    public boolean get(String key, boolean defaultValue) {

        return mPreference.getBoolean(key, defaultValue);
    }

    public int get(String key, int defaultValue) {

        return mPreference.getInt(key, defaultValue);
    }

    public long get(String key, long defaultValue) {

        return mPreference.getLong(key, defaultValue);
    }


    /**
     * This method is used mainly for Logout purpose
     * Clear all stored data here
     */
    public void clearUserSession() {
        mPreference.edit().clear().apply();
    }

}
