package com.example.filesync.Views;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filesync.Adapter.ApprovedFileListAdapter;
import com.example.filesync.Helpers.ApprovedFolderHelpers;
import com.example.filesync.Helpers.FileUploadHelpers;
import com.example.filesync.Helpers.PcConnectionHelper;
import com.example.filesync.Models.ApprovedFolder;
import com.example.filesync.R;
import com.example.filesync.Storage.FolderUriManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class LandingActivity extends AppCompatActivity {
    TextView deviceNameTv,connectionResponseTV;
    RecyclerView approvedFolderListRecyclerView;
    FloatingActionButton addNewFolderInPermissionBtn;
    FolderUriManager folderUriManager;
    ApprovedFolderHelpers approvedFolderHelpers;
    ApprovedFileListAdapter approvedFileListAdapter;

    List<ApprovedFolder> approvedFolderList=new ArrayList<>();
    ProgressDialog progressDialog;
    PcConnectionHelper pcConnectionHelper;

    // Request code for storage permission request
    private static final int REQUEST_CODE_PERMISSION = 1001;

    // Request code for folder picker activity result
    private static final int REQUEST_CODE_PICK_FOLDER = 1002;
    boolean IS_DEVICE_CONNECTED=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_landing);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initUi();
        approvedFileListAdapter=new ApprovedFileListAdapter(this,approvedFolderList);
        approvedFolderListRecyclerView.setAdapter(approvedFileListAdapter);
        //prepare page data
        addNewFolderInPermissionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestUserForPermission();
            }
        });

        connectWithPc();



        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();

        connectivityManager.registerNetworkCallback(networkRequest, new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                // 🎯 Internet connection is restored here
                Log.d("NetworkCallback", "Internet connection restored");
                LandingActivity.this.IS_DEVICE_CONNECTED=false;
                connectWithPc();

            }

            @Override
            public void onLost(Network network) {
                // ❌ Internet connection lost
                Log.d("NetworkCallback", "Internet connection lost");
                connectWithPc();
            }
        });



    }
   PcConnectionHelper.ConnectionCallBack pcConnectionCalback= new PcConnectionHelper.ConnectionCallBack() {
        @Override
        public void onConnected() {
            LandingActivity.this.IS_DEVICE_CONNECTED=true;
            prepareApproveFolderList();
            runOnUiThread(()->connectionResponseTV.setText("Connected"));
        }

        @Override
        public void onDeviceFound(String ipAddress, int port, String deviceName) {
            runOnUiThread(() -> {
                deviceNameTv.setText(deviceName+" At Ip: "+ipAddress+":"+port);
            });
        }

        @Override
        public void onStartConnecting() {
            runOnUiThread(()->connectionResponseTV.setText("Checking Connection..."));
        }

        @Override
        public void onConnectionFailed() {
            LandingActivity.this.IS_DEVICE_CONNECTED=false;
            runOnUiThread(()->connectionResponseTV.setText("Connection Failed"));
        }
        @Override
        public void onDisConnected() {
            LandingActivity.this.IS_DEVICE_CONNECTED=false;
        }
    };
    private void connectWithPc() {
        connectionResponseTV.setText("Not Connected");
        deviceNameTv.setText("Finding Device..");
        pcConnectionHelper=new PcConnectionHelper(this);
        pcConnectionHelper.listen(LandingActivity.this.pcConnectionCalback);
    }

    private ApprovedFolderHelpers.FolderListCallback folderListCallback = new ApprovedFolderHelpers.FolderListCallback() {
        @Override
        public void onFolderListPrepared(List<ApprovedFolder> folders) {
            runOnUiThread(()->{
                Log.d("ApprovedFolderList", "onFolderListPrepared: "+folders.size());
                approvedFolderList.clear();
                approvedFolderList.addAll(folders);
                progressDialog.dismiss();
                approvedFileListAdapter.notifyDataSetChanged();
            });
        }

        @Override
        public void onCurrentStatus(String text) {
            runOnUiThread(()->progressDialog.setMessage(text));
        }

        @Override
        public void onCompleted(List<ApprovedFolder> folders) {
            runOnUiThread(()->{
                Toast.makeText(LandingActivity.this,"Folder Scan Completed",Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public void onItemUpdated(ApprovedFolder folder, int position) {
            runOnUiThread(()->{
                approvedFolderList.set(position,folder);
                approvedFileListAdapter.notifyItemChanged(position);
            });
        }
        @Override
        public void onFolderScanCompleted(ApprovedFolder folder, List<DocumentFile> uploadedFiles, List<DocumentFile> pendingToUploadFiles) {
            if(!pendingToUploadFiles.isEmpty()){
                uploadFiles(folder.getFolderName(),pendingToUploadFiles);
            }

        }

    };
    private void initUi() {
        approvedFolderListRecyclerView=findViewById(R.id.approvedFolderListRecyclerView);
        deviceNameTv=findViewById(R.id.device_name_TextView);
        connectionResponseTV=findViewById(R.id.connection_response_TextView);
        addNewFolderInPermissionBtn=findViewById(R.id.addNewFolderInPermissionBtn);
        approvedFolderListRecyclerView.setHasFixedSize(true);
        approvedFolderListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        progressDialog=new ProgressDialog(this);


        //initialized global services
        folderUriManager=new FolderUriManager(this);
        approvedFolderHelpers=new ApprovedFolderHelpers(this);

    }
    void prepareApproveFolderList(){
        progressDialog.setTitle("Loading Approved Folders");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
         Set<Uri> folderUris=folderUriManager.getFolderUrisAsUris();

         approvedFolderHelpers.prepareApproveFolderList(folderUris, LandingActivity.this.folderListCallback);
    }
    private void updateFileUploaded(int fileSize, String folderName){
        for(int i=0;i<approvedFolderList.size();i++){
            ApprovedFolder approvedFolder=approvedFolderList.get(i);
            if(approvedFolder.getFolderName().equals(folderName)){
                approvedFolder.setUploadedFiles(approvedFolder.getUploadedFiles()+fileSize);
                approvedFolder.setPendingUploadFiles(approvedFolder.getPendingUploadFiles()-fileSize);
                approvedFileListAdapter.notifyItemChanged(i);
                break;
            }
        }
    }
    private void uploadFiles(String folderName,List<DocumentFile> pendingToUploadFiles){
        if(LandingActivity.this.IS_DEVICE_CONNECTED){
            FileUploadHelpers uploadHelpers=new FileUploadHelpers(LandingActivity.this,folderName);
            uploadHelpers.start(pendingToUploadFiles, new FileUploadHelpers.UploaderCallBack() {
                @Override
                public void onUploadingStarted(int fileSize, String folderName) {
                    for(int i=0;i<approvedFolderList.size();i++){
                        ApprovedFolder approvedFolder=approvedFolderList.get(i);
                        if(approvedFolder.getFolderName().equals(folderName)){
                            approvedFolder.setUploading(true);
                            approvedFileListAdapter.notifyItemChanged(i);
                            break;
                        }
                    }
                }

                @Override
                public void onUploadCompleted(int fileSize, String folderName) {
                    for(int i=0;i<approvedFolderList.size();i++){
                        ApprovedFolder approvedFolder=approvedFolderList.get(i);
                        if(approvedFolder.getFolderName().equals(folderName)){
                           approvedFolder.setUploading(false);
                            approvedFileListAdapter.notifyItemChanged(i);
                            break;
                        }
                    }
                }

                @Override
                public void onFolderChunkUploaded(int fileSize, String folderName) {
                    updateFileUploaded(fileSize,folderName);
                }
            });
        }else{
            Toast.makeText(LandingActivity.this,"Device Not Connected",Toast.LENGTH_SHORT).show();
        }

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

    private void syncFiles() {
        prepareApproveFolderList();
    }
}