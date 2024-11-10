package com.example.cybersec.ui.whois;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.cybersec.databinding.FragmentWhoisBinding;

public class WhoisFragment extends Fragment {

    FragmentWhoisBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        WhoisViewModel whoisViewModel =
                new ViewModelProvider(this).get(WhoisViewModel.class);

        binding = FragmentWhoisBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textWhois;
        whoisViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}