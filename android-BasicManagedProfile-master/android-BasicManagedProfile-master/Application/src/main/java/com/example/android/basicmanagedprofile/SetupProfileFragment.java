/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.basicmanagedprofile;

import android.app.Activity;
import android.app.Fragment;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * This {@link Fragment} handles initiation of managed profile provisioning.
 */
public class SetupProfileFragment extends Fragment implements View.OnClickListener {

    private static final int REQUEST_PROVISION_MANAGED_PROFILE = 1;

    TextView msgTV;

    public static SetupProfileFragment newInstance(Intent intent) {

        SetupProfileFragment fragment = new SetupProfileFragment();

        if(intent != null && intent.getData() != null) {
            Bundle args = new Bundle();
            args.putString("fileuri", intent.getData().toString());
            fragment.setArguments(args);
        }

        return fragment;
    }

    public SetupProfileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setup_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        view.findViewById(R.id.set_up_profile).setOnClickListener(this);

        msgTV = (TextView) view.findViewById(R.id.msg);

        //Not used as of now. Moved to ShareFileActivity
        if(getArguments() != null && getArguments().getString("fileuri") != null){
            String uriString = getArguments().getString("fileuri");
            displayFileContents(Uri.parse(uriString));
        }


        if (Build.VERSION.SDK_INT >= 26) {
//            setAffiliationId();
        }

    }


    @RequiresApi(api = 26)
    private void setAffiliationId(){
        DevicePolicyManager manager =
                (DevicePolicyManager) getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE);
        Set<String > affiliationIds = new HashSet<>(Arrays.asList("a", "w"));
        manager.setAffiliationIds(BasicDeviceAdminReceiver.getComponentName(getActivity()), affiliationIds);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.set_up_profile: {
                provisionManagedProfile();
                break;
            }
        }
    }

    /**
     * Initiates the managed profile provisioning. If we already have a managed profile set up on
     * this device, we will get an error dialog in the following provisioning phase.
     */
    private void provisionManagedProfile() {
        Activity activity = getActivity();
        if (null == activity) {
            return;
        }
        Intent intent = new Intent(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE);

        // Use a different intent extra below M to configure the admin component.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            //noinspection deprecation
            intent.putExtra(DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_PACKAGE_NAME,
                    activity.getApplicationContext().getPackageName());
        } else {
            final ComponentName component = new ComponentName(activity,
                    BasicDeviceAdminReceiver.class.getName());
            intent.putExtra(DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME,
                    component);
        }

        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_PROVISION_MANAGED_PROFILE);
            activity.finish();
        } else {
            Toast.makeText(activity, "Device provisioning is not enabled. Stopping.",
                           Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PROVISION_MANAGED_PROFILE) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(getActivity(), "Provisioning done.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Provisioning failed.", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void displayFileContents(Uri fileUri){

        ParcelFileDescriptor mInputPFD;
        try {
            Log.d("MINE", "start logs. Uri: " + fileUri);
            mInputPFD = getActivity().getContentResolver().openFileDescriptor(fileUri, "r");
            FileDescriptor fd = mInputPFD.getFileDescriptor();
            FileInputStream inputStream = new FileInputStream(fd);

            Log.d("MINE", "PFD: " + mInputPFD);
            Log.d("MINE", "FD: " + fd);
            Log.d("MINE", "inputStream: " + inputStream);


            byte[] buffer = new byte[4096];
            StringBuilder stringBuilder = new StringBuilder();
            if (inputStream.read(buffer) > 0){
                stringBuilder.append(new String(buffer));
            }


            msgTV.setText(stringBuilder.toString());
            Log.d("MINE", "Message: " + stringBuilder.toString());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d("MINE", "Error: " + e);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("MINE", "Error: " + e);
        }
    }

}
