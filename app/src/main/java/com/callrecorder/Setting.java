package com.callrecorder;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.callrecorder.login.LoginActivity;

import java.io.File;

public class Setting extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        TextView recordingTxt = findViewById(R.id.RecordingDirectory);
        recordingTxt.setText(PhoneStateReceiver.RecordingDirectory);

        findViewById(R.id.btnDirectory).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    OpenFolderBroseDialog();
                }
                catch (Exception ex)
                {
                    String msg = ex.getMessage();
                    Toast.makeText(getApplicationContext(),msg, Toast.LENGTH_LONG);
                }
            }
        });

    }

    private void OpenFolderBroseDialog()
    {
        Uri selectedUri = Uri.parse(Environment.getExternalStorageDirectory().getAbsolutePath());
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent,100);
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
                    PreferenceManager pref = PreferenceManager.instance(MainActivity.context);
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
}
