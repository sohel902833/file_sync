package com.example.filesync.Helpers;

import android.app.Activity;
import android.net.Uri;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;

import com.example.filesync.Api.ApiClient;
import com.example.filesync.Api.ConectionRepository.ConnectionRepository;
import com.example.filesync.Models.CurrentDeviceInfo;
import com.example.filesync.Storage.ConnectionStorageManager;
import com.example.filesync.Storage.FileSnapshotStorageHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class FileUploadHelpers {
    private String folderName;
    Activity activity;
    private ExecutorService executorService;
    ConnectionRepository connectionRepository;
    CurrentDeviceInfo currentDeviceInfo;
    FileSnapshotStorageHelper snapshotStorageHelper;

    public FileUploadHelpers(Activity activity,String folderName) {
        this.folderName = folderName;
        this.activity = activity;
        this.executorService = Executors.newFixedThreadPool(2);
        this.snapshotStorageHelper=new FileSnapshotStorageHelper(activity,folderName);
        ConnectionStorageManager connectionStorageManager=new ConnectionStorageManager(activity);
        if(connectionStorageManager.getIp()!=null && !connectionStorageManager.getIp().isEmpty()){
            String baseUrl= ApiClient.generateBaseUrl(connectionStorageManager.getIp(),connectionStorageManager.getPort());
            this.connectionRepository=new ConnectionRepository(baseUrl);
            this.currentDeviceInfo= CurrentDeviceInfo.generateDeviceInfo(activity);
        }


    }

    private File createTempFileFromUri(Uri uri,String fileName) throws IOException {
        InputStream inputStream = activity.getContentResolver().openInputStream(uri);
        if (inputStream == null) {
            throw new IOException("Unable to open input stream from URI: " + uri);
        }

        File tempFile = new File(activity.getCacheDir(), fileName);

        OutputStream outputStream = new FileOutputStream(tempFile);
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        inputStream.close();
        outputStream.close();

        return tempFile;
    }
    public void start( List<DocumentFile> pendingToUploadFiles,UploaderCallBack callBack){

        List<MultipartBody.Part> fileParts = new ArrayList<>();
        Map<String, Long> fileSnapshots=new HashMap<>();
        Runnable uploadTask = () -> {
            try {
                if(!pendingToUploadFiles.isEmpty()){
                    callBack.onUploadingStarted(pendingToUploadFiles.size(),FileUploadHelpers.this.folderName);
                }
                for(DocumentFile file:pendingToUploadFiles){
                    Uri fileUri = file.getUri();
                    try {
                        File tempFile = createTempFileFromUri(fileUri,file.getName());
                        RequestBody requestFile = RequestBody.create(MediaType.parse("application/octet-stream"), tempFile);
                        MultipartBody.Part part = MultipartBody.Part.createFormData(
                                "files",
                                tempFile.getName(),
                                requestFile
                        );
                        fileParts.add(part);
                        fileSnapshots.put(file.getName(),file.lastModified());
                        Log.d("Gnerating Multipart body","Generating multipart body --->"+folderName);
                    } catch (IOException e) {
                        Log.e("Upload", "Failed to copy file from Uri: " + fileUri, e);
                    }
                }
                uploadFilesInChunks(fileParts,fileSnapshots,callBack);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        executorService.submit(uploadTask);


    }

    private void uploadFilesInChunks(List<MultipartBody.Part> fileParts,Map<String, Long> allFileSnapshots,UploaderCallBack callBack) {
        if(connectionRepository!=null && !fileParts.isEmpty()){
            connectionRepository.uploadFilesInChunks(currentDeviceInfo, fileParts,folderName, new ConnectionRepository.FileUploadCallBack() {
                @Override
                public void onFileUploaded(Map<String, String> uploadedFileMap) {
                   saveUploadedInfo(allFileSnapshots,uploadedFileMap);
                   callBack.onUploadCompleted(uploadedFileMap.size(),FileUploadHelpers.this.folderName);
                }

                @Override
                public void onChunkUploaded(Map<String, String> uploadedFileMap) {
                    saveUploadedInfo(allFileSnapshots,uploadedFileMap);
                    callBack.onFolderChunkUploaded(uploadedFileMap.size(),FileUploadHelpers.this.folderName);
                }
            });
        }
    }

    private void saveUploadedInfo(Map<String, Long> allFileSnapshots,Map<String, String> uploadedFileMap){
        Map<String, Long> upLoadedFileSnapshot=new HashMap<>();
        for (Map.Entry<String, String> entry : uploadedFileMap.entrySet()) {
            String key = entry.getKey();
            if(allFileSnapshots.containsKey(key)){
                upLoadedFileSnapshot.put(key,allFileSnapshots.get(key));
            }else{
                Log.d("File Not Found On Snapshot Map", "File Not Found On Snapshot Map");
            }
        }
        snapshotStorageHelper.saveSnapshot(upLoadedFileSnapshot);
        Log.d("File Saved On Snapshot Map", "File Saved On Snapshot Map");
        Log.d("Posted File Size ", ""+allFileSnapshots.size());
        Log.d("Uploaded File Size ", ""+uploadedFileMap.size());
    }


    public interface UploaderCallBack {
        void onUploadingStarted(int fileSize,String folderName);
        void onUploadCompleted(int fileSize,String folderName);
        void onFolderChunkUploaded(int fileSize,String folderName);
    }

}
