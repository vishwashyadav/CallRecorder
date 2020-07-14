
package com.callrecorder;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.CallLog;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
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
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import retrofit2.Retrofit;

import static com.callrecorder.PhoneStateReceiver.LastCallDetails;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> folderNames = new ArrayList<>();
    ArrayList<CallHelper> folderWatchList = new ArrayList<>();
    DatabaseHandler db = new DatabaseHandler(this);
    final static String TAGMA = "Main Activity";
    final  static  String folders="Folders";
    DashboardListAdapter rAdapter;
    RecyclerView recycler;
    boolean checkResume = false;
    public static Context context;
    CallHelper helper;

    public List<UserCallDetailsList> CallDetailsLists;
    private WebServiceProvider apiProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        PreferenceManager pref = PreferenceManager.instance(this);
        pref.set(PreferenceManager.numOfCalls, 0);
        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(this));
        context = this;

        try {
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
        }
        catch (Exception ex)
        {

        }

        findViewById(R.id.logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                   new DatabaseManager(MainActivity.this).delete();
                    PreferenceManager.instance(MainActivity.this).clearUserSession();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }
                catch (Exception ex)
                {
                    String msg = ex.getMessage();
                    Toast.makeText(getApplicationContext(),msg, Toast.LENGTH_LONG);
                }
            }
        });





        findViewById(R.id.scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if(PhoneStateReceiver.FolderNames==null)
                        PhoneStateReceiver.FolderNames = new ArrayList<>();
                    //new SyncCallDetails().execute();
                    //Toast.makeText(getApplicationContext(),PhoneStateReceiver.FolderNames.size(),Toast.LENGTH_LONG);
                    File rootDir = Environment.getExternalStorageDirectory();
                    new FolderScanner().execute(rootDir);
                }
                catch (Exception ex)
                {
                    String msg = ex.getMessage();
                    Toast.makeText(getApplicationContext(),msg, Toast.LENGTH_LONG);
                }
            }
        });

        new SyncCallDetails().execute();
        InitiateSavedValue();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return true;
    }

    private void InitiateSavedValue()
    {
        PreferenceManager pref = GetPreferenceManager();
        PhoneStateReceiver.RecordingDirectory =  pref.get(PhoneStateReceiver.RecordingDirectoryTag,"");

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.refresh:
                getCallDetails();

                break;
            case R.id.save:
                new SaveCallDetails().execute(CallDetailsLists);

                break;
            case R.id.setRecordingPath:
                Intent myIntent = new Intent(this, Setting.class);
                this.startActivity(myIntent);

                break;
        }
        return  true;
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            switch (requestCode) {
                case 100:
                    Uri folderPath = data.getData();
                    String path = getRealPathFromURI(getApplicationContext(),data.getData());
                   File fs = new File(path);
                    PhoneStateReceiver.RecordingDirectory = fs.getParent();
                    PreferenceManager pref = PreferenceManager.instance(context);
                    pref.set(PhoneStateReceiver.RecordingDirectoryTag,PhoneStateReceiver.RecordingDirectory);

                    break;
            }
        }
        catch (Exception ex)
        {

        }

    }
    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.e("Check", "onResume: ");
        if (checkPermission()) {

            if(PhoneStateReceiver.FolderNames.size()==0)
                Toast.makeText(getApplicationContext(),"Please click on Scan Directory to find Recording Directory", Toast.LENGTH_LONG);
            if (checkResume == false && CanRefreshData()) {
                getCallDetails();
            }

        }
    }

    private  boolean CanRefreshData()
    {
        try {
            String lastCallID = "";
            PreferenceManager pref = PreferenceManager.instance(this);
            lastCallID = pref.get(PreferenceManager.LastCallID, "");
            Uri contacts = CallLog.Calls.CONTENT_URI;

            Cursor managedCursor = context.getContentResolver().query(contacts, null, null, null, android.provider.CallLog.Calls.DATE + " desc limit 1");

            int id_index = managedCursor.getColumnIndex(CallLog.Calls._ID);
            String callID = "";
            while (managedCursor.moveToNext()) {
                callID = managedCursor.getString(id_index);
            }
                managedCursor.close();

            boolean canRefresh = false;
            canRefresh = !(lastCallID.equals(callID));
            if (canRefresh) {
                pref.set(PreferenceManager.LastCallID, callID);
            }

            return canRefresh;
        }

    catch (SecurityException e) {
        Log.e("Security Exception", "User denied call log permission");
        return  false;
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


    public void setUi() {
        recycler = (RecyclerView) findViewById(R.id.recyclerView);


        rAdapter = new DashboardListAdapter(this, CallDetailsLists, new DashboardListAdapter.Listener() {
            @Override
            public void deleteItem(@NotNull UserCallDetailsList details) {

                deleteCallDetails(details);
                new SyncCallDetails().execute();
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

    private  void SetCallDetailsStatus()
    {
        Utils.Companion.showDialog(context,"Fetching Content,\r\n Please Wait...","Refreshing");
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
                            List<UserCallDetailsList> tmpList = loginResponse.getUserCallDetailsList();
                            for(UserCallDetailsList item : tmpList)
                            {
                                UserCallDetailsList findItem = CallDetailsLists.stream()
                                        .filter(s -> s.getExternalId().equals(item.getExternalId()))
                                        .findAny()
                                        .orElse(null);


                                if(findItem != null) {
                                    findItem.setId(item.getId());
                                    findItem.setUrl(item.getUrl());
                                    findItem.IsUploaded = true;
                                    findItem.IsDeleted = item.getCallStatus().contains("DELETE");
                                    findItem.setCallStatus(item.getCallStatus());
                                    findItem.IsFileUploaded = !TextUtils.isEmpty(item.getUrl());
                                }
                            }
                        }
                        setUi();
                        Utils.Companion.hideDialog();

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();

                    }
                });

    }

    private void getCallDetails() {
        new SyncCallDetails().execute();

    }

    private PreferenceManager GetPreferenceManager()
    {
        PreferenceManager pref = PreferenceManager.instance(this);
        return pref;
    }

    private void IterateRecursive(File root,ArrayList<String> names)
    {

        try {

            File[] list = root.listFiles();

            for (File f : list) {
                if (f.isDirectory()) {
                    if (!f.getName().startsWith(".") || !f.getName().startsWith("com.")) {
                        names.add(f.getAbsolutePath());
                        IterateRecursive(f,names);
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

    public  class FolderScanner extends AsyncTask<File,String,ArrayList<String>>
    {
        @Override
        protected void onPreExecute() {
            Utils.Companion.showDialog(context,"Scanning Directories,\r\n Please Wait...","Progress");
        }

        @Override
        protected void onPostExecute(ArrayList<String> strings) {
            PhoneStateReceiver.FolderNames = strings;
            PhoneStateReceiver.StopWatching();
            Utils.Companion.hideDialog();

        }

        @Override
        protected ArrayList<String> doInBackground(File... strings) {
            File rootDir = strings[0];

            ArrayList<String> files = new ArrayList<>();
            IterateRecursive(rootDir,files);
            return  files;
        }
    }


    public class SyncCallDetails extends AsyncTask<String,String,List<UserCallDetailsList>>
    {
        @Override
        protected void onPreExecute() {
            Utils.Companion.showDialog(context,"Loading Call details,\r\n Please Wait...","Progress");
        }

        @Override
        protected void onPostExecute(List<UserCallDetailsList> callDetailsLists) {
            CallDetailsLists = callDetailsLists;
            SetCallDetailsStatus();
            Utils.Companion.hideDialog();

        }

        @Override
        protected List<UserCallDetailsList> doInBackground(String... strings) {
            List<UserCallDetailsList> callDetailsLists = new ArrayList<>();
            Date lastCallDate = new Date();
            Uri contacts = CallLog.Calls.CONTENT_URI;

            try {

                Cursor managedCursor = context.getContentResolver().query(contacts, null, null, null, android.provider.CallLog.Calls.DATE +" desc ");

                int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
                int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);
                int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
                int id_index = managedCursor.getColumnIndex(CallLog.Calls._ID);
                int incomingtype = managedCursor.getColumnIndex(String.valueOf(CallLog.Calls.INCOMING_TYPE));

                while (managedCursor.moveToNext()) {
                    int daysDifference=0;

                    UserCallDetailsList callDetails = new UserCallDetailsList();
                    String callType;
                    String phNumber = managedCursor.getString(number);
                    callDetails.setToNum(phNumber);
                    if(incomingtype == -1){
                        callType = "OUTGOING";
                    }
                    else {
                        callType = "INCOMING";
                    }
                    callDetails.setCallType(callType);
                    callDetails.setExternalId( managedCursor.getString(id_index));
                    String callDate = managedCursor.getString(date);

                    Date callDayTime = new Date(Long.valueOf(callDate));
                    Date dt2 = new Date();
                    int diffInDays = (int) ((dt2.getTime() - callDayTime.getTime()) / (1000 * 60 * 60 * 24));
                    if(diffInDays > 7)
                        break;

                    int callDuration = managedCursor.getInt(duration);
                    callDetails.setCallDate(Utils.Companion.DateToString(callDayTime));
                    callDetails.setCallDuration(callDuration);
                    if(callDuration>0)
                    callDetailsLists.add(callDetails);
                }
                managedCursor.close();

            } catch (SecurityException e) {
                Log.e("Security Exception", "User denied call log permission");

            }

            return callDetailsLists;

        }

    }

    public  class SaveCallDetails extends AsyncTask<List<UserCallDetailsList>,String, List<UserCallDetailsList>>
    {
        @Override
        protected void onPreExecute() {
            Utils.Companion.showDialog(context,"Saving data..,\r\n Please Wait...","Progress");

        }

        @Override
        protected void onPostExecute(List<UserCallDetailsList> callDetailsLists) {
            Utils.Companion.hideDialog();

        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected List<UserCallDetailsList> doInBackground(List<UserCallDetailsList>... lists) {
            UploadIntentService service = new UploadIntentService();

            PreferenceManager pref = GetPreferenceManager();
            String recordingDirectory = pref.get(PhoneStateReceiver.RecordingDirectoryTag, "");
            File file = new File(recordingDirectory);
            File[] files = new File(recordingDirectory).listFiles();

            List<CallDetails> callDetails = new ArrayList<>();
            int count=0;
            for(UserCallDetailsList item : lists[0])

            {
                if(!item.IsDeleted && ( !item.IsUploaded || !item.IsFileUploaded)) {
                    CallDetails details = new CallDetails();
                    details.CallType = item.getCallType();

                    details.ExternalID = item.getExternalId();
                    details.Duration = Integer.toString(item.getCallDuration());
                    details.setDate1(item.getCallDate());
                    details.setFlag("false");
                    details.setNum(item.getToNum());
                    if(files!=null) {
                        File recordingFile = GetLastFile(getApplicationContext(), files, item);
                        if (recordingFile != null)
                            details.setFilePath(recordingFile.getAbsolutePath());
                    }

                    if(!item.IsUploaded || (item.IsUploaded && !TextUtils.isEmpty(details.getFilePath())))
                    callDetails.add(details);
                }


            }

            service.SaveCallDetails(callDetails);
            return  null;
        }

        private File GetLastFile(Context con,File[] files, UserCallDetailsList callDetails)
        {

            List<File> fileList = Arrays.asList(files);
            Date startDate = Utils.Companion.ToDate(callDetails.getCallDate());
            startDate.setSeconds(startDate.getSeconds()-15);
            Date endDate = Utils.Companion.ToDate(callDetails.getCallDate());
            endDate.setSeconds(endDate.getSeconds()+callDetails.getCallDuration()+15);

               if(files.length>0) {

                    File file = fileList.stream()
                           .filter(s ->
                           {
                               try {
                                   String name = s.getName();

                                   BasicFileAttributes attr = null;
                                   if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                       attr = Files.readAttributes(s.toPath(), BasicFileAttributes.class);
                                       long createdAt = attr.creationTime().toMillis();
                                       Date d =  new Date(createdAt);
                                       if ((d.after(startDate) && (d.before(endDate) || d.equals(endDate))) && (name.endsWith(".amr") || name.endsWith(".mp4") || name.endsWith(".mp3") || name.endsWith(".m4a"))) {
                                           return true;
                                       }
                                   }
                                   return  false;
                               }catch (IOException ex)
                               {
                                   return  false;
                               }

                           })
                           .findAny().orElse(null);;

                        return  file;
                }

            return  null;
        }
    }
}
