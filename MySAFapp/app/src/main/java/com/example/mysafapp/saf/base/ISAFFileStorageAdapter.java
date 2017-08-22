/*******************************************************************************
 * Copyright (C) 2017 AirWatch, LLC. All rights reserved.
 * This product is protected by copyright and intellectual property laws in the United States and other countries as well as by international treaties.
 * AirWatch products may be covered by one or more patents listed at http://www.vmware.com/go/patents.
 *
 *
 *******************************************************************************/
package com.airwatch.contentlocker.saf.base;

import com.airwatch.contentlocker.model.ContentEntity;
import com.airwatch.contentlocker.model.Folder;
import com.airwatch.contentlocker.model.Repository;

import java.util.ArrayList;

/**
 * Created by alaxminarayan on 5/24/17.
 */

public interface ISAFFileStorageAdapter {

    static final String FOLDER = "Folder";

    static final String FILE = "File";

    static final String CATEGORY = "Category";


    ArrayList<Repository> getRootDocuments();

    ArrayList<Folder> getChildFolders(long parentId);

    ArrayList<ContentEntity> getChildContents(long parentId);

}
