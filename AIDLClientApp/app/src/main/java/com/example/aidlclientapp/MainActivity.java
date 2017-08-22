package com.example.aidlclientapp;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.aidlapplication.ITestAidlService;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    ITestAidlService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final HashMap<String, String> hMap = new HashMap<>();
        hMap.put("key1", "v1");
        hMap.put("key2", null);
        hMap.put(null, null);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*try {
                    Snackbar.make(view, service.getAppendedString("AIDL"), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }*/

                String finalStr = "";
                for (Map.Entry<String, String > entry: hMap.entrySet()){
                    finalStr = finalStr + entry.getKey() + "--" + entry.getValue() + "\n";
                }
                Toast.makeText(MainActivity.this, finalStr, Toast.LENGTH_LONG).show();

            }
        });
    }

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            service = ITestAidlService.Stub.asInterface(iBinder);
            Toast.makeText(MainActivity.this, "Service connected", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        bindService(new Intent().setClassName("com.example.aidlapplication", "com.example.aidlapplication.IPCService"),
                serviceConnection,
                BIND_AUTO_CREATE);

    }

    @Override
    protected void onStop() {
        unbindService(serviceConnection);
        super.onStop();
    }
}
