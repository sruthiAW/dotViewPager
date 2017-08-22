package com.example.android.basicmanagedprofile;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.TextView;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ShareFileActivity2 extends Activity {

    TextView tv3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_file2);
        tv3 = (TextView) findViewById(R.id.tv3);

        if (getIntent() != null && getIntent().getData() != null) {
            displayFileContents(getIntent().getData());
        }

        PackageManager packageManager = getPackageManager();
        packageManager.setComponentEnabledSetting(new ComponentName(this, com.example.android.basicmanagedprofile.ShareFileActivity.class),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
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

            tv3.setText(stringBuilder.toString());
            Log.d("MINE", "Message: " + stringBuilder.toString());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d("MINE", "Error1: " + e);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("MINE", "Error2: " + e);
        }
    }

}
