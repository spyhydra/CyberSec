package com.example.cybersec.ui.nmap;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.cybersec.R;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class NmapFragment extends Fragment {

    private EditText targetIpEditText, portRangeEditText;
    private Button scanButton;
    private TextView resultsTextView;

    private static final Map<Integer, String> PORT_TO_SERVICE_MAP = new HashMap<>();

    static {
        PORT_TO_SERVICE_MAP.put(80, "HTTP");
        PORT_TO_SERVICE_MAP.put(443, "HTTPS");
        PORT_TO_SERVICE_MAP.put(22, "SSH");
        PORT_TO_SERVICE_MAP.put(21, "FTP");
        PORT_TO_SERVICE_MAP.put(23, "Telnet");
        PORT_TO_SERVICE_MAP.put(25, "SMTP");
        PORT_TO_SERVICE_MAP.put(53, "DNS");
        PORT_TO_SERVICE_MAP.put(3306, "MySQL");
        PORT_TO_SERVICE_MAP.put(8080, "HTTP Proxy");
        PORT_TO_SERVICE_MAP.put(3389, "RDP (Remote Desktop)");
        // Add more ports and services as needed
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_scanner, container, false);

        targetIpEditText = root.findViewById(R.id.targetIp);
        portRangeEditText = root.findViewById(R.id.portRange);
        scanButton = root.findViewById(R.id.scanButton);
        resultsTextView = root.findViewById(R.id.results);

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ip = targetIpEditText.getText().toString().trim();
                String portRange = portRangeEditText.getText().toString().trim();

                if (ip.isEmpty() || portRange.isEmpty()) {
                    Toast.makeText(getActivity(), "Please enter both IP and port range", Toast.LENGTH_SHORT).show();
                } else {
                    String[] range = portRange.split("-");
                    if (range.length == 2) {
                        int startPort = Integer.parseInt(range[0]);
                        int endPort = Integer.parseInt(range[1]);
                        new PortScannerTask().execute(ip, startPort, endPort);
                    } else {
                        Toast.makeText(getActivity(), "Invalid port range format", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        return root;
    }

    // AsyncTask for port scanning
    private class PortScannerTask extends AsyncTask<Object, Void, String> {

        @Override
        protected String doInBackground(Object... params) {
            String ip = (String) params[0];
            int startPort = (int) params[1];
            int endPort = (int) params[2];

            StringBuilder openPorts = new StringBuilder();
            for (int port = startPort; port <= endPort; port++) {
                if (isPortOpen(ip, port)) {
                    String service = getServiceName(port);
                    openPorts.append("Port ").append(port).append(" is OPEN (").append(service).append(")\n");
                }
            }

            return openPorts.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            // Show scan results in the TextView
            if (result.isEmpty()) {
                resultsTextView.setText("No open ports found.");
            } else {
                resultsTextView.setText(result);
            }
        }

        private boolean isPortOpen(String ip, int port) {
            try {
                Socket socket = new Socket();
                socket.connect(new java.net.InetSocketAddress(ip, port), 1000);  // 1 second timeout
                socket.close();
                return true;
            } catch (IOException e) {
                return false;
            }
        }

        // Lookup service by port number
        private String getServiceName(int port) {
            return PORT_TO_SERVICE_MAP.getOrDefault(port, "Unknown Service");
        }
    }
}
