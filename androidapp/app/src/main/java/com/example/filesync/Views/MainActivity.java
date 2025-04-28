package com.example.filesync.Views;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.filesync.Api.ApiClient;
import com.example.filesync.Api.ConectionRepository.ConnectionRepository;
import com.example.filesync.Helpers.BackgroundFileSyncHelper;
import com.example.filesync.Helpers.FileSyncHelper;
import com.example.filesync.Helpers.NsdListHelper;
import com.example.filesync.Helpers.NsdHelper;
import com.example.filesync.Models.ConnectionResponse;
import com.example.filesync.Models.CurrentDeviceInfo;
import com.example.filesync.R;
import com.example.filesync.Storage.ConnectionStorageManager;
import com.example.filesync.Storage.FolderUriManager;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    TextView deviceNameTv;
    TextView deviceIpAndPortTV;
    TextView connectionTv;
    Button selectFolderButton;

    String ipAddress;
    int port;
    String deviceName;
    FolderUriManager folderUriManager;
    // Request code for storage permission request
    private static final int REQUEST_CODE_PERMISSION = 1001;

    // Request code for folder picker activity result
    private static final int REQUEST_CODE_PICK_FOLDER = 1002;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        init();
        listenOnNsd();


        selectFolderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestUserForPermission();
            }
        });


    }

    private void requestUserForPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            // For Android 9 (API 28) and below, request read and write storage permissions
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE_PERMISSION);
            } else {
                // Permission already granted, open the folder picker
                openFolderPicker();
            }
        } else {
            // Directly open the folder picker if version is Q (API 29) or higher
            openFolderPicker();
        }
    }

    private void openFolderPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // Allow selecting multiple folders
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION |
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION |
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION |
                Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
        startActivityForResult(intent, REQUEST_CODE_PICK_FOLDER);
    }
    private void listenOnNsd() {
        NsdHelper nsdHelper = new NsdHelper(this);

        nsdHelper.discoverServices(new NsdHelper.NsdFoundCallback() {
            @Override
            public void onServiceFound(String ipAddress, int port, String deviceName) {
                Log.d("MainActivity", "Found device at IP: " + ipAddress + " Port: " + port);
                MainActivity.this.ipAddress=ipAddress;
                MainActivity.this.port=port;
                MainActivity.this.deviceName=deviceName;
                runOnUiThread(() -> {
                    deviceNameTv.setText("Device Name: "+deviceName+"");
                    deviceIpAndPortTV.setText("At IP: "+ipAddress+":"+port+"");
                });
                String baseUrl= ApiClient.generateBaseUrl(ipAddress,port);
                checkConnection(baseUrl);
                // ✅ Save IP and port in SharedPreferences for later
                // ✅ Start calling your Express API from Android
            }
        });
    }

    private void init(){
        folderUriManager=new FolderUriManager(this);
       deviceNameTv=findViewById(R.id.device_name_TextView);
       deviceIpAndPortTV=findViewById(R.id.device_ip_port_TextView);
       connectionTv=findViewById(R.id.connection_response_TextView);
        selectFolderButton=findViewById(R.id.selectFolderButton);
    }
    private void writeConnectionTV(boolean isConnected){
            if(isConnected){
                connectionTv.setText("Connected");
            }else{
                connectionTv.setText("Not Connected");
            }
    }
    private void checkConnection(String baseUrl){
        runOnUiThread(()->connectionTv.setText("Checking Connection..."));
        ConnectionRepository connectionRepository=new ConnectionRepository(baseUrl);
        CurrentDeviceInfo deviceInfo=
                CurrentDeviceInfo.generateDeviceInfo(this);
        connectionRepository.checkConnection(deviceInfo, new ConnectionRepository.ConnectionCallback() {
            @Override
            public void onConnectionResponse(ConnectionResponse response) {
                    if(response.isConnected()){
                        writeConnectionTV(true);
                        syncFiles();
                        saveConneectionInfo();
                    }else{
                        connectionTv.setText("Connecting...");
                        makeConnection(baseUrl);
                    }
            }
        });
    }

    private void saveConneectionInfo() {
        ConnectionStorageManager storageManager = new ConnectionStorageManager(getApplicationContext());
        storageManager.saveConnectionInfo(ipAddress, port,deviceName);
    }

    private void makeConnection(String baseUrl){
        connectionTv.setText("Making Connection...");
        ConnectionRepository connectionRepository=new ConnectionRepository(baseUrl);
        CurrentDeviceInfo deviceInfo=
                CurrentDeviceInfo.generateDeviceInfo(this);
        connectionRepository.setConnection(deviceInfo, new ConnectionRepository.ConnectionCallback() {
            @Override
            public void onConnectionResponse(ConnectionResponse response) {
                    if(response.isConnected()){
                        saveConneectionInfo();
                        writeConnectionTV(true);
                        syncFiles();
                    }else{
                        connectionTv.setText("Connection Failed");
                    }
            }
        });
    }

    private void syncFiles(){
//        Set<Uri> savedUris = folderUriManager.getFolderUrisAsUris();  // Convert to Set<Uri>
//        for (Uri uri : savedUris) {
//            Log.d("Saved Folder", uri.toString());
//        }
//        Log.d("Sync Folder","Sync Folder Started");
//        BackgroundFileSyncHelper syncHelper = new BackgroundFileSyncHelper(this);
////        syncHelper.logFileSnapshotContent();
////        syncHelper.deleteFileSnapshot(); //delete was for testing purpus
//        syncHelper.findUnsyncedFiles(savedUris, new BackgroundFileSyncHelper.SyncCallback() {
//
//
//            @Override
//            public void onUnsyncedFilesFound(List<Map<String, Object>> unsyncedFiles) {
//                // This list has ONLY the new/changed files
//                for (Map<String, Object> file : unsyncedFiles) {
//                    String name = (String) file.get("fileName");
//                    Long updatedAt = (Long) file.get("updatedAt");
//                    Log.d("Unsynced", "File: " + name + " Updated: " + updatedAt);
//                }
//            }
//
//            @Override
//            public void onUnsyncedFileUploaded(Map<String, String> uploadedFiles) {
//
//            }
//
//            @Override
//            public void onProcessingStatus(String message) {
//
//            }
//        });
//        FileSyncHelper fileSyncHelper=new FileSyncHelper(this);
//        List<Map<String, Object>> filesInfo = fileSyncHelper.getFilesFromMultipleUris(savedUris);
//
//// Log the details of each file
//        for (Map<String, Object> fileInfo : filesInfo) {
//            String fileName = (String) fileInfo.get("fileName");
//            long updatedAt = (long) fileInfo.get("updatedAt");
//            String logMessage = "File: " + fileName + " | Updated At: " + updatedAt;
//
//            // Log the file info
//            Log.d("FileSync", logMessage);
//        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, now open the folder picker
                openFolderPicker();
            } else {
                // Permission denied, show a message to the user
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_PICK_FOLDER) {
            if(data!=null){
                Uri treeUri = data.getData();  // This is the URI for the selected folder

                if(treeUri!=null){
                    // Very Important: Take persistable permission
                    final int takeFlags =(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    getContentResolver().takePersistableUriPermission(treeUri, takeFlags);

                    // Now, you can store this URI in SharedPreferences for future use
                    folderUriManager.saveFolderUri(treeUri);
                    syncFiles();
                }


            }


        }
    }

}