package com.example.filesync.Storage;
import android.content.Context;
import android.content.SharedPreferences;

public class ConnectionStorageManager {
    private static final String PREF_NAME = "connection_prefs";
    private static final String KEY_IP = "device_ip";
    private static final String KEY_PORT = "device_port";
    private static final String KEY_DEVICE_NAME = "device_name";

    private SharedPreferences sharedPreferences;

    public ConnectionStorageManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveConnectionInfo(String ip, int port, String deviceName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_IP, ip);
        editor.putInt(KEY_PORT, port);
        editor.putString(KEY_DEVICE_NAME, deviceName);
        editor.apply();
    }

    public String getIp() {
        return sharedPreferences.getString(KEY_IP, null);
    }

    public int getPort() {
        return sharedPreferences.getInt(KEY_PORT, -1);
    }

    public String getDeviceName() {
        return sharedPreferences.getString(KEY_DEVICE_NAME, null);
    }

    public void clearConnectionInfo() {
        sharedPreferences.edit().clear().apply();
    }
}
