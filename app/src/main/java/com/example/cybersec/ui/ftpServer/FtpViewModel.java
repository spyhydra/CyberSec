package com.example.cybersec.ui.ftpServer;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;



public class FtpViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public FtpViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is ftp");
    }

    public LiveData<String> getText() {
        return mText;
    }
}