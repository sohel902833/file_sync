package com.example.filesync.Storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import java.util.HashSet;
import java.util.Set;

public class FolderUriManager {

    private static final String PREF_NAME = "FolderUriPrefs";
    private static final String KEY_FOLDERS = "saved_folders";
    private SharedPreferences sharedPreferences;
    public FolderUriManager(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // Save the folder URIs to SharedPreferences
    public void saveFolderUri(Uri folderUri) {
        // Always make a fresh copy
        Set<String> folderUris = new HashSet<>(getFolderUris());
        folderUris.add(folderUri.toString()); // Save Uri as String

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(KEY_FOLDERS, folderUris);
        editor.apply();
    }

    // Retrieve the folder URIs from SharedPreferences as Strings
    public Set<String> getFolderUris() {
        return sharedPreferences.getStringSet(KEY_FOLDERS, new HashSet<>());
    }

    // Convert a Set<String> to Set<Uri>
    public Set<Uri> getFolderUrisAsUris() {
        Set<String> folderUrisStringSet = getFolderUris();
        Set<Uri> folderUris = new HashSet<>();
        for (String folderUriString : folderUrisStringSet) {
            folderUris.add(Uri.parse(folderUriString)); // Convert back to Uri
        }
        return folderUris;
    }

    // Check if a folder Uri already exists
    public boolean containsFolderUri(Uri folderUri) {
        Set<String> folderUrisStringSet = getFolderUris();
        return folderUrisStringSet.contains(folderUri.toString());
    }

    // Remove a folder Uri
    public void removeFolderUri(Uri folderUri) {
        Set<String> folderUrisStringSet = getFolderUris();
        folderUrisStringSet.remove(folderUri.toString()); // Remove the Uri

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(KEY_FOLDERS, folderUrisStringSet);
        editor.apply();
    }

    // Clear all saved folder URIs
    public void clearAllFolderUris() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_FOLDERS);
        editor.apply();
    }
}
