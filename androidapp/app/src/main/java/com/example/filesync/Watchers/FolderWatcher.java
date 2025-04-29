package com.example.filesync.Watchers;


import android.app.Activity;
import android.os.FileObserver;
import android.content.Context;
import androidx.documentfile.provider.DocumentFile;
import android.net.Uri;

import com.example.filesync.Api.ApiClient;
import com.example.filesync.Api.ConectionRepository.ConnectionRepository;
import com.example.filesync.Helpers.ApprovedFolderHelpers;
import com.example.filesync.Helpers.FileUploadHelpers;
import com.example.filesync.Models.ApprovedFolder;
import com.example.filesync.Models.CurrentDeviceInfo;
import com.example.filesync.Storage.ConnectionStorageManager;
import com.example.filesync.Storage.FileSnapshotStorageHelper;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FolderWatcher {
    private final Activity context;
    private final List<Uri> folderUris;
    private final ExecutorService executorService;
    private final List<FileObserver> observers = new ArrayList<>();

    public FolderWatcher(Activity context, List<Uri> folderUris) {
        this.context = context;
        this.folderUris = folderUris;
        this.executorService = Executors.newFixedThreadPool(4);
    }

    public void startWatching() {
        for (Uri uri : folderUris) {
            DocumentFile documentFile = DocumentFile.fromTreeUri(context, uri);
            if (documentFile != null && documentFile.isDirectory() && documentFile.getUri().getPath()!=null) {
                File folder = new File(documentFile.getUri().getPath());
                startObserverForFolder(folder,uri);
            }
        }
    }

    private void startObserverForFolder(File folder,Uri folderUri) {
        FileObserver observer = new FileObserver(folder.getAbsolutePath(), FileObserver.CREATE | FileObserver.MODIFY) {
            @Override
            public void onEvent(int event, String path) {
                if (path != null) {
                    File changedFile = new File(folder, path);
                    if (event == FileObserver.CREATE || event == FileObserver.MODIFY) {
                        executorService.submit(() -> uploadFile(folderUri,folder.getName()));
                    }
                }
            }
        };
        observer.startWatching();
        observers.add(observer);
    }

    private void uploadFile(Uri folderUri,String folderName) {
        FileUploadHelpers uploadHelpers=new FileUploadHelpers(context,folderName);
        ApprovedFolderHelpers approvedFolderHelpers=new ApprovedFolderHelpers(context);
        approvedFolderHelpers.preparePendingFiles(folderUri, (uploadedFiles, pendingToUploadFiles) -> {
             if(!pendingToUploadFiles.isEmpty()){
                 uploadHelpers.start(pendingToUploadFiles, new FileUploadHelpers.UploaderCallBack() {
                     @Override
                     public void onUploadingStarted(int fileSize, String folderName) {

                     }

                     @Override
                     public void onUploadCompleted(int fileSize, String folderName) {

                     }

                     @Override
                     public void onFolderChunkUploaded(int fileSize, String folderName) {

                     }
                 });
             }
        });
    }

    public void stopWatching() {
        for (FileObserver observer : observers) {
            observer.stopWatching();
        }
        executorService.shutdown();
    }
}
