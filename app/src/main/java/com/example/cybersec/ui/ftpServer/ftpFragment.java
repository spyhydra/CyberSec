package com.example.cybersec.ui.ftpServer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;

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

public class ftpFragment extends Fragment {

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ftp, container, false);

        // Initialize UI components
        btnSelectFolder = view.findViewById(R.id.btnSelectFolder);
        btnStartServer = view.findViewById(R.id.btnStartServer);
        btnStopServer = view.findViewById(R.id.btnStopServer);
        tvSelectedFolder = view.findViewById(R.id.tvSelectedFolder);
        tvServerStatus = view.findViewById(R.id.tvServerStatus);

        // Set click listeners
        btnSelectFolder.setOnClickListener(v -> openFolderPicker());
        btnStartServer.setOnClickListener(v -> startFtpServer());
        btnStopServer.setOnClickListener(v -> stopFtpServer());

        // Request permissions
        requestPermissions();

        return view;
    }

    // Get local IP address method remains the same
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
            // Check and request storage permissions
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
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
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FOLDER_REQUEST && resultCode == AppCompatActivity.RESULT_OK && data != null) {
            selectedFolderUri = data.getData();
            if (selectedFolderUri != null) {
                requireContext().getContentResolver().takePersistableUriPermission(selectedFolderUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                Log.d(TAG, "Selected folder URI: " + selectedFolderUri);
                DocumentFile pickedDir = DocumentFile.fromTreeUri(requireContext(), selectedFolderUri);

                if (pickedDir != null && pickedDir.canRead()) {
                    tvSelectedFolder.setText("Selected folder: " + pickedDir.getName());
                } else {
                    Toast.makeText(requireContext(), "Cannot access selected folder", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e(TAG, "Error: selectedFolderUri is null");
                Toast.makeText(requireContext(), "Error: Unable to retrieve folder URI", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startFtpServer() {
        if (selectedFolderUri == null) {
            Toast.makeText(requireContext(), "Please select a folder first", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            FtpServerFactory serverFactory = new FtpServerFactory();
            ListenerFactory factory = new ListenerFactory();
            factory.setPort(PORT);

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

            // You'll need to implement AndroidFileSystemFactory or use a compatible file system
            // serverFactory.setFileSystem(new AndroidFileSystemFactory(requireContext(), selectedFolderUri));

            ftpServer = serverFactory.createServer();
            ftpServer.start();

            String ipAddress = getLocalIpAddress();
            if (ipAddress != null) {
                tvServerStatus.setText("FTP Server is running on ftp://" + ipAddress + ":" + PORT);
            } else {
                tvServerStatus.setText("FTP Server is running on port " + PORT + " (IP Address not available)");
            }

            Toast.makeText(requireContext(), "FTP Server started", Toast.LENGTH_SHORT).show();

        } catch (FtpException e) {
            Log.e(TAG, "Failed to start FTP server", e);
            Toast.makeText(requireContext(), "Failed to start FTP server", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopFtpServer() {
        if (ftpServer != null) {
            ftpServer.stop();
            tvServerStatus.setText("FTP Server is stopped");
            Toast.makeText(requireContext(), "FTP Server stopped", Toast.LENGTH_SHORT).show();
        }
    }
}