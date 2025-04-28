package com.example.filesync.Helpers;
import android.os.FileObserver;
import android.util.Log;

public class FileChangeListener extends FileObserver {
    private static final String TAG = "FileChangeListener";
    private final String directoryPath;

    public interface FileChangeCallback {
        void onFileChanged(String filePath);
    }

    private final FileChangeCallback callback;

    public FileChangeListener(String path, FileChangeCallback callback) {
        super(path, FileObserver.CREATE | FileObserver.MODIFY | FileObserver.DELETE);
        this.directoryPath = path;
        this.callback = callback;
    }

    @Override
    public void onEvent(int event, String path) {
        if (path == null) return;

        String fullPath = directoryPath + "/" + path;

        switch (event) {
            case FileObserver.CREATE:
                Log.d(TAG, "File created: " + fullPath);
                callback.onFileChanged(fullPath);
                break;
            case FileObserver.MODIFY:
                Log.d(TAG, "File modified: " + fullPath);
                callback.onFileChanged(fullPath);
                break;
            case FileObserver.DELETE:
                Log.d(TAG, "File deleted: " + fullPath);
                callback.onFileChanged(fullPath);
                break;
        }
    }
}
