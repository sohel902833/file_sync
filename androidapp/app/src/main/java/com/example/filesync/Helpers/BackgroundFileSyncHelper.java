package com.example.filesync.Helpers;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.OpenableColumns;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;

import com.example.filesync.Api.ApiClient;
import com.example.filesync.Api.ConectionRepository.ConnectionRepository;
import com.example.filesync.Models.ConnectionResponse;
import com.example.filesync.Models.CurrentDeviceInfo;
import com.example.filesync.Storage.ConnectionStorageManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class BackgroundFileSyncHelper {

    private Activity context;
    private static final String SNAPSHOT_FILE_NAME = "file_snapshot.json";

    public BackgroundFileSyncHelper(Activity context) {
        this.context = context;
    }

    public interface SyncCallback {
        void onUnsyncedFilesFound(List<Map<String, Object>> unsyncedFiles);
        void onUnsyncedFileUploaded(Map<String, String> uploadedFiles);
        void onProcessingStatus(String message);
    }

    public void findUnsyncedFiles(Set<Uri> folderUris, SyncCallback callback) {
        new ScanAndCompareTask(folderUris,this.context, callback).execute();
    }

    private class ScanAndCompareTask extends AsyncTask<Void, Void, List<Map<String, Object>>> {

        private Set<Uri> folderUris;
        private SyncCallback callback;
        ConnectionRepository connectionRepository=null;
        CurrentDeviceInfo currentDeviceInfo;
        ScanAndCompareTask(Set<Uri> folderUris, Activity context, SyncCallback callback) {

            this.folderUris = folderUris;
            this.callback = callback;
            ConnectionStorageManager connectionStorageManager=new ConnectionStorageManager(context);
            if(connectionStorageManager.getIp()!=null && !connectionStorageManager.getIp().isEmpty()){
                String baseUrl= ApiClient.generateBaseUrl(connectionStorageManager.getIp(),connectionStorageManager.getPort());
                this.connectionRepository=new ConnectionRepository(baseUrl);
                this.currentDeviceInfo=CurrentDeviceInfo.generateDeviceInfo(context);
            }

        }
        private String getFolderNameFromUri(Uri uri) {
            String path = uri.getPath();
            if (path != null) {
                String[] segments = path.split("/");
                if (segments.length > 0) {
                    return segments[segments.length - 1]; // Last part is usually the folder name
                }
            }
            return "UnknownFolder";
        }
        private String getFileNameFromUri(Uri uri) {
            String result = null;
            if (uri.getScheme().equals("content")) {
                Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
                try {
                    if (cursor != null && cursor.moveToFirst()) {
                        int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                        result = cursor.getString(nameIndex);
                    }
                } finally {
                    if (cursor != null) cursor.close();
                }
            }
            if (result == null) {
                result = uri.getLastPathSegment();
            }
            return result;
        }

        private File createTempFileFromUri(Uri uri) throws IOException {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                throw new IOException("Unable to open input stream from URI: " + uri);
            }

            String fileName = getFileNameFromUri(uri); // we'll write this helper too
            File tempFile = new File(context.getCacheDir(), fileName);

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

        @Override
        protected List<Map<String, Object>> doInBackground(Void... voids) {

            List<Map<String, Object>> globalUnsyncedFiles = new ArrayList<>();

            for (Uri folderUri : folderUris) {
                String folderName=getFolderNameFromUri(folderUri);
                Log.d("Running For Folder","Running for folder --->"+folderName);
                Set<Uri> dummyUris=new HashSet<>();
               dummyUris.add(folderUri);
               Map<String, Long> previousSnapshot = loadSnapshot();
               List<Map<String,Object>> currentSnapshot = scanFiles(dummyUris);

                Log.d("File Scan Completed","Running for folder --->"+folderName+"File Size-->"+currentSnapshot.size());

               List<Map<String, Object>> unsyncedFiles = new ArrayList<>();

               for (Map<String, Object> fileInfo : currentSnapshot) {
                   String fileName = (String) fileInfo.get("fileName");
                   String filePath = (String) fileInfo.get("filePath");
                   Long updatedAt = (Long) fileInfo.get("updatedAt");

                   Long previousUpdatedAt = previousSnapshot.get(fileName);

                   if (!previousSnapshot.containsKey(fileName) || previousUpdatedAt == null || !previousUpdatedAt.equals(updatedAt)) {
                       Map<String, Object> unsyncedFileInfo = new HashMap<>();
                       unsyncedFileInfo.put("fileName", fileName);
                       unsyncedFileInfo.put("filePath", filePath); // include filePath!
                       unsyncedFileInfo.put("updatedAt", updatedAt);
                       unsyncedFiles.add(unsyncedFileInfo);
                       Log.d("Setting into unsyncedFiles","Running for folder --->"+folderName);

                   }
               }
                Log.d("Setting into unsyncedFiles","Completed folder --->"+folderName);
               globalUnsyncedFiles.addAll(unsyncedFiles);
               List<MultipartBody.Part> fileParts = new ArrayList<>();

                Log.d("Gnerating Multipart body","Generating multipart body started --->"+folderName);
               for (Map<String, Object> fileInfo : unsyncedFiles) {
                   String filePath = (String) fileInfo.get("filePath"); // This is a content Uri string
                   Uri fileUri = Uri.parse(filePath);
                   try {
                       File tempFile = createTempFileFromUri(fileUri);
                       RequestBody requestFile = RequestBody.create(MediaType.parse("application/octet-stream"), tempFile);
                       MultipartBody.Part part = MultipartBody.Part.createFormData(
                               "files",
                               tempFile.getName(),
                               requestFile
                       );
                       fileParts.add(part);
                       Log.d("Gnerating Multipart body","Generating multipart body --->"+folderName);
                   } catch (IOException e) {
                       Log.e("Upload", "Failed to copy file from Uri: " + fileUri, e);
                   }
               }

               if(!unsyncedFiles.isEmpty()){
                   Log.d("UploadRequired", "Unsynced Files Count: --->" + unsyncedFiles.size());
               }else{
                   Log.d("UploadNotRequired", "No Unsynced files found"+"File Size-->"+currentSnapshot.size());
               }


               if(connectionRepository!=null && !fileParts.isEmpty()){
                   connectionRepository.uploadFilesInChunks(currentDeviceInfo, fileParts,folderName, new ConnectionRepository.FileUploadCallBack() {
                       @Override
                       public void onFileUploaded(Map<String, String> uploadedFileMap) {
                           Map<String, Long> uploadedSnapshot=new HashMap<>();
                           for (Map<String, Object> fileInfo : currentSnapshot) {
                               String fileName = (String) fileInfo.get("fileName");
                               Long updatedAt = (Long) fileInfo.get("updatedAt");
                               if(uploadedFileMap.containsKey(fileName)){
                                   uploadedSnapshot.put(fileName, updatedAt);
                               }else {
                                   Log.d("File Not Found On Snapshot Map", "File Not Found On Snapshot Map");
                               }
                           }
                           saveSnapshot(uploadedSnapshot);
                           Log.d("File Saved On Snapshot Map", "File Saved On Snapshot Map");
                           Log.d("Posted File Size ", ""+fileParts.size());
                           Log.d("Uploaded File Size ", ""+uploadedFileMap.size());
                       }

                       @Override
                       public void onChunkUploaded(Map<String, String> uploadedFileMap) {
                           callback.onUnsyncedFileUploaded(uploadedFileMap);
                           Map<String, Long> uploadedSnapshot=new HashMap<>();
                           for (Map<String, Object> fileInfo : currentSnapshot) {
                               String fileName = (String) fileInfo.get("fileName");
                               Long updatedAt = (Long) fileInfo.get("updatedAt");
                               if(uploadedFileMap.containsKey(fileName)){
                                   uploadedSnapshot.put(fileName, updatedAt);
                               }else {
                                   Log.d("File Not Found On Snapshot Map", "File Not Found On Snapshot Map");
                               }
                           }
                           saveSnapshot(uploadedSnapshot);
                           Log.d("File Saved On Snapshot Map", "File Saved On Snapshot Map");
                           Log.d("Posted File Size ", ""+fileParts.size());
                           Log.d("Uploaded File Size ", ""+uploadedFileMap.size());

                       }
                   });
               }else{
                   Log.d("ConnectionRepository", "ConnectionRepository is null");
               }

           }
//            saveSnapshot(currentSnapshot);
            return globalUnsyncedFiles;
        }

        @Override
        protected void onPostExecute(List<Map<String, Object>> unsyncedFiles) {
            if (callback != null) {
                callback.onUnsyncedFilesFound(unsyncedFiles);
            }
        }
    }

    private List<Map<String, Object>> scanFiles(Set<Uri> folderUris) {
//        Map<String, Long> fileMap = new HashMap<>();
//
//        for (Uri folderUri : folderUris) {
//            DocumentFile folder = DocumentFile.fromTreeUri(context, folderUri);
//            if (folder != null && folder.exists() && folder.isDirectory()) {
//                for (DocumentFile file : folder.listFiles()) {
//                    if (file.isFile()) {
//                        fileMap.put(file.getName(), file.lastModified());
//                    }
//                }
//            }
//        }
//
//        return fileMap;
        List<Map<String, Object>> fileList = new ArrayList<>();

        for (Uri folderUri : folderUris) {
            DocumentFile folder = DocumentFile.fromTreeUri(context, folderUri);
            if (folder != null && folder.exists() && folder.isDirectory()) {
                int count=0;
                for (DocumentFile file : folder.listFiles()) {
                    if (file.isFile()) {
                        count++;
                        Log.d("Scanning Files","Scanning Files"+file.getName()+"-->"+count);
                        Map<String, Object> fileInfo = new HashMap<>();
                        fileInfo.put("fileName", file.getName());
                        fileInfo.put("filePath", file.getUri().toString()); // <-- important: save fileUri
                        fileInfo.put("updatedAt", file.lastModified());
                        fileList.add(fileInfo);
                    }
                }
            }
        }

        return fileList;
    }

    private void saveSnapshot(Map<String, Long> snapshot) {
        try {
            Map<String,Long> oldSnapshot=loadSnapshot();
            snapshot.putAll(oldSnapshot);
            JSONArray jsonArray = new JSONArray();
            for (Map.Entry<String, Long> entry : snapshot.entrySet()) {
                JSONObject fileObject = new JSONObject();
                fileObject.put("fileName", entry.getKey());
                fileObject.put("updatedAt", entry.getValue());
                jsonArray.put(fileObject);
            }

            File snapshotFile = new File(context.getFilesDir(), SNAPSHOT_FILE_NAME);
            FileWriter writer = new FileWriter(snapshotFile);
            writer.write(jsonArray.toString());
            writer.close();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    private Map<String, Long> loadSnapshot() {
        Map<String, Long> snapshot = new HashMap<>();
        File snapshotFile = new File(context.getFilesDir(), SNAPSHOT_FILE_NAME);
        if (!snapshotFile.exists()) return snapshot;

        try {
            BufferedReader reader = new BufferedReader(new FileReader(snapshotFile));
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
            reader.close();

            JSONArray jsonArray = new JSONArray(jsonBuilder.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject fileObject = jsonArray.getJSONObject(i);
                String fileName = fileObject.getString("fileName");
                long updatedAt = fileObject.getLong("updatedAt");
                snapshot.put(fileName, updatedAt);
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return snapshot;
    }

    // Method to log the content of the file snapshot as a string
    public void logFileSnapshotContent() {
        File snapshotFile = new File(context.getFilesDir(), SNAPSHOT_FILE_NAME);

        if (!snapshotFile.exists()) {
            Log.e("FileSyncHelper", "File snapshot does not exist.");
            return;
        }

        // Read and log the content of the file snapshot
        try {
            BufferedReader reader = new BufferedReader(new FileReader(snapshotFile));
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
            reader.close();

            // Parse the content to log it in a structured manner
            JSONArray jsonArray = new JSONArray(jsonBuilder.toString());
            Log.d("FileSyncSnapshotHelper", "File Snapshot Content: ");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject fileObject = jsonArray.getJSONObject(i);
                String fileName = fileObject.getString("fileName");
                long updatedAt = fileObject.getLong("updatedAt");
                Log.d("FileSyncSnapshotHelper", "File Name: " + fileName + ", Updated At: " + updatedAt);
            }
        } catch (IOException | org.json.JSONException e) {
            Log.e("FileSyncSnapshotHelper", "Error reading file snapshot: " + e.getMessage());
        }
    }
    public void deleteFileSnapshot() {
        File snapshotFile = new File(context.getFilesDir(), SNAPSHOT_FILE_NAME);

        if (snapshotFile.exists()) {
            boolean isDeleted = snapshotFile.delete();
            if (isDeleted) {
                Log.d("FileSyncHelperDelete", "Successfully deleted file snapshot: " + snapshotFile.getPath());
            } else {
                Log.e("FileSyncHelperDelete", "Failed to delete file snapshot: " + snapshotFile.getPath());
            }
        } else {
            Log.e("FileSyncHelperDelete", "File snapshot does not exist.");
        }
    }
}
