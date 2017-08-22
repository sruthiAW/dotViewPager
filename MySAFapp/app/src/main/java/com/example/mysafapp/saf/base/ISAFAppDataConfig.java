/*******************************************************************************
 * Copyright (C) 2017 AirWatch, LLC. All rights reserved.
 * This product is protected by copyright and intellectual property laws in the United States and other countries as well as by international treaties.
 * AirWatch products may be covered by one or more patents listed at http://www.vmware.com/go/patents.
 *
 *
 *******************************************************************************/
package com.airwatch.contentlocker.saf.base;

/**
 * Created by alaxminarayan on 5/24/17.
 */

public interface ISAFAppDataConfig {

    String getApplicationTitle();

    int getDocumentProviderFlagSupportForApplication();

    String getApplicationSummary();

    int getApplicationDocumentId();

    Object getApplicationIcon();

}
