package com.example.filesync.Helpers;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.util.Pair;

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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApprovedFolderHelpers {
    Activity activity;
    private ExecutorService executorService;
    public ApprovedFolderHelpers(Activity activity){
        this.activity=activity;
        this.executorService = Executors.newFixedThreadPool(2);
    }

    public static String getFolderNameFromUri(Context context, Uri uri) {
        DocumentFile folder = DocumentFile.fromTreeUri(context, uri);
        if (folder != null && folder.getName() != null) {
            return folder.getName();
        }
        return "UnknownFolder";
    }

    private Pair<List<ApprovedFolder>,CountDownLatch> prepareApprovedFolderList(Set<Uri> folderUris, FolderListCallback callback){
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
        CountDownLatch latch = new CountDownLatch(folderList.size());

        //folder list is ready show on the ui
        callback.onFolderListPrepared(fileList);
        for (int i = 0; i < folderList.size(); i++) {
            int finalI = i;
            Runnable scanTask = () -> {
                // This will run in background
                try {
                    List<DocumentFile> uploadedFiles = new ArrayList<>();
                    List<DocumentFile> pendingToUploadFiles = new ArrayList<>();
                    DocumentFile folder = folderList.get(finalI);
                    ApprovedFolder approvedFolder=fileList.get(finalI);
                    System.out.println("Scanning folder: " + approvedFolder.getFolderName() + " on thread " + Thread.currentThread().getName());
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
                            callback.onItemUpdated(approvedFolder, finalI);
                        }
                    }
                    callback.onFolderScanCompleted(approvedFolder,uploadedFiles,pendingToUploadFiles);
                    System.out.println("Finished scanning: " + folderName);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown(); // very important!
                }
            };
            executorService.submit(scanTask);
        }
        return new Pair<>(fileList,latch);
    }
    public void preparePendingFiles(Uri folderUri, FilePrepareCallback callback){

        DocumentFile folder = DocumentFile.fromTreeUri(activity, folderUri);
        if (folder != null && folder.exists() && folder.isDirectory()) {
            String folderName=getFolderNameFromUri(activity,folderUri);

            Runnable scanTask = () -> {
                // This will run in background
                try {
                    List<DocumentFile> uploadedFiles = new ArrayList<>();
                    List<DocumentFile> pendingToUploadFiles = new ArrayList<>();
                    System.out.println("Scanning folder: " + folderName + " on thread " + Thread.currentThread().getName());
                    FileSnapshotStorageHelper fileSnapshotStorageHelper=new FileSnapshotStorageHelper(activity,folderName);
                    Map<String,Long> prevUploadedFiles=fileSnapshotStorageHelper.loadSnapshot();
                    for (DocumentFile file : folder.listFiles()) {
                        if (file.isFile()) {
                            String fileName=file.getName();
                            Log.d("ApprovedFolderList", "Checking : "+fileName+" --> On "+folderName);
                            if(prevUploadedFiles.containsKey(fileName)){
                                uploadedFiles.add(file);
                            }else{
                                pendingToUploadFiles.add(file);
                            }
                        }
                    }
                    callback.onFolderScanCompleted(uploadedFiles,pendingToUploadFiles);
                    System.out.println("Finished scanning: " + folderName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
           new Thread(scanTask).start();

        }
    }
    public void prepareApproveFolderList(Set<Uri> folderUris, FolderListCallback callback) {
        Runnable task = () -> {
            try{
                Pair<List<ApprovedFolder>,CountDownLatch> result = prepareApprovedFolderList(folderUris,callback);
                result.second.await();
                callback.onCompleted(result.first);

            }catch (Exception e){
                e.printStackTrace();
            }finally {
                if(executorService!=null){
                    executorService.shutdown();
                }
            }

        };
        new Thread(task).start();
    }

    public interface FolderListCallback {
        void onFolderListPrepared(List<ApprovedFolder> folders);
        void onCurrentStatus(String text);
        void onCompleted(List<ApprovedFolder> folders);
        void onItemUpdated(ApprovedFolder folder,int position);
        void onFolderScanCompleted(ApprovedFolder folder,List<DocumentFile> uploadedFiles,List<DocumentFile> pendingToUploadFiles);
    }
    public interface FilePrepareCallback {
        void onFolderScanCompleted(List<DocumentFile> uploadedFiles,List<DocumentFile> pendingToUploadFiles);
    }
}
