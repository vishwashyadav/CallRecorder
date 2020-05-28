package com.callrecorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.CallLog;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.callrecorder.utils.CallHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class PhoneStateReceiver extends BroadcastReceiver {

    static ArrayList<String> FolderNames = new ArrayList<>();
    static ArrayList<CallHelper> WatchList = new ArrayList<>();
    static final String TAG = "State";
    static final String TAG1 = " Inside State";
    static Boolean recordStarted;
    public static String phoneNumber;
    public static String name;
    public  static  CallDetails LastCallDetails;
    @Override
    public void onReceive(Context context, Intent intent) {
        PreferenceManager pref = PreferenceManager.instance(context);


        Boolean switchCheckOn = pref.get("switchOn", false);
        if (switchCheckOn) {
            try {
                System.out.println("Receiver Start");

//            boolean callWait=pref.getBoolean("recordStarted",false);
                Bundle extras = intent.getExtras();
                String state = extras.getString(TelephonyManager.EXTRA_STATE);
                Log.d(TAG, " onReceive: " + state);
                Toast.makeText(context, "Call detected(Incoming/Outgoing) " + state, Toast.LENGTH_SHORT).show();

                if (extras != null) {
                       if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                        Log.d(TAG1, " Inside " + state);
                    } else if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)/*&& pref.getInt(PreferenceManager.numOfCalls,1)==1*/) {

                        OnRecieve(pref,intent, context, state);

                    } else if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {

                           OnCallEnd(pref, state, context);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public void getLastCallDetails(Context context) {

        Uri contacts = CallLog.Calls.CONTENT_URI;
        try {

            Cursor managedCursor = context.getContentResolver().query(contacts, null, null, null, android.provider.CallLog.Calls.DATE + " DESC limit 1;");

            int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
            int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);
            int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
            int incomingtype = managedCursor.getColumnIndex(String.valueOf(CallLog.Calls.INCOMING_TYPE));

                while (managedCursor.moveToNext()) {
                    String callType;
                    String phNumber = managedCursor.getString(number);
                    LastCallDetails.setNum(phNumber);
                    if(incomingtype == -1){
                        callType = "OUTGOING";
                    }
                    else {
                        callType = "INCOMING";
                    }
                    String callDate = managedCursor.getString(date);
                    Date callDayTime = new Date(Long.valueOf(callDate));

                    String callDuration = managedCursor.getString(duration);
                    LastCallDetails.CallType = callType;
                    LastCallDetails.setDate1(Utils.Companion.DateToString(callDayTime));
                    LastCallDetails.Duration = callDuration;
                }
            managedCursor.close();

        } catch (SecurityException e) {
            Log.e("Security Exception", "User denied call log permission");

        }

    }


    private  void OnRecieve(PreferenceManager pref, Intent intent, Context context, String state)
    {
        int j = pref.get(PreferenceManager.numOfCalls, 0);
        pref.set(PreferenceManager.numOfCalls, ++j);
        Log.d(TAG, "onReceive: num of calls " + pref.get(PreferenceManager.numOfCalls, 0));

        Log.d(TAG1, " recordStarted in offhook: " + recordStarted);
        Log.d(TAG1, " Inside " + state);

        phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

        Log.d(TAG1, " Phone Number in receiver " + phoneNumber);

        if (pref.get(PreferenceManager.numOfCalls, 1) == 1) {

            LastCallDetails = new CallDetails();
            LastCallDetails.IsCurrentCall=true;

            Intent reivToServ = new Intent(context, RecorderService.class);
            String time = new CommonMethods().getTIme();

            String fileName = phoneNumber + "_" + time ;

            reivToServ.putExtra("number", phoneNumber);
            reivToServ.putExtra("fileName", fileName);
            StartWatching(context);
     /*       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(reivToServ);
            } else {
                context.startService(reivToServ);
            }*/

            //name=new CommonMethods().getContactName(phoneNumber,context);

            int serialNumber = pref.get("serialNumData", 1);
            //new DatabaseManager(context).addCallDetails(new CallDetails(serialNumber, phoneNumber, time, new CommonMethods().getDate()));
            //recordStarted=true;
            pref.set("serialNumData", ++serialNumber);
            pref.set("recordStarted", true);
        }

    }

    private void StartWatching(Context contex)
    {
        if(WatchList.size()==0) {
            for (String folder : FolderNames) {
                CallHelper helper = new CallHelper(contex, folder);
                helper.startWatching();
                WatchList.add(helper);
            }
        }
        else
        {
            for(CallHelper h:WatchList)
            {
                h.startWatching();
            }
        }
    }

    private  void StopWatching()
    {
        for(CallHelper h:WatchList)
        {
            h.stopWatching();
        }

      //  WatchList.clear();
    }

    private  void OnCallEnd(PreferenceManager pref, String state, Context context)
    {
        int k = pref.get(PreferenceManager.numOfCalls, 1);
        pref.set(PreferenceManager.numOfCalls, --k);
        int l = pref.get(PreferenceManager.numOfCalls, 0);
        Log.d(TAG1, " Inside " + state);
        recordStarted = pref.get("recordStarted", false);
        Log.d(TAG1, " recordStarted in idle :" + recordStarted);
        if (recordStarted && l == 0)
        {
            Toast.makeText(context, "Call Ended", Toast.LENGTH_SHORT).show();

        //    Log.d(TAG1, " Inside to stop recorder " + state);

            Toast.makeText(context, "Status:"+ LastCallDetails.IsCurrentCall, Toast.LENGTH_SHORT).show();
          //  context.stopService(new Intent(context, RecorderService.class));
            if(LastCallDetails.IsCurrentCall) {
             getLastCallDetails(context);
             Toast.makeText(context, "FilePath:"+ LastCallDetails.getFilePath(), Toast.LENGTH_SHORT).show();
                new DatabaseManager(context).addCallDetails(LastCallDetails);
                pref.set("recordStarted", false);

                Intent uploadIntent = new Intent(context, UploadIntentService.class);
                context.startService(uploadIntent);
                StopWatching();
            }


        }



    }
}
