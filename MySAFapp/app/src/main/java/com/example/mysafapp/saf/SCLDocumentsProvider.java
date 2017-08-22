/*******************************************************************************
 * Copyright (C) 2017 AirWatch, LLC. All rights reserved.
 * This product is protected by copyright and intellectual property laws in the United States and other countries as well as by international treaties.
 * AirWatch products may be covered by one or more patents listed at http://www.vmware.com/go/patents.
 *
 *
 *******************************************************************************/
package com.example.mysafapp.saf;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.DocumentsProvider;
import android.text.TextUtils;

import com.airwatch.contentlocker.model.ContentEntity;
import com.airwatch.contentlocker.model.Folder;
import com.airwatch.contentlocker.model.Repository;
import com.airwatch.contentlocker.saf.base.ISAFAppDataConfig;
import com.airwatch.contentlocker.saf.base.ISAFFileStorageAdapter;
import com.airwatch.login.timeout.SDKSessionManagerInternal;
import com.airwatch.util.Logger;

import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * Created by alaxminarayan on 5/24/17.
 */

public class SCLDocumentsProvider extends DocumentsProvider {


    private ISAFAppDataConfig sclsafAppDataConfig = new SCLSAFAppDataConfig();

    private ISAFFileStorageAdapter sclSAFFileStorageAdapter = new SCLSAFFileStorageAdapter();

    private static final String ROOT = "root";

    // Use these as the default columns to return information about a root if no specific
    // columns are requested in a query.
    private static final String[] DEFAULT_ROOT_PROJECTION = new String[]{
            DocumentsContract.Root.COLUMN_ROOT_ID,
            DocumentsContract.Root.COLUMN_MIME_TYPES,
            DocumentsContract.Root.COLUMN_FLAGS,
            DocumentsContract.Root.COLUMN_ICON,
            DocumentsContract.Root.COLUMN_TITLE,
            DocumentsContract.Root.COLUMN_SUMMARY,
            DocumentsContract.Root.COLUMN_DOCUMENT_ID,
            DocumentsContract.Root.COLUMN_AVAILABLE_BYTES
    };

    // Use these as the default columns to return information about a document if no specific
    // columns are requested in a query.
    private static final String[] DEFAULT_DOCUMENT_PROJECTION = new String[]{
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED,
            DocumentsContract.Document.COLUMN_FLAGS,
            DocumentsContract.Document.COLUMN_SIZE
    };


    /**
     * @param projection the requested root column projection
     * @return either the requested root column projection, or the default projection if the
     * requested projection is null.
     */
    private static String[] resolveRootProjection(String[] projection) {
        return projection != null ? projection : DEFAULT_ROOT_PROJECTION;
    }

    private static String[] resolveDocumentProjection(String[] projection) {
        return projection != null ? projection : DEFAULT_DOCUMENT_PROJECTION;
    }

    private boolean isUserLoggedIn() {
        Logger.d("isUserLoggedIn");

        return SDKSessionManagerInternal.getsSdkSessionDelegate(getContext()).isUserLoggedIn();
    }

    @Override
    public Cursor queryRoots(String[] projection) throws FileNotFoundException {

        Logger.d("ASCL-173693: queryRoots.");

        final MatrixCursor result = new MatrixCursor(resolveRootProjection(projection));

        // If user is not logged in, return an empty root cursor.  This removes our provider from the list entirely.
        if (!isUserLoggedIn()) {
            Logger.d("ASCL-173693: queryRoots -> user not logged in");
            return result;
        }

        Logger.d("ASCL-173693: queryRoots -> user logged in");

        final MatrixCursor.RowBuilder row = result.newRow();
        createRootRow(row);

        Logger.d("ASCL-173693: queryRoots -> root row generated.");
        return result;
    }

    /**
     * Function to create the application root row displayed on the SAF UI by Android.
     * @param row
     */
    private void createRootRow(MatrixCursor.RowBuilder row) {
        row.add(DocumentsContract.Root.COLUMN_ROOT_ID, ROOT);
        row.add(DocumentsContract.Root.COLUMN_TITLE, sclsafAppDataConfig.getApplicationTitle());

        // FLAG_SUPPORTS_CREATE means at least one directory under the root supports creating
        // documents.  FLAG_SUPPORTS_RECENTS means your application's most recently used
        // documents will show up in the "Recents" category.  FLAG_SUPPORTS_SEARCH allows users
        // to search all documents the application shares.
        row.add(DocumentsContract.Root.COLUMN_FLAGS, sclsafAppDataConfig.getDocumentProviderFlagSupportForApplication());

        // COLUMN_TITLE is the root title (e.g. what will be displayed to identify your provider).
        row.add(DocumentsContract.Root.COLUMN_SUMMARY, sclsafAppDataConfig.getApplicationSummary());

        // This document id must be unique within this provider and consistent across time.  The
        // system picker UI may save it and refer to it later.
        row.add(DocumentsContract.Root.COLUMN_DOCUMENT_ID, sclsafAppDataConfig.getApplicationDocumentId());

        row.add(DocumentsContract.Root.COLUMN_ICON, sclsafAppDataConfig.getApplicationIcon());
    }

    @Override
    public Cursor queryDocument(String documentId, String[] projection) throws FileNotFoundException {

        Logger.d("ASCL-173693: queryDocument.");

        // If user is not logged in, return an empty root cursor.  This removes our provider from the list entirely.
        if (!isUserLoggedIn()) {
            Logger.d("ASCL-173693: queryDocument -> user not logged in");
            return null;
        }

        final MatrixCursor result = new MatrixCursor(resolveDocumentProjection(projection));

        if (isRoot(documentId)) {
            final MatrixCursor.RowBuilder row = result.newRow();
            createRootRow(row);
            return result;

        } else {
            Logger.d("ASCL-173693: queryDocument -> docId not ROOT.");
        }


        return null;
    }

    private void addDocumentRow(MatrixCursor.RowBuilder row, Repository repo) {
        row.add(DocumentsContract.Document.COLUMN_DOCUMENT_ID, repo.RootFolderId + ISAFFileStorageAdapter.FOLDER);
        row.add(DocumentsContract.Document.COLUMN_MIME_TYPE, DocumentsContract.Document.MIME_TYPE_DIR);
        row.add(DocumentsContract.Document.COLUMN_DISPLAY_NAME, repo.getName());
        row.add(DocumentsContract.Document.COLUMN_LAST_MODIFIED, repo.getModifiedOn());
        row.add(DocumentsContract.Document.COLUMN_FLAGS, DocumentsContract.Document.FLAG_DIR_SUPPORTS_CREATE);
        row.add(DocumentsContract.Document.COLUMN_SIZE, 1);
    }

    private boolean isRoot(String documentId) {
        return ROOT.equalsIgnoreCase(documentId) || "0".equalsIgnoreCase(documentId);
    }

    @Override
    public Cursor queryChildDocuments(String parentDocumentId, String[] projection, String sortOrder) throws FileNotFoundException {
        // If user is not logged in, return an empty root cursor.  This removes our provider from the list entirely.
        if (!isUserLoggedIn()) {
            Logger.d("ASCL-173693: queryChildDocuments -> user not logged in");
            return null;
        }


        Logger.d("ASCL-173693: queryChildDocuments -> user is logged in.");

        final MatrixCursor result = new MatrixCursor(resolveDocumentProjection(projection));

        if (isRoot(parentDocumentId)) {
            ArrayList<Repository> repositoryList = sclSAFFileStorageAdapter.getRootDocuments();

            for (Repository repo : repositoryList) {
                final MatrixCursor.RowBuilder row = result.newRow();
                addDocumentRow(row, repo);
                Logger.d("ASCL-173693: queryChildDocuments -> row id - " + repo.Id + ", row name - " + repo.getName());
            }

            Logger.d("ASCL-173693: queryChildDocuments -> result size - " + result.getCount());
        } else {
            if(TextUtils.isEmpty(parentDocumentId)) {
                Logger.d("ASCL-173693: queryChildDocuments -> id is empty :O");
            } else {
                if(parentDocumentId.endsWith(ISAFFileStorageAdapter.FOLDER)) {
                    long parentId = getId(parentDocumentId);
                    ArrayList<Folder> folderList = sclSAFFileStorageAdapter.getChildFolders(parentId);
                    ArrayList<ContentEntity> contentList = sclSAFFileStorageAdapter.getChildContents(parentId);
                    for (Folder folder : folderList) {
                        final MatrixCursor.RowBuilder row = result.newRow();
                        addDocumentRow(row, folder);
                        Logger.d("ASCL-173693: queryChildDocuments (folder) -> row id - " + folder.getmFolderId() + ", row name - " + folder.getmName());
                    }
                    for (ContentEntity content : contentList) {
                        final MatrixCursor.RowBuilder row = result.newRow();
                        addDocumentRow(row, content);
                        Logger.d("ASCL-173693: queryChildDocuments (content) -> row id - " + content.Id + ", row name - " + content.getName());
                    }
                }
            }
        }

        return result;
    }

    private void addDocumentRow(MatrixCursor.RowBuilder row, Folder folder) {
        row.add(DocumentsContract.Document.COLUMN_DOCUMENT_ID, folder.getmFolderId() + ISAFFileStorageAdapter.FOLDER);
        row.add(DocumentsContract.Document.COLUMN_MIME_TYPE, DocumentsContract.Document.MIME_TYPE_DIR);
        row.add(DocumentsContract.Document.COLUMN_DISPLAY_NAME, folder.getmName());
        row.add(DocumentsContract.Document.COLUMN_LAST_MODIFIED, folder.getmModifiedOn());
        row.add(DocumentsContract.Document.COLUMN_FLAGS, DocumentsContract.Document.FLAG_DIR_SUPPORTS_CREATE);
        row.add(DocumentsContract.Document.COLUMN_SIZE, 1);
    }

    private void addDocumentRow(MatrixCursor.RowBuilder row, ContentEntity content) {
        row.add(DocumentsContract.Document.COLUMN_DOCUMENT_ID, content.Id + ISAFFileStorageAdapter.FILE);
        row.add(DocumentsContract.Document.COLUMN_MIME_TYPE, content.getMimeType());
        row.add(DocumentsContract.Document.COLUMN_DISPLAY_NAME, content.getName());
        row.add(DocumentsContract.Document.COLUMN_LAST_MODIFIED, content.getLastModifiedDate());
        row.add(DocumentsContract.Document.COLUMN_FLAGS, DocumentsContract.Document.FLAG_DIR_SUPPORTS_CREATE);
        row.add(DocumentsContract.Document.COLUMN_SIZE, 1);
    }

    private long getId(String Id) {
        if (TextUtils.isEmpty(Id)) {
            return 0;
        }

        int idLength = 0;
        if (Id.contains(ISAFFileStorageAdapter.FOLDER)) {
            idLength = ISAFFileStorageAdapter.FOLDER.length();
        } else if (Id.contains(ISAFFileStorageAdapter.FILE)) {
            idLength = ISAFFileStorageAdapter.FILE.length();
        } else if (Id.contains(ISAFFileStorageAdapter.CATEGORY)) {
            idLength = ISAFFileStorageAdapter.CATEGORY.length();
        }

        return Long.parseLong(Id.substring(0, Id.length() - idLength ));
    }

    @Override
    public ParcelFileDescriptor openDocument(String documentId, String mode, CancellationSignal signal) throws FileNotFoundException {
        return null;
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public String createDocument(String parentDocumentId, String mimeType, String displayName) throws FileNotFoundException {
        return super.createDocument(parentDocumentId, mimeType, displayName);
    }
}
