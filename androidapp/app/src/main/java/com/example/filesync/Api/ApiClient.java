package com.example.filesync.Api;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static Retrofit retrofit = null;

    public static ApiService getApiService(String BASE_URL) {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
    public static String generateBaseUrl(String ip,int port) {
          String baseUrl="http://"+ip+":"+port+"/api/";
          return baseUrl;
    }
}
