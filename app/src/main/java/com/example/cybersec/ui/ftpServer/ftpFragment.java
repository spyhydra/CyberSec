package com.example.cybersec.ui.ftpServer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cybersec.ftpUse;

public class ftpFragment extends Fragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Use a proper context-safe initialization in Fragment lifecycle methods
        startFtpUseActivity();
    }

    private void startFtpUseActivity() {
        Context context = getContext();
        if (context != null) {
            Intent intent = new Intent(context, ftpUse.class);
            startActivity(intent);
        } else {
            // Log or handle the situation where the fragment's context is null
        }
    }
}
