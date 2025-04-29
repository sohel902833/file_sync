package com.example.filesync.Helpers;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.core.util.Consumer;
import androidx.documentfile.provider.DocumentFile;

import com.example.filesync.Models.ApprovedFolder;
import com.example.filesync.Storage.FileSnapshotStorageHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ApprovedFolderHelpers {
    Activity activity;

    public ApprovedFolderHelpers(Activity activity){
        this.activity=activity;
    }

    public static String getFolderNameFromUri(Context context, Uri uri) {
        DocumentFile folder = DocumentFile.fromTreeUri(context, uri);
        if (folder != null && folder.getName() != null) {
            return folder.getName();
        }
        return "UnknownFolder";
    }

    private List<ApprovedFolder> prepareApprovedFolderList(Set<Uri> folderUris,FolderListCallback callback){
        List<ApprovedFolder> fileList = new ArrayList<>();
        List<DocumentFile> folderList=new ArrayList<>();

        //prepare the folder only and respond instant
        for (Uri folderUri : folderUris) {
            DocumentFile folder = DocumentFile.fromTreeUri(activity, folderUri);
            if (folder != null && folder.exists() && folder.isDirectory()) {
                folderList.add(folder);
                String folderName=getFolderNameFromUri(activity,folderUri);
                final int totalFiles=folder.listFiles().length;
                ApprovedFolder approvedFolder=new ApprovedFolder(folderUri,folderName,false,totalFiles,0,0);
                fileList.add(approvedFolder);
            }
        }
        //folder list is ready show on the ui
        callback.onFolderListPrepared(fileList);
        for (int i = 0; i < folderList.size(); i++) {
            List<DocumentFile> uploadedFiles = new ArrayList<>();
            List<DocumentFile> pendingToUploadFiles = new ArrayList<>();
            DocumentFile folder = folderList.get(i);
            ApprovedFolder approvedFolder=fileList.get(i);
            String folderName=approvedFolder.getFolderName();
            FileSnapshotStorageHelper fileSnapshotStorageHelper=new FileSnapshotStorageHelper(activity,folderName);
            Map<String,Long> prevUploadedFiles=fileSnapshotStorageHelper.loadSnapshot();
            int totalUploaded=0;
            int pendingToUpload=0;
            for (DocumentFile file : folder.listFiles()) {
                if (file.isFile()) {
                    String fileName=file.getName();
                    callback.onCurrentStatus("Checking : "+fileName+" --> On "+folderName);
                    Log.d("ApprovedFolderList", "Checking : "+fileName+" --> On "+folderName);
                    if(prevUploadedFiles.containsKey(fileName)){
                        totalUploaded++;
                        uploadedFiles.add(file);
                    }else{
                        pendingToUpload++;
                        pendingToUploadFiles.add(file);
                    }
                    approvedFolder.setUploadedFiles(totalUploaded);
                    approvedFolder.setPendingUploadFiles(pendingToUpload);
                    callback.onItemUpdated(approvedFolder,i);
                }
            }
            callback.onFolderScanCompleted(approvedFolder,uploadedFiles,pendingToUploadFiles);
        }
        return fileList;
    }

    public List<ApprovedFolder> getApprovedFolderList(Set<Uri> folderUris,FolderListCallback callback){
          return prepareApprovedFolderList(folderUris,callback);
    }
    public void prepareApproveFolderList(Set<Uri> folderUris, FolderListCallback callback) {
        new Thread(() -> {
            List<ApprovedFolder> fileList = prepareApprovedFolderList(folderUris,callback);
           callback.onCompleted(fileList);
        }).start();
    }

    public interface FolderListCallback {
        void onFolderListPrepared(List<ApprovedFolder> folders);
        void onCurrentStatus(String text);
        void onCompleted(List<ApprovedFolder> folders);
        void onItemUpdated(ApprovedFolder folder,int position);
        void onFolderScanCompleted(ApprovedFolder folder,List<DocumentFile> uploadedFiles,List<DocumentFile> pendingToUploadFiles);
    }
}
