package com.example.filesync.Helpers;

import android.content.Context;
import android.net.Uri;
import android.util.Log;


import androidx.documentfile.provider.DocumentFile;

import com.example.filesync.Storage.FolderUriManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FileSyncHelper {

    Context context;
    public FileSyncHelper(Context context) {
        this.context=context;
    }
    // Method to get files info from multiple folder URIs
    public List<Map<String, Object>> getFilesFromMultipleUris(Set<Uri> folderUris) {
        List<Map<String, Object>> filesList = new ArrayList<>();

        // Iterate through all folder URIs
        for (Uri folderUri : folderUris) {
            // Use DocumentFile to access the folder
            DocumentFile folder = DocumentFile.fromTreeUri(context, folderUri);

            if (folder != null && folder.exists() && folder.isDirectory()) {
                // List all files in the folder
                DocumentFile[] files = folder.listFiles();
                if (files != null) {
                    for (DocumentFile file : files) {
                        if (file.isFile()) {
                            Map<String, Object> fileInfo = new HashMap<>();
                            fileInfo.put("fileName", file.getName());
                            fileInfo.put("updatedAt", file.lastModified()); // Unix timestamp
                            filesList.add(fileInfo);
                        }
                    }
                }
            }
        }

        return filesList;
    }
}
