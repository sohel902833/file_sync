package com.example.filesync.Models;

import android.app.Activity;
import android.provider.Settings;

public class CurrentDeviceInfo {
    private String deviceName;
    private String deviceId;

    public CurrentDeviceInfo(){}
    public CurrentDeviceInfo(String deviceName, String deviceId) {
        this.deviceName = deviceName;
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public static  CurrentDeviceInfo generateDeviceInfo(Activity activity){
        String manufacturer = android.os.Build.MANUFACTURER;
        String model = android.os.Build.MODEL;
        String deviceName = manufacturer + " " + model;
        String androidId = Settings.Secure.getString(
                activity.getContentResolver(),
                Settings.Secure.ANDROID_ID
                );
        return new CurrentDeviceInfo(deviceName,androidId);
    }

}
