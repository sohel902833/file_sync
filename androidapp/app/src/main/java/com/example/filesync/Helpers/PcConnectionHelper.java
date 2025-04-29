package com.example.filesync.Helpers;

import android.app.Activity;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;

import com.example.filesync.Api.ApiClient;
import com.example.filesync.Api.ConectionRepository.ConnectionRepository;
import com.example.filesync.Models.ApprovedFolder;
import com.example.filesync.Models.ConnectionResponse;
import com.example.filesync.Models.CurrentDeviceInfo;
import com.example.filesync.Storage.ConnectionStorageManager;
import com.example.filesync.Views.MainActivity;

import java.util.List;

public class PcConnectionHelper {

    Activity activity;
    NsdHelper nsdHelper;
    private  String ipAddress;
    private String deviceName;
    private String baseUrl;
    private  int port;
    public PcConnectionHelper(Activity activity){
        this.activity=activity;
        this.nsdHelper = new NsdHelper(activity);
    }

    public void listen(ConnectionCallBack callBack){
        nsdHelper.discoverServices(new NsdHelper.NsdFoundCallback() {
            @Override
            public void onServiceFound(String ipAddress, int port, String deviceName) {
                Log.d("MainActivity", "Found device at IP: " + ipAddress + " Port: " + port);
                PcConnectionHelper.this.ipAddress=ipAddress;
                PcConnectionHelper.this.port=port;
                PcConnectionHelper.this.deviceName=deviceName;
                PcConnectionHelper.this.baseUrl= ApiClient.generateBaseUrl(ipAddress,port);

                callBack.onDeviceFound(ipAddress,port,deviceName);
                checkConnection(baseUrl,callBack);
                // ✅ Save IP and port in SharedPreferences for later
                // ✅ Start calling your Express API from Android
            }
        });
    }

    private void checkConnection(String baseUrl,ConnectionCallBack callBack){
//        runOnUiThread(()->connectionTv.setText("Checking Connection..."));
        callBack.onStartConnecting();
        ConnectionRepository connectionRepository=new ConnectionRepository(baseUrl);
        CurrentDeviceInfo deviceInfo=
                CurrentDeviceInfo.generateDeviceInfo(activity);
        connectionRepository.checkConnection(deviceInfo, new ConnectionRepository.ConnectionCallback() {
            @Override
            public void onConnectionResponse(ConnectionResponse response) {
                if(response.isConnected()){
                    callBack.onConnected();
                    saveConneectionInfo();
                }else{
                    makeConnection(baseUrl,callBack);
                }
            }
        });
    }

    private void makeConnection(String baseUrl,ConnectionCallBack callBack){
        callBack.onStartConnecting();
        ConnectionRepository connectionRepository=new ConnectionRepository(baseUrl);
        CurrentDeviceInfo deviceInfo=
                CurrentDeviceInfo.generateDeviceInfo(activity);
        connectionRepository.setConnection(deviceInfo, new ConnectionRepository.ConnectionCallback() {
            @Override
            public void onConnectionResponse(ConnectionResponse response) {
                if(response.isConnected()){
                  callBack.onConnected();
                    saveConneectionInfo();
                }else{
                    callBack.onConnectionFailed();
                }
            }
        });
    }

    private void saveConneectionInfo() {
        ConnectionStorageManager storageManager = new ConnectionStorageManager(activity);
        storageManager.saveConnectionInfo(ipAddress, port,deviceName);
    }

    public interface ConnectionCallBack {
        void onConnected();
        void onDeviceFound(String ipAddress, int port, String deviceName);
        void onStartConnecting();
        void onConnectionFailed();
        void onDisConnected();
    }
}
