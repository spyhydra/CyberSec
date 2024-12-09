package com.example.cybersec;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.util.Log;
import android.content.pm.PackageManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import com.example.cybersec.R;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.ConcurrentLoginPermission;
import org.apache.ftpserver.usermanager.impl.TransferRatePermission;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ftpUse extends AppCompatActivity {

    private static final int PICK_FOLDER_REQUEST = 1;
    private static final int PORT = 2121;
    private static final String TAG = "FtpServer";

    private Button btnSelectFolder;
    private Button btnStartServer;
    private Button btnStopServer;
    private TextView tvSelectedFolder;
    private TextView tvServerStatus;

    private Uri selectedFolderUri;
    private FtpServer ftpServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_ftp);

        btnSelectFolder = findViewById(R.id.btnSelectFolder);
        btnStartServer = findViewById(R.id.btnStartServer);
        btnStopServer = findViewById(R.id.btnStopServer);
        tvSelectedFolder = findViewById(R.id.tvSelectedFolder);
        tvServerStatus = findViewById(R.id.tvServerStatus);

        btnSelectFolder.setOnClickListener(v -> openFolderPicker());
        btnStartServer.setOnClickListener(v -> startFtpServer());
        btnStopServer.setOnClickListener(v -> stopFtpServer());

        requestPermissions();
    }

    //show ip address
    private String getLocalIpAddress() {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface networkInterface : interfaces) {
                List<InetAddress> addresses = Collections.list(networkInterface.getInetAddresses());
                for (InetAddress address : addresses) {
                    if (!address.isLoopbackAddress()) {
                        String ipAddress = address.getHostAddress();
                        if (ipAddress.indexOf(':') < 0) { // Ignore IPv6 addresses
                            return ipAddress;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Unable to get local IP address", e);
        }
        return null;
    }



    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    private void openFolderPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        startActivityForResult(intent, PICK_FOLDER_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FOLDER_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedFolderUri = data.getData();
            if (selectedFolderUri != null) {
                getContentResolver().takePersistableUriPermission(selectedFolderUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                Log.d(TAG, "Selected folder URI: " + selectedFolderUri);
                DocumentFile pickedDir = DocumentFile.fromTreeUri(this, selectedFolderUri);
                if (pickedDir != null && pickedDir.canRead()) {
                    tvSelectedFolder.setText("Selected folder: " + pickedDir.getName());
                } else {
                    Toast.makeText(this, "Cannot access selected folder", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e(TAG, "Error: selectedFolderUri is null");
                Toast.makeText(this, "Error: Unable to retrieve folder URI", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e(TAG, "Error: Intent data is null or result is not OK");
            Toast.makeText(this, "Error: Unable to retrieve selected folder", Toast.LENGTH_SHORT).show();
        }
    }

    private void startFtpServer() {



        if (selectedFolderUri == null) {
            Toast.makeText(this, "Please select a folder first", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            FtpServerFactory serverFactory = new FtpServerFactory();
            ListenerFactory factory = new ListenerFactory();
            factory.setPort(PORT);

            // Enable passive mode with a range of ports
            factory.setPort(2121);

            serverFactory.addListener("default", factory.createListener());

            BaseUser user = new BaseUser();
            user.setName("user");
            user.setPassword("pass");
            user.setHomeDirectory("/");

            List<Authority> authorities = new ArrayList<>();
            authorities.add(new WritePermission());
            authorities.add(new ConcurrentLoginPermission(2, 2));
            authorities.add(new TransferRatePermission(90000, 90000));
            user.setAuthorities(authorities);

            serverFactory.getUserManager().save(user);

            serverFactory.setFileSystem(new AndroidFileSystemFactory(this, selectedFolderUri));

            ftpServer = serverFactory.createServer();
            ftpServer.start();

            String ipAddress = getLocalIpAddress();
            if (ipAddress != null) {
                tvServerStatus.setText("FTP Server is running on ftp://" + ipAddress + ":" + PORT);
            } else {
                tvServerStatus.setText("FTP Server is running on port " + PORT + " (IP Address not available)");
            }

            Toast.makeText(this, "FTP Server started", Toast.LENGTH_SHORT).show();

        } catch (FtpException e) {
            Log.e(TAG, "Failed to start FTP server", e);
            Toast.makeText(this, "Failed to start FTP server", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopFtpServer() {
        if (ftpServer != null) {
            ftpServer.stop();
            tvServerStatus.setText("FTP Server is stopped");
            Toast.makeText(this, "FTP Server stopped", Toast.LENGTH_SHORT).show();
        }
    }
}