package com.example.filesync.Api;

import com.example.filesync.Models.ConnectionResponse;
import com.example.filesync.Models.CurrentDeviceInfo;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ApiService {

    @POST("connection/get")
    Call<Map<String, Object>> getConnection(@Body CurrentDeviceInfo deviceInfo);

    @POST("connection/set")
    Call<Map<String, Object>> setConnection(@Body CurrentDeviceInfo deviceInfo);
    @Multipart
    @POST("upload/upload-files/{deviceId}/{folderName}")
    Call<Map<String, Object>> uploadFiles(
            @Path("deviceId") String deviceId,
            @Path("folderName") String folderName,
            @Part List<MultipartBody.Part> files
    );
}
