package com.xstudio.snapclean.fragments;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SelecionadosViewModel extends ViewModel {
    private MutableLiveData<Boolean> isLayoutVisible = new MutableLiveData<>();

    public LiveData<Boolean> getIsLayoutVisible(){
        return isLayoutVisible;
    }

    public void toggleLayoutVisibility(){
        boolean isVisible = isLayoutVisible.getValue() != null ? isLayoutVisible.getValue() : false;
        isLayoutVisible.setValue(!isVisible);
    }
}
