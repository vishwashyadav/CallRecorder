
package com.callrecorder;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.callrecorder.adapter.DashboardListAdapter;
import com.callrecorder.bean.CallDetailsListResponseBean;
import com.callrecorder.bean.UserCallDetailsList;
import com.callrecorder.login.LoginActivity;
import com.callrecorder.login.LoginResponseBean;
import com.callrecorder.utils.CallHelper;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> folderNames = new ArrayList<>();
    ArrayList<CallHelper> folderWatchList = new ArrayList<>();
    DatabaseHandler db = new DatabaseHandler(this);
    final static String TAGMA = "Main Activity";
    final  static  String folders="Folders";
    DashboardListAdapter rAdapter;
    RecyclerView recycler;
    boolean checkResume = false;
CallHelper helper;

    private WebServiceProvider apiProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PreferenceManager pref = PreferenceManager.instance(this);
        pref.set(PreferenceManager.numOfCalls, 0);
        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(this));

        findViewById(R.id.logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatabaseManager(MainActivity.this).delete();
                PreferenceManager.instance(MainActivity.this).clearUserSession();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();

            }
        });

//        Utils.Companion.showDialog(getApplicationContext(), "Please wait..", "Scanning Directories");

  //      Utils.Companion.hideDialog();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("Check", "onResume: ");
        if (checkPermission()) {
            if(PhoneStateReceiver.FolderNames.size()==0) {
                IterateRecursive(Environment.getExternalStorageDirectory());
                PhoneStateReceiver.FolderNames = folderNames;
            }

            Toast.makeText(getApplicationContext(), "Permission already granted", Toast.LENGTH_LONG).show();
            if (checkResume == false) {
                getCallDetails();
            }
        }
    }

    protected void onPause() {
        super.onPause();
        PreferenceManager pref3 = PreferenceManager.instance(this);
        if (pref3.get(PreferenceManager.pauseStateVLC, false)) {
            checkResume = true;
            pref3.set(PreferenceManager.pauseStateVLC, false);
        } else
            checkResume = false;
    }


    public void setUi(List<UserCallDetailsList> list) {
        recycler = (RecyclerView) findViewById(R.id.recyclerView);


        rAdapter = new DashboardListAdapter(this, list, new DashboardListAdapter.Listener() {
            @Override
            public void deleteItem(@NotNull UserCallDetailsList details) {

                deleteCallDetails(details);
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recycler.setLayoutManager(layoutManager);
        recycler.setItemAnimator(new DefaultItemAnimator());
        recycler.setAdapter(rAdapter);


    }


    private boolean checkPermission() {
        int i = 0;
        String[] perm = {Manifest.permission.READ_PHONE_STATE, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_CONTACTS, Manifest.permission.READ_CALL_LOG};
        List<String> reqPerm = new ArrayList<>();

        for (String permis : perm) {
            int resultPhone = ContextCompat.checkSelfPermission(MainActivity.this, permis);
            if (resultPhone == PackageManager.PERMISSION_GRANTED)
                i++;
            else {
                reqPerm.add(permis);
            }
        }

        if (i == 6)
            return true;
        else
            return requestPermission(reqPerm);
    }


    private boolean requestPermission(List<String> perm) {
        // String[] permissions={Manifest.permission.READ_PHONE_STATE,Manifest.permission.RECORD_AUDIO,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE};

        String[] listReq = new String[perm.size()];
        listReq = perm.toArray(listReq);
        for (String permissions : listReq) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permissions)) {
                Toast.makeText(getApplicationContext(), "Phone Permissions needed for " + permissions, Toast.LENGTH_LONG);
            }
        }

        ActivityCompat.requestPermissions(MainActivity.this, listReq, 1);


        return false;
    }


    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(getApplicationContext(), "Permission Granted to access Phone calls", Toast.LENGTH_LONG);
                else
                    Toast.makeText(getApplicationContext(), "You can't access Phone calls", Toast.LENGTH_LONG);
                break;
        }

    }


    private void getCallDetails() {


        apiProvider = WebServiceProvider.Companion.getRetrofit().create(WebServiceProvider.class);

        apiProvider.getCallDetails(PreferenceManager.instance(this).get(PreferenceManager.USER_ID, null))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<CallDetailsListResponseBean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(CallDetailsListResponseBean loginResponse) {

                        if (loginResponse.isStatus()) {
                            setUi(loginResponse.getUserCallDetailsList());
                        } else if (!TextUtils.isEmpty(loginResponse.getMessage())) {
                            Utils.Companion.toast(loginResponse.getMessage(), MainActivity.this);
                        } else {

                            Utils.Companion.toast(loginResponse.getMessage(), MainActivity.this);
                        }

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();

                    }
                });


    }


    private void IterateRecursive(File root)
    {
        try {

            File[] list = root.listFiles();

            for (File f : list) {
                if (f.isDirectory()) {
                    if (!f.getName().startsWith(".") || !f.getName().startsWith("com.")) {
                        folderNames.add(f.getAbsolutePath());
                        IterateRecursive(f);
                    }
                }
            }
        }
        catch (Exception ex)
        {
            Toast.makeText(this.getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG);
        }
    }


    private void deleteCallDetails(UserCallDetailsList bean) {
        JsonObject user = new JsonObject();

        user.addProperty("callDetailsId", bean.getId());
        user.addProperty("url", bean.getUrl());


        apiProvider = WebServiceProvider.Companion.getRetrofit().create(WebServiceProvider.class);

        apiProvider.deleteRecord(user)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<LoginResponseBean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(LoginResponseBean loginResponse) {
                        getCallDetails();
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();

                    }
                });


    }

}
