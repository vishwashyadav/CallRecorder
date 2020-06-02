package com.callrecorder.utils;

import android.content.Context;
import android.os.FileObserver;
import android.util.Log;
import android.widget.Toast;

import com.callrecorder.PhoneStateReceiver;
import com.callrecorder.PreferenceManager;

import java.io.File;

public class CallHelper extends FileObserver{
    static final String TAG="FILEOBSERVER";
    /**
     * should be end with File.separator
     */
    String rootPath;
    Context context;
    static final int mask = (FileObserver.CREATE);

    public CallHelper(Context context,String root){
        super(root, mask);
this.context = context;
        if (! root.endsWith(File.separator)){
            root += File.separator;
        }
        rootPath = root;
    }


    public void onEvent(int event, String path) {

        if((path==null || path=="") || (!path.endsWith(".amr") && !path.endsWith(".mp4") && !path.endsWith(".m4a")))
            return;

        switch(event){
            case FileObserver.CREATE:
                if(PhoneStateReceiver.LastCallDetails != null && PhoneStateReceiver.LastCallDetails.IsCurrentCall)
                {
                    PhoneStateReceiver.RecordingDirectory = rootPath;
                    String tmPath=path;
                    if(tmPath.startsWith("."))
                        tmPath = path.substring(1);
                    String filePath = rootPath+tmPath;
                    PreferenceManager pref = PreferenceManager.instance(context);
                    pref.set(PhoneStateReceiver.RecordingDirectoryTag,rootPath);
                }

                break;
            case FileObserver.DELETE:
                Toast.makeText(context,"DELETE:" + rootPath + path,Toast.LENGTH_LONG);

                break;
            case FileObserver.DELETE_SELF:
                Toast.makeText(context,"DELETE_SELF:" + rootPath + path,Toast.LENGTH_LONG);
                break;
            case FileObserver.MODIFY:
                Toast.makeText(context,"MODIFY:" + rootPath + path,Toast.LENGTH_LONG);
                break;
            case FileObserver.MOVED_FROM:
                Toast.makeText(context,"MOVED_FROM:" + rootPath + path,Toast.LENGTH_LONG);
                break;
            case FileObserver.MOVED_TO:
                Log.d(TAG, "MOVED_TO:" + path);
                break;
            case FileObserver.MOVE_SELF:
                Log.d(TAG, "MOVE_SELF:" + path);
                break;
            default:
                Toast.makeText(context,event + rootPath + path,Toast.LENGTH_LONG);
                // just ignore
                break;
        }
    }

    public void close(){
        super.finalize();
    }
}