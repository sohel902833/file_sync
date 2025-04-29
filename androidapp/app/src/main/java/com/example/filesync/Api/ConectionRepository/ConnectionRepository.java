package com.example.filesync.Api.ConectionRepository;
import android.util.Log;


import com.example.filesync.Api.ApiClient;
import com.example.filesync.Api.ApiService;
import com.example.filesync.Models.ConnectionResponse;
import com.example.filesync.Models.CurrentDeviceInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConnectionRepository {

    private final ApiService apiService;

    public ConnectionRepository(String baseUrl) {
        this.apiService = ApiClient.getApiService(baseUrl);
    }

    public void checkConnection(CurrentDeviceInfo deviceInfo,ConnectionCallback callback) {
        apiService.getConnection(deviceInfo).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                ConnectionResponse connectionResponse=new ConnectionResponse();
                if (response.isSuccessful()) {
                    Map<String, Object> result = response.body();

                    if (result!=null && result.containsKey("connected")) {
                        Object connectedValue = result.get("connected");
                        if (connectedValue instanceof Boolean) {
                            connectionResponse.setConnected((Boolean) connectedValue);
                        } else{
                            connectionResponse.setConnected(false);
                        }
                    }
                    Log.d("ApiSuccess", "Response: " + result);
                } else {
                    connectionResponse.setConnected(false);
                    Log.d("ApiError", "Failed: " + response.message()+response.toString());
                }
                callback.onConnectionResponse(connectionResponse);
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.d("ApiError", "API ON Failed: " + t.getMessage());
            }
        });
    }

    public void setConnection(CurrentDeviceInfo deviceInfo, ConnectionCallback callback) {
        apiService.setConnection(deviceInfo).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                ConnectionResponse connectionResponse=new ConnectionResponse();
                if (response.isSuccessful()) {
                    Map<String, Object> result = response.body();

                    if (result!=null && result.containsKey("connected")) {
                        Object connectedValue = result.get("connected");
                        if (connectedValue instanceof Boolean) {
                            connectionResponse.setConnected((Boolean) connectedValue);
                        } else{
                            connectionResponse.setConnected(false);
                        }
                    }
                    Log.d("ApiSuccess", "Response: " + result);
                } else {
                    connectionResponse.setConnected(false);
                    Log.d("ApiError", "Failed: " + response.message()+response.toString());
                }
                callback.onConnectionResponse(connectionResponse);
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.d("ApiError", "API ON Failed: " + t.getMessage());
            }
        });
    }
    public void uploadFilesInChunks(CurrentDeviceInfo deviceInfo, List<MultipartBody.Part> fileParts,String folderName, FileUploadCallBack callback) {
        Log.d("ApiSuccess Files", "Total files to upload: " + fileParts.size());

        final int chunkSize = 4; // 10 files per upload
        final int totalChunks = (int) Math.ceil(fileParts.size() / (double) chunkSize);

        final int[] uploadedChunks = {0}; // to track how many chunks uploaded

        Map<String,String> uploadedFileMap=new HashMap<>();
        for (int i = 0; i < totalChunks; i++) {
            int startIndex = i * chunkSize;
            int endIndex = Math.min(startIndex + chunkSize, fileParts.size());
            List<MultipartBody.Part> chunk = new ArrayList<>(fileParts.subList(startIndex, endIndex));

            Log.d("ApiSuccess Chunk", "Uploading chunk " + (i + 1) + " with " + chunk.size() + " files");

            apiService.uploadFiles(deviceInfo.getDeviceId(),folderName, chunk).enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    uploadedChunks[0]++;
                    if (response.isSuccessful() && response.body() != null) {
                        Map<String, String> dataMap = (Map<String, String>) response.body().get("dataMap");

                        if (dataMap != null) {
                            // Merge the received data map with the global map
                            callback.onChunkUploaded(dataMap);
                            uploadedFileMap.putAll(dataMap);
                        }

                        Log.d("ApiSuccess", "Chunk " + " uploaded. Global map: " + uploadedFileMap);
                    }else{
                        Log.d("Api Failed", "Chunk Error " + "  " + response);
                    }
                    if (uploadedChunks[0] == totalChunks) {
                        Log.d("ApiSuccess", "All chunks uploaded successfully");
                        // or set based on actual response
                        callback.onFileUploaded(uploadedFileMap);
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    uploadedChunks[0]++;

                    Log.e("ApiError", "Chunk upload failed: " + t.getMessage());

                    if (uploadedChunks[0] == totalChunks) {
                        Log.d("ApiSuccess", "All chunks attempted (some may have failed)");// or set based on failure
                        callback.onFileUploaded(uploadedFileMap);
                    }
                }
            });
        }
    }

    public interface ConnectionCallback {
        void onConnectionResponse(ConnectionResponse response);
    }
    public interface FileUploadCallBack {
        void onFileUploaded(Map<String,String> uploadedFileMap);
        void onChunkUploaded(Map<String,String> uploadedFileMap);
    }
}
