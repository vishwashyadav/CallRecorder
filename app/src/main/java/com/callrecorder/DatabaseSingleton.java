package com.callrecorder;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;


public class DatabaseSingleton {
    public static SQLiteDatabase database;

    public static SQLiteDatabase getInstance(Context activity) {
        if (database == null)
            database = new DatabaseHandler(activity).getWritableDatabase();
        return database;
    }
}
