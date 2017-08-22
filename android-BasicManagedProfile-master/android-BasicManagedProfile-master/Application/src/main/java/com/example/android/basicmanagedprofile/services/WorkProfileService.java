package com.example.android.basicmanagedprofile.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import com.example.android.basicmanagedprofile.IAIDLServiceInterface;

/**
 * Created by ssurendran on 8/16/2017.
 */

public class WorkProfileService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new IAIDLServiceInterface.Stub() {

            @Override
            public String getPersonalProfileData() throws RemoteException {
                return "From Work to Personal through service";
            }
        };
    }
}
