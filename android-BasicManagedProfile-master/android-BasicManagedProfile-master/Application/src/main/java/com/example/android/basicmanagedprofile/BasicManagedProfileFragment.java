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
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.basicmanagedprofile.services.WorkProfileService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.app.admin.DevicePolicyManager.FLAG_MANAGED_CAN_ACCESS_PARENT;
import static android.app.admin.DevicePolicyManager.FLAG_PARENT_CAN_ACCESS_MANAGED;
import static android.support.v4.content.FileProvider.getUriForFile;
import static com.example.android.basicmanagedprofile.Constants.CUSTOM_INTENT_ACTION;
import static com.example.android.basicmanagedprofile.Constants.CUSTOM_INTENT_ACTION2;
import static com.example.android.basicmanagedprofile.Constants.FILE_PROVIDER_AUTHORITY;

/**
 * Provides several functions that are available in a managed profile. This includes
 * enabling/disabling other apps, setting app restrictions, enabling/disabling intent forwarding,
 * and wiping out all the data in the profile.
 */
public class BasicManagedProfileFragment extends Fragment
        implements View.OnClickListener,
        CompoundButton.OnCheckedChangeListener {

    private static final String SAMPLE_FILE_NAME = "sampleFile.txt";

    /**
     * Tag for logging.
     */
    private static final String TAG = "ManagedProfileFragment";

    /**
     * Package name of calculator
     */
    private static final String PACKAGE_NAME_CALCULATOR = "com.android.calculator2";

    /**
     * Package name of Chrome
     */
    private static final String PACKAGE_NAME_CHROME = "com.android.chrome";

    /**
     * {@link Button} to remove this managed profile.
     */
    private Button mButtonRemoveProfile;

    /**
     * Whether the calculator app is enabled in this profile
     */
    private boolean mCalculatorEnabled;

    /**
     * Whether Chrome is enabled in this profile
     */
    private boolean mChromeEnabled;
    private IAIDLServiceInterface service;

    public BasicManagedProfileFragment() {
    }

    public static BasicManagedProfileFragment newInstance() {
        return new BasicManagedProfileFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Retrieves whether the calculator app is enabled in this profile
        mCalculatorEnabled = isApplicationEnabled(PACKAGE_NAME_CALCULATOR);
        // Retrieves whether Chrome is enabled in this profile
        mChromeEnabled = isApplicationEnabled(PACKAGE_NAME_CHROME);

        saveFile();

        if (Build.VERSION.SDK_INT >= 26) {
            setAffiliationId();
        }
    }

    @RequiresApi(api = 26)
    private void setAffiliationId(){
        DevicePolicyManager manager =
                (DevicePolicyManager) getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE);
        Set<String > affiliationIds = new HashSet<>(Arrays.asList("a", "w"));
        manager.setAffiliationIds(BasicDeviceAdminReceiver.getComponentName(getActivity()), affiliationIds);
    }

    /**
     * Checks if the application is available in this profile.
     *
     * @param packageName The package name
     * @return True if the application is available in this profile.
     */
    private boolean isApplicationEnabled(String packageName) {
        Activity activity = getActivity();
        PackageManager packageManager = activity.getPackageManager();
        try {
            int packageFlags;
            if(Build.VERSION.SDK_INT < 24){
                //noinspection deprecation
                packageFlags = PackageManager.GET_UNINSTALLED_PACKAGES;
            }else{
                packageFlags = PackageManager.MATCH_UNINSTALLED_PACKAGES;
            }
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(
                    packageName, packageFlags);
            // Return false if the app is not installed in this profile
            if (0 == (applicationInfo.flags & ApplicationInfo.FLAG_INSTALLED)) {
                return false;
            }
            // Check if the app is not hidden in this profile
            DevicePolicyManager devicePolicyManager =
                    (DevicePolicyManager) activity.getSystemService(Activity.DEVICE_POLICY_SERVICE);
            return !devicePolicyManager.isApplicationHidden(
                    BasicDeviceAdminReceiver.getComponentName(activity), packageName);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Bind event listeners and initial states
        view.findViewById(R.id.set_chrome_restrictions).setOnClickListener(this);
        view.findViewById(R.id.clear_chrome_restrictions).setOnClickListener(this);
        view.findViewById(R.id.enable_forwarding).setOnClickListener(this);
        view.findViewById(R.id.disable_forwarding).setOnClickListener(this);
        view.findViewById(R.id.send_intent).setOnClickListener(this);
        view.findViewById(R.id.bind_service).setOnClickListener(this);
        view.findViewById(R.id.share_via_service).setOnClickListener(this);
        mButtonRemoveProfile = (Button) view.findViewById(R.id.remove_profile);
        mButtonRemoveProfile.setOnClickListener(this);
        Switch toggleCalculator = (Switch) view.findViewById(R.id.toggle_calculator);
        toggleCalculator.setChecked(mCalculatorEnabled);
        toggleCalculator.setOnCheckedChangeListener(this);
        Switch toggleChrome = (Switch) view.findViewById(R.id.toggle_chrome);
        toggleChrome.setChecked(mChromeEnabled);
        toggleChrome.setOnCheckedChangeListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.set_chrome_restrictions: {
                setChromeRestrictions();
                break;
            }
            case R.id.clear_chrome_restrictions: {
                clearChromeRestrictions();
                break;
            }
            case R.id.enable_forwarding: {
                enableForwarding();
                break;
            }
            case R.id.disable_forwarding: {
                disableForwarding();
                break;
            }
            case R.id.send_intent: {
                sendIntent();
                break;
            }
            case R.id.bind_service:
                if (Build.VERSION.SDK_INT >= 26) {
                    startCrossProfileCustomService();
                }
                break;
            case R.id.share_via_service:
                getPersonalProfileDetailsFromService();
                break;
            case R.id.remove_profile: {
                mButtonRemoveProfile.setEnabled(false);
                removeProfile();
                break;
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
        switch (compoundButton.getId()) {
            case R.id.toggle_calculator: {
                setAppEnabled(PACKAGE_NAME_CALCULATOR, checked);
                mCalculatorEnabled = checked;
                break;
            }
            case R.id.toggle_chrome: {
                setAppEnabled(PACKAGE_NAME_CHROME, checked);
                mChromeEnabled = checked;
                break;
            }
        }
    }

    /**
     * Enables or disables the specified app in this profile.
     *
     * @param packageName The package name of the target app.
     * @param enabled     Pass true to enable the app.
     */
    private void setAppEnabled(String packageName, boolean enabled) {
        Activity activity = getActivity();
        if (null == activity) {
            return;
        }
        PackageManager packageManager = activity.getPackageManager();
        DevicePolicyManager devicePolicyManager =
                (DevicePolicyManager) activity.getSystemService(Context.DEVICE_POLICY_SERVICE);
        try {
            int packageFlags;
            if(Build.VERSION.SDK_INT < 24){
                //noinspection deprecation
                packageFlags = PackageManager.GET_UNINSTALLED_PACKAGES;
            }else{
                packageFlags = PackageManager.MATCH_UNINSTALLED_PACKAGES;
            }
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName,
                    packageFlags);
            // Here, we check the ApplicationInfo of the target app, and see if the flags have
            // ApplicationInfo.FLAG_INSTALLED turned on using bitwise operation.
            if (0 == (applicationInfo.flags & ApplicationInfo.FLAG_INSTALLED)) {
                // If the app is not installed in this profile, we can enable it by
                // DPM.enableSystemApp
                if (enabled) {
                    devicePolicyManager.enableSystemApp(
                            BasicDeviceAdminReceiver.getComponentName(activity), packageName);
                } else {
                    // But we cannot disable the app since it is already disabled
                    Log.e(TAG, "Cannot disable this app: " + packageName);
                    return;
                }
            } else {
                // If the app is already installed, we can enable or disable it by
                // DPM.setApplicationHidden
                devicePolicyManager.setApplicationHidden(
                        BasicDeviceAdminReceiver.getComponentName(activity), packageName, !enabled);
            }
            Toast.makeText(activity, enabled ? R.string.enabled : R.string.disabled,
                    Toast.LENGTH_SHORT).show();
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "The app cannot be found: " + packageName, e);
        }
    }

    /**
     * Sets restrictions to Chrome
     */
    private void setChromeRestrictions() {
        final Activity activity = getActivity();
        if (null == activity) {
            return;
        }
        final DevicePolicyManager manager =
            (DevicePolicyManager) activity.getSystemService(Context.DEVICE_POLICY_SERVICE);
        final Bundle settings = new Bundle();
        settings.putString("EditBookmarksEnabled", "false");
        settings.putString("IncognitoModeAvailability", "1");
        settings.putString("ManagedBookmarks",
                           "[{\"name\": \"Chromium\", \"url\": \"http://chromium.org\"}, " +
                           "{\"name\": \"Google\", \"url\": \"https://www.google.com\"}]");
        settings.putString("DefaultSearchProviderEnabled", "true");
        settings.putString("DefaultSearchProviderName", "\"LMGTFY\"");
        settings.putString("DefaultSearchProviderSearchURL",
                "\"http://lmgtfy.com/?q={searchTerms}\"");
        settings.putString("URLBlacklist", "[\"example.com\", \"example.org\"]");
        StringBuilder message = new StringBuilder("Setting Chrome restrictions:");
        for (String key : settings.keySet()) {
            message.append("\n");
            message.append(key);
            message.append(": ");
            message.append(settings.getString(key));
        }
        ScrollView view = new ScrollView(activity);
        TextView text = new TextView(activity);
        text.setText(message);
        int size = (int) activity.getResources().getDimension(R.dimen.activity_horizontal_margin);
        view.setPadding(size, size, size, size);
        view.addView(text);
        new AlertDialog.Builder(activity)
                .setView(view)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // This is how you can set restrictions to an app.
                        // The format for settings in Bundle differs from app to app.
                        manager.setApplicationRestrictions
                                (BasicDeviceAdminReceiver.getComponentName(activity),
                                        PACKAGE_NAME_CHROME, settings);
                        Toast.makeText(activity, R.string.restrictions_set,
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    /**
     * Clears restrictions to Chrome
     */
    private void clearChromeRestrictions() {
        final Activity activity = getActivity();
        if (null == activity) {
            return;
        }
        final DevicePolicyManager manager =
                (DevicePolicyManager) activity.getSystemService(Context.DEVICE_POLICY_SERVICE);
        // In order to clear restrictions, pass null as the restriction Bundle for
        // setApplicationRestrictions
        manager.setApplicationRestrictions
                (BasicDeviceAdminReceiver.getComponentName(activity),
                        PACKAGE_NAME_CHROME, null);
        Toast.makeText(activity, R.string.cleared, Toast.LENGTH_SHORT).show();
    }

    /**
     * Enables forwarding of share intent between private account and managed profile.
     */
    private void enableForwarding() {
        Activity activity = getActivity();
        if (null == activity || activity.isFinishing()) {
            return;
        }
        DevicePolicyManager manager =
                (DevicePolicyManager) activity.getSystemService(Context.DEVICE_POLICY_SERVICE);
        try {
            IntentFilter filter = new IntentFilter(Intent.ACTION_SEND);
            filter.addAction(CUSTOM_INTENT_ACTION);
            filter.addAction(CUSTOM_INTENT_ACTION2);
            filter.addDataType("text/plain");
            filter.addDataType("image/jpeg");
            // This is how you can register an IntentFilter as allowed pattern of Intent forwarding
            manager.addCrossProfileIntentFilter(BasicDeviceAdminReceiver.getComponentName(activity),
                    filter, FLAG_MANAGED_CAN_ACCESS_PARENT | FLAG_PARENT_CAN_ACCESS_MANAGED);
        } catch (IntentFilter.MalformedMimeTypeException e) {
            e.printStackTrace();
        }
    }

    /**
     * Disables forwarding of all intents.
     */
    private void disableForwarding() {
        Activity activity = getActivity();
        if (null == activity || activity.isFinishing()) {
            return;
        }
        DevicePolicyManager manager =
                (DevicePolicyManager) activity.getSystemService(Context.DEVICE_POLICY_SERVICE);
        manager.clearCrossProfileIntentFilters(BasicDeviceAdminReceiver.getComponentName(activity));
    }

    /**
     * Sends a sample intent of a plain text message.  This is just a utility function to see how
     * the intent forwarding works.
     */
    private void sendIntent() {
        /*Activity activity = getActivity();
        if (null == activity || activity.isFinishing()) {
            return;
        }
        DevicePolicyManager manager =
                (DevicePolicyManager) activity.getSystemService(Context.DEVICE_POLICY_SERVICE);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT,
                manager.isProfileOwnerApp(activity.getApplicationContext().getPackageName())
                        ? "From the managed account" : "From the primary account");
        try {
            startActivity(intent);
            Log.d(TAG, "A sample intent was sent.");
        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
        }*/

        shareFile();
    }

    @RequiresApi(api = 26)
    private void startCrossProfileCustomService(){
        //Not used. Requires COMP mode
        DevicePolicyManager manager =
                (DevicePolicyManager) getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE);
        List<UserHandle> users = manager.getBindDeviceAdminTargetUsers(BasicDeviceAdminReceiver.getComponentName(getActivity()));
        Log.d("MINE", "users are: " + users);

        ServiceConnection serviceConnection = new ServiceConnection() {
            @Override
            public  void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                Log.d(TAG, "onServiceConnected() called");
                Toast.makeText(getActivity(), "Service connected", Toast.LENGTH_SHORT).show();
                service = IAIDLServiceInterface.Stub.asInterface(iBinder);
            }

            @Override
            public  void onServiceDisconnected(ComponentName componentName) {
                Log.e(TAG, "onServiceDisconnected() called");
                getActivity().unbindService(this);
            }
        };

        Intent mServiceIntent = new Intent(getActivity(), WorkProfileService.class);


        if (users.size() > 0) {
            boolean isServiceBound = manager.bindDeviceAdminServiceAsUser(
                    BasicDeviceAdminReceiver.getComponentName(getActivity()),
                    mServiceIntent,
                    serviceConnection,
                    Context.BIND_AUTO_CREATE,
                    users.get(0)); // TODO: change after checking users list. Should be only one

            Log.d("MINE", "bindDeviceAdminServiceAsUser: " + isServiceBound);
        }

    }


    private void getPersonalProfileDetailsFromService(){
        //Not used
        if(service != null){
            try {
                Toast.makeText(getActivity(), "Data: " + service.getPersonalProfileData(), Toast.LENGTH_SHORT).show();
            } catch (RemoteException e) {
                Log.d("MINE","getPersonalProfileDetailsFromService() "+e.getMessage());
                e.printStackTrace();
            }

        }
    }

    /**
     * Wipes out all the data related to this managed profile.
     */
    private void removeProfile() {
        Activity activity = getActivity();
        if (null == activity || activity.isFinishing()) {
            return;
        }
        DevicePolicyManager manager =
                (DevicePolicyManager) activity.getSystemService(Context.DEVICE_POLICY_SERVICE);
        manager.wipeData(0);
        // The screen turns off here
    }

    private void shareFile() {
        File file = new File(getActivity().getFilesDir(), "media");
        File sampleFile = new File(file, SAMPLE_FILE_NAME);
        Uri contentUri = getUriForFile(getActivity(), FILE_PROVIDER_AUTHORITY, sampleFile);

        Intent intent = new Intent();
        intent.setAction(Constants.CUSTOM_INTENT_ACTION);
        intent.setDataAndType(contentUri, "text/plain");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);


        PackageManager packageManager = getActivity().getPackageManager();

        /*List<ResolveInfo> currentResolveList = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (int i = currentResolveList.size() - 1; i >= 0; i--) {
            if (currentResolveList.get(i).activityInfo.packageName.equals(getActivity().getApplicationContext().getPackageName())) {
                currentResolveList.remove(i);
                break;
            }
        }*/

        packageManager.setComponentEnabledSetting(new ComponentName(this.getActivity(), com.example.android.basicmanagedprofile.ShareFileActivity.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent);
        }

    }


    private void saveFile(){
        File file = new File(getActivity().getFilesDir(), "media");

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

        String filetext = "Some app data read from a file, saved and shared from the WORK profile";

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
