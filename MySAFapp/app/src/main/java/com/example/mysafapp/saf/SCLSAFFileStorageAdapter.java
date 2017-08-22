/*******************************************************************************
 * Copyright (C) 2017 AirWatch, LLC. All rights reserved.
 * This product is protected by copyright and intellectual property laws in the United States and other countries as well as by international treaties.
 * AirWatch products may be covered by one or more patents listed at http://www.vmware.com/go/patents.
 *
 *
 *******************************************************************************/
package com.airwatch.contentlocker.saf;

import com.airwatch.contentlocker.db.DbAdapter;
import com.airwatch.contentlocker.model.ContentEntity;
import com.airwatch.contentlocker.model.Folder;
import com.airwatch.contentlocker.model.Repository;
import com.airwatch.contentlocker.saf.base.ISAFFileStorageAdapter;

import java.util.ArrayList;

/**
 * Created by alaxminarayan on 5/24/17.
 */

public class SCLSAFFileStorageAdapter implements ISAFFileStorageAdapter {

    private DbAdapter mDB = DbAdapter.getInstance();

    @Override
    public ArrayList<Repository> getRootDocuments() {

        ArrayList<Repository> repoList = (ArrayList<Repository>) mDB.getAllVisitedWritableRepositories();

        return repoList;
    }

    @Override
    public ArrayList<Folder> getChildFolders(long parentId) {
        return mDB.getFolderByParentId(parentId);
    }

    @Override
    public ArrayList<ContentEntity> getChildContents(long parentId) {
        return mDB.getContentByFolderId(parentId, false);
    }
}
