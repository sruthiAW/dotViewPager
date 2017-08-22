package com.example.aidlapplication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;

/**
 * Created by ssurendran on 7/12/2017.
 */

public class IPCService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new ITestAidlService.Stub() {
            @Override
            public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {
                //do nothing
            }

            @Override
            public String getAppendedString(String name) throws RemoteException {
                return "Hello " + name;
            }
        };
    }
}
