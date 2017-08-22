/*******************************************************************************
 * Copyright (C) 2017 AirWatch, LLC. All rights reserved.
 * This product is protected by copyright and intellectual property laws in the United States and other countries as well as by international treaties.
 * AirWatch products may be covered by one or more patents listed at http://www.vmware.com/go/patents.
 *
 *
 *******************************************************************************/
package com.airwatch.contentlocker.saf;

import android.provider.DocumentsContract;

import com.airwatch.contentlocker.R;
import com.airwatch.contentlocker.SettingFacade;
import com.airwatch.contentlocker.saf.base.ISAFAppDataConfig;

import static com.airwatch.contentlocker.ContentLockerApplication.getContext;

/**
 * Created by alaxminarayan on 5/24/17.
 */

public class SCLSAFAppDataConfig implements ISAFAppDataConfig {


    public String getApplicationTitle() {
        return getContext().getString(R.string.content);
    }

    public int getDocumentProviderFlagSupportForApplication() {
        return DocumentsContract.Root.FLAG_SUPPORTS_CREATE |
                DocumentsContract.Root.FLAG_SUPPORTS_RECENTS |
                DocumentsContract.Root.FLAG_SUPPORTS_SEARCH;
    }

    @Override
    public String getApplicationSummary() {
        return SettingFacade.getInstance().getSystemUser().getCredentials().userName;
    }

    @Override
    public int getApplicationDocumentId() {
        return 0;
    }

    @Override
    public Object getApplicationIcon() {
        return R.drawable.airwatchcontent;
    }

}
