package com.example.filesync.Helpers;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

public class NsdHelper {

    private static final String TAG = "OldNsdHelper";
    private static final String SERVICE_TYPE = "_filesync._tcp.";  // Match the type from your Electron app
    private static final String SERVICE_NAME = "FileSyncLaptop";   // Optional: if you want to match by name

    private final NsdManager nsdManager;
    private NsdManager.DiscoveryListener discoveryListener;

    public NsdHelper(Context context) {
        nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
    }

    public void discoverServices(NsdFoundCallback callback) {
        initializeDiscoveryListener(callback);
        nsdManager.discoverServices(
                SERVICE_TYPE,
                NsdManager.PROTOCOL_DNS_SD,
                discoveryListener
        );
    }

    private void initializeDiscoveryListener(NsdFoundCallback callback) {
        discoveryListener = new NsdManager.DiscoveryListener() {
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                Log.d(TAG, "Service discovery success: " + service);

                if (!service.getServiceType().equals(SERVICE_TYPE)) {
                    Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
                } else {
                    // Resolve service to get host and port
                    nsdManager.resolveService(service, new NsdManager.ResolveListener() {
                        @Override
                        public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                            Log.e(TAG, "Resolve failed: " + errorCode);
                        }

                        @Override
                        public void onServiceResolved(NsdServiceInfo serviceInfo) {
                            Log.d(TAG, "Service resolved: " + serviceInfo);
                            String hostAddress = serviceInfo.getHost().getHostAddress();
                            int port = serviceInfo.getPort();
                            String deviceName = serviceInfo.getServiceName();
                            callback.onServiceFound(hostAddress, port,deviceName);
                        }
                    });
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                Log.e(TAG, "service lost: " + service);
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                nsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Stop Discovery failed: Error code:" + errorCode);
                nsdManager.stopServiceDiscovery(this);
            }
        };
    }

    public interface NsdFoundCallback {
        void onServiceFound(String ipAddress, int port,String deviceName);
    }

    public void stopDiscovery() {
        if (discoveryListener != null) {
            nsdManager.stopServiceDiscovery(discoveryListener);
        }
    }
}
