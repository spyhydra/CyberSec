package com.example.cybersec.ui.nmap;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class NmapViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public NmapViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Port Scanner fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}