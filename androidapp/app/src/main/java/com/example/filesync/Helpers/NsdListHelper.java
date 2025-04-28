package com.example.filesync.Helpers;
import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class NsdListHelper {
    private Context context;
    private NsdManager nsdManager;
    private NsdManager.DiscoveryListener discoveryListener;
    private List<NsdServiceInfo> serviceList = new ArrayList<>();
    private static final String SERVICE_TYPE = "_filesync._tcp.";  // Match the type from your Electron app

    public interface DiscoveryCallback {
        void onDeviceListUpdated(List<NsdServiceInfo> updatedList);
    }

    private DiscoveryCallback callback;

    public NsdListHelper(Context context, DiscoveryCallback callback) {
        this.context = context;
        this.callback = callback;
        nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
    }

    public void initializeDiscoveryListener() {
        discoveryListener = new NsdManager.DiscoveryListener() {
            @Override
            public void onDiscoveryStarted(String serviceType) {
                Log.d("NsdHelper", "Service discovery started");
                serviceList.clear();
            }

            @Override
            public void onServiceFound(NsdServiceInfo serviceInfo) {
                Log.d("NsdHelper", "Service found: " + serviceInfo.getServiceName()+" at "+serviceInfo.getHost()+serviceInfo.getPort());

                // You can filter your serviceType here if needed
                serviceList.add(serviceInfo);

                if (callback != null) {
                    callback.onDeviceListUpdated(new ArrayList<>(serviceList)); // send updated list
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo serviceInfo) {
                Log.d("NsdHelper", "Service lost: " + serviceInfo.getServiceName());
                serviceList.remove(serviceInfo);

                if (callback != null) {
                    callback.onDeviceListUpdated(new ArrayList<>(serviceList)); // update
                }
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.d("NsdHelper", "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e("NsdHelper", "Discovery failed: Error code:" + errorCode);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e("NsdHelper", "Stop Discovery failed: Error code:" + errorCode);
            }
        };
    }

    public void discoverServices() {
        initializeDiscoveryListener();
        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
    }

    public void stopDiscovery() {
        if (discoveryListener != null) {
            nsdManager.stopServiceDiscovery(discoveryListener);
        }
    }
}
