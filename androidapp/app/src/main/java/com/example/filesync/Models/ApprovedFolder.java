package com.example.filesync.Models;

import android.net.Uri;

public class ApprovedFolder {
    Uri folderUri;
    String folderName;
    boolean isUploading;
    int uploadedFiles=0;
    int pendingUploadFiles=0;
    int totalFiles=0;

    public ApprovedFolder(Uri folderUri, String folderName, boolean isUploading,int totalFiles, int uploadedFiles, int pendingUploadFiles) {
        this.folderUri = folderUri;
        this.folderName = folderName;
        this.isUploading = isUploading;
        this.uploadedFiles = uploadedFiles;
        this.pendingUploadFiles = pendingUploadFiles;
        this.totalFiles=totalFiles;
    }
    public ApprovedFolder(){

    }


    public int getTotalFiles() {
        return totalFiles;
    }

    public void setTotalFiles(int totalFiles) {
        this.totalFiles = totalFiles;
    }

    public Uri getFolderUri() {
        return folderUri;
    }

    public void setFolderUri(Uri folderUri) {
        this.folderUri = folderUri;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public boolean isUploading() {
        return isUploading;
    }

    public void setUploading(boolean uploading) {
        isUploading = uploading;
    }

    public int getUploadedFiles() {
        return uploadedFiles;
    }

    public void setUploadedFiles(int uploadedFiles) {
        this.uploadedFiles = uploadedFiles;
    }

    public int getPendingUploadFiles() {
        return pendingUploadFiles;
    }

    public void setPendingUploadFiles(int pendingUploadFiles) {
        this.pendingUploadFiles = pendingUploadFiles;
    }
}
