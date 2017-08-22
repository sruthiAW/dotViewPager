package com.example.android.basicmanagedprofile;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.support.v4.content.FileProvider.getUriForFile;
import static com.example.android.basicmanagedprofile.Constants.FILE_PROVIDER_AUTHORITY;

public class ShareFileActivity extends Activity {

    private static final String SAMPLE_FILE_NAME = "myTestFile.txt";
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_file);

        tv = (TextView) findViewById(R.id.tv);
        saveFile();

        if (getIntent() != null && getIntent().getData() != null) {
            displayFileContents(getIntent().getData());
        }


        Button btn = (Button) findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareFile();
            }
        });


    }


    private void displayFileContents(Uri fileUri){

        ParcelFileDescriptor mInputPFD;
        try {
            mInputPFD = getContentResolver().openFileDescriptor(fileUri, "r");
            FileDescriptor fd = null;
            if (mInputPFD != null) {
                fd = mInputPFD.getFileDescriptor();
            }
            FileInputStream inputStream = null;
            if (fd != null) {
                inputStream = new FileInputStream(fd);
            }

            byte[] buffer = new byte[256];
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("File uri: ");
            stringBuilder.append(fileUri);
            stringBuilder.append("\n");
            stringBuilder.append("\n File Content: ");
            if (inputStream != null && inputStream.read(buffer) > 0) {
                stringBuilder.append(new String(buffer));
            }

            tv.setText(stringBuilder.toString());
            Log.d("MINE", "Message: " + stringBuilder.toString());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d("MINE", "Error1: " + e);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("MINE", "Error2: " + e);
        }
    }


    private void shareFile() {
        File file = new File(getFilesDir(), "media");
        File sampleFile = new File(file, SAMPLE_FILE_NAME);
        Uri contentUri = getUriForFile(this, FILE_PROVIDER_AUTHORITY, sampleFile);

        Intent intent = new Intent();
        intent.setAction(Constants.CUSTOM_INTENT_ACTION2);
        intent.setDataAndType(contentUri, "text/plain");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);


        PackageManager packageManager = getPackageManager();

        packageManager.setComponentEnabledSetting(new ComponentName(this, com.example.android.basicmanagedprofile.ShareFileActivity2.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent);
        }

    }


    private void saveFile(){
        File file = new File(getFilesDir(), "media");

        if( !file.exists() ) {
            file.mkdirs();
        }

        File sampleFile = new File(file, SAMPLE_FILE_NAME);
        if( !sampleFile.exists() ) {
            try {
                sampleFile.createNewFile();
            } catch (IOException e) {
                Log.d("MINE", "saveFile()1 : " + e);
                e.printStackTrace();
            }
        }

        String filetext = "Some app data read from a file, saved and shared from the PERSONAL profile";

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(sampleFile);
            fileOutputStream.write(filetext.getBytes());
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            Log.d("MINE", "saveFile()2 : " + e);
            e.printStackTrace();
        } catch (IOException e) {
            Log.d("MINE", "saveFile()3 : " + e);
            e.printStackTrace();
        }
    }
}
