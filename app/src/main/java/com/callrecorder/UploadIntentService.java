package com.callrecorder;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.callrecorder.login.LoginResponseBean;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.List;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;


public class UploadIntentService extends IntentService {

    private WebServiceProvider apiProvider;

    List<CallDetails> callDetailsList;

    public UploadIntentService() {
        super("UploadIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        apiProvider = WebServiceProvider.Companion.getRetrofit().create(WebServiceProvider.class);

        callDetailsList = new DatabaseManager(this).getAllDetails();
        Log.d("UploadIntentService", " onHandleIntent " + callDetailsList.size());

        for (CallDetails bean : callDetailsList) {
            if (!TextUtils.isEmpty(bean.getFlag()) && bean.getFlag().equalsIgnoreCase("false")) {
                uploadFile(bean);
            }
        }
    }

    private void uploadFile(CallDetails bean) {

      String path = bean.getFilePath();
        if(path ==null || path=="")
        {
            updateDetails("",bean);
        }
        else {
            String extension = bean.getFilePath().substring(path.lastIndexOf("."));
            String fileName = bean.getNum() + "_" + bean.getDate1() + extension;
            String destPath = Environment.getExternalStorageDirectory()+"/My Records/Upload/"+fileName;
            //  Log.d("path", "onClick: " + path);
            //Log.d("upload status", "upload: " + bean.getFlag());
            if (!TextUtils.isEmpty(bean.getFlag()) && bean.getFlag().equalsIgnoreCase("false")) {
                ImageUploadHandler.Companion.uploadImageToServer(100, path, fileName, destPath, this, new ImageUploadHandler.UploadListener() {
                    @Override
                    public void onUpload(@org.jetbrains.annotations.Nullable String url, int requestCode) {

                        updateDetails(url, bean);

                    }

                    @Override
                    public void onFailed(@org.jetbrains.annotations.Nullable String error) {

                    }
                });
            } else {
                Log.d("upload status" + bean.getNum(), "upload: " + bean.getFlag());
            }
        }
    }

    private void updateDetails(String url, CallDetails bean) {
        JsonObject user = new JsonObject();

        user.addProperty("userId", PreferenceManager.instance(this).get(PreferenceManager.USER_ID, null));
        user.addProperty("contactName", "abc");
        user.addProperty("formNum", PreferenceManager.instance(this).get(PreferenceManager.USER_MOBILE_NUMBER, null));
        user.addProperty("toNum", bean.getNum());
        user.addProperty("callType", bean.CallType);
        user.addProperty("callDuration", bean.Duration);
        user.addProperty("callTime", bean.getDate1());
        user.addProperty("url", url);
        user.addProperty("updatedBy", PreferenceManager.instance(this).get(PreferenceManager.USER_NAME, null));


        apiProvider = WebServiceProvider.Companion.getRetrofit().create(WebServiceProvider.class);

        apiProvider.updateDetails(user)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<LoginResponseBean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(LoginResponseBean loginResponse) {
                        updateUploadFileStatus(bean);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();

                    }
                });


    }

    private void updateUploadFileStatus(CallDetails bean) {

        new DatabaseManager(this).updateItem(bean);
    }


}