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
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.callrecorder.utils.CallHelper;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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
    static  final  String FilePath="FilePath";
    public static  final  String RecordingDirectoryTag="RecordingDirectory";
    public static  final  String LastRecordingFileNameTag="LastRecordingFileName";
    public static String RecordingDirectory;
    public static String LastRecordingFileName;
    public  static  boolean IsStartedWatching;
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


            if(LastCallDetails==null || !LastCallDetails.IsCurrentCall) {
                Toast.makeText(context, "Call detected(Incoming/Outgoing) " + state, Toast.LENGTH_SHORT).show();

                LastCallDetails = new CallDetails();
                LastCallDetails.IsCurrentCall = true;

                Intent reivToServ = new Intent(context, RecorderService.class);
                String time = new CommonMethods().getTIme();

                String fileName = phoneNumber + "_" + time;

                reivToServ.putExtra("number", phoneNumber);
                reivToServ.putExtra("fileName", fileName);
                if(!IsStartedWatching) {
                    StartWatching(context);
                    IsStartedWatching=true;
                }
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

    public static   void StopWatching()
    {
        IsStartedWatching=false;
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

            //  context.stopService(new Intent(context, RecorderService.class));
            if(LastCallDetails.IsCurrentCall) {
             getLastCallDetails(context);
                //if(TextUtils.isEmpty(LastCallDetails.getFilePath()))
                {

                    LastRecordingFileName = pref.get(LastRecordingFileNameTag, "");
                    RecordingDirectory = pref.get(RecordingDirectoryTag, "");
                    if (!TextUtils.isEmpty(RecordingDirectory)) {
                        String fileName = GetLastFile(context, RecordingDirectory, LastRecordingFileName);
                        if (!TextUtils.isEmpty(fileName) && !IsFileAccessed(context, pref, fileName)) {
                            pref.set(LastRecordingFileNameTag,fileName);
                            LastCallDetails.setFilePath(RecordingDirectory + fileName);
                            SaveAccessedFilePathToPref(context, pref, fileName);
                        } else
                            LastCallDetails.setFilePath(null);
                    }
                }


                Toast.makeText(context, "FilePath:"+ LastCallDetails.getFilePath(), Toast.LENGTH_SHORT).show();
                new DatabaseManager(context).addCallDetails(LastCallDetails);
                pref.set("recordStarted", false);

                Intent uploadIntent = new Intent(context, UploadIntentService.class);
                context.startService(uploadIntent);
                LastCallDetails.IsCurrentCall=false;
            }


        }



    }

    private void SaveAccessedFilePathToPref(Context con, PreferenceManager pref, String s)
    {
        Gson gson = new Gson();
        List<String> items = new ArrayList<>();
        String savedData = pref.get(FilePath,"");
        if(TextUtils.isEmpty(savedData))
        {
            items.add(s);
        }
        else
        {
            String[] text = gson.fromJson(savedData, String[].class);
            items = new ArrayList<>( Arrays.asList(text));
            if(items.size()>20)
               items.removeAll(items.subList(0,19));

            items.add(s);
        }

        String jsonData = gson.toJson(items);
        pref.set(FilePath, jsonData);
    }

    private  boolean IsFileAccessed(Context con, PreferenceManager pref, String s)
    {
        Gson gson = new Gson();
        String savedData = pref.get(FilePath,"");
        String[] text = gson.fromJson(savedData, String[].class);
        if(text!=null) {
            List<String> items = Arrays.asList(text);
            return items.contains(s);
        }
        else
            return false;
    }

    private String GetLastFile(Context con,String root, String lastAccessFilePath)
    {

        if(!TextUtils.isEmpty(root))
        {
            File directory = new File(root);
            File[] files = directory.listFiles();
            if(files.length>0) {
                Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
                String name = files[0].getName();
                if(name != lastAccessFilePath && (name.endsWith(".amr") || name.endsWith(".mp4") || name.endsWith(".m4a")))
                    return  name;
            }

        }

        return  null;
    }
}
