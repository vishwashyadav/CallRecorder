package com.callrecorder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;


// Will be performing all actions on database.
public class DatabaseManager {

    SQLiteDatabase sqLiteDatabase;

    public DatabaseManager(Context activity) {
        sqLiteDatabase = DatabaseSingleton.getInstance(activity);
    }

    public void addCallDetails(CallDetails callDetails) {
try {

    ContentValues values = new ContentValues();
    values.put(DatabaseHandler.PHONE_NUMBER, callDetails.getNum());
    // values.put(DatabaseHandler.CONTACT_NAME,callDetails.getName());
    values.put(DatabaseHandler.TIME, callDetails.getTime1());
    values.put(DatabaseHandler.DATE, callDetails.getDate1());
    values.put(DatabaseHandler.UPLOAD_FLAG, "false");
    values.put(DatabaseHandler.FILE_PATH, callDetails.getFilePath());
    values.put(DatabaseHandler.CALL_DURATION, callDetails.Duration);
    values.put(DatabaseHandler.CALL_TYPE, callDetails.CallType);

    sqLiteDatabase.insert(DatabaseHandler.TABLE_RECORD, null, values);
}
catch (Exception ex)
        {
            String message = ex.getMessage();
        }
    }


    public List<CallDetails> getAllDetails() {
        List<CallDetails> recordList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + DatabaseHandler.TABLE_RECORD;

        Cursor cursor = sqLiteDatabase.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                CallDetails callDetails = new CallDetails();
                callDetails.setSerial(cursor.getInt(0));
                callDetails.setNum(cursor.getString(1));
                // callDetails.setName(cursor.getString(2));
                callDetails.setTime1(cursor.getString(2));
                callDetails.setFlag(cursor.getString(3));
                callDetails.setDate1(cursor.getString(4));
                String s = cursor.getString(5);
                callDetails.setFilePath(cursor.getString(5));
                callDetails.Duration= cursor.getString(6);
                callDetails.CallType = cursor.getString(7);
                recordList.add(callDetails);
            } while (cursor.moveToNext());
        }

        return recordList;
    }

    public void updateItem(CallDetails callDetails) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHandler.UPLOAD_FLAG, "true");
        String whereClause = DatabaseHandler.SERIAL_NUMBER + "=?";
        String whereArgs[] = {"" + callDetails.getSerial()};
        sqLiteDatabase.update(DatabaseHandler.TABLE_RECORD, contentValues, whereClause, whereArgs);
    }

    public void delete() {

        sqLiteDatabase.execSQL("delete from "+ DatabaseHandler.TABLE_RECORD);

    }

}
