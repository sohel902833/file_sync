package com.example.filesync.Storage;

import android.app.Activity;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FileSnapshotStorageHelper {
    private String SNAPSHOT_FILE_NAME="file_snapshot.json";
    private Activity context;
    public FileSnapshotStorageHelper(Activity activity,String folderName) {
        if(!folderName.isEmpty()){
            this.SNAPSHOT_FILE_NAME= folderName.toLowerCase()+".json";
        }
        this.context = activity;
    }

    public void saveSnapshot(Map<String, Long> snapshot,String fileName) {
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

    public Map<String, Long> loadSnapshot() {
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
