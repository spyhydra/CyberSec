package com.example.cybersec.ui.whois;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class WhoisViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public WhoisViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Whois fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}