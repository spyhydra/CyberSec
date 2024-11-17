package com.example.cybersec.ui.whois;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.cybersec.databinding.FragmentWhoisBinding;

public class WhoisFragment extends Fragment {

    private FragmentWhoisBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWhoisBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        EditText editTextDomain = binding.editTextWhois;
        Button btnLookup = binding.btnLookup;
        TextView textViewResults = binding.textViewResults;

        btnLookup.setOnClickListener(v -> {
            String domain = editTextDomain.getText().toString().trim();
            if (domain.isEmpty()) {
                textViewResults.setText("Please enter a valid domain.");
                return;
            }

            textViewResults.setText("Fetching WHOIS information...");
            performWhoisLookup(domain, textViewResults);
        });

        return root;
    }

    private void performWhoisLookup(String domain, TextView textViewResults) {
        WhoisLookup whoisLookup = new WhoisLookup();
        whoisLookup.performWhoisLookup(domain, new WhoisLookup.WhoisCallback() {  // Changed to WhoisCallback
            @Override
            public void onSuccess(String result) {
                if (isAdded()) {  // Check if Fragment is still attached
                    requireActivity().runOnUiThread(() -> {
                        if (binding != null) {  // Check if binding is still valid
                            textViewResults.setText(result);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (isAdded()) {  // Check if Fragment is still attached
                    requireActivity().runOnUiThread(() -> {
                        if (binding != null) {  // Check if binding is still valid
                            textViewResults.setText("Error: " + e.getMessage());
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}