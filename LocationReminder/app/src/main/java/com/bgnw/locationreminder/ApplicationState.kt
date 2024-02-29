package com.bgnw.locationreminder

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ApplicationState : ViewModel() {
    val loggedInUsername: MutableLiveData<String?> by lazy {
        MutableLiveData<String?>()
    }
    val loggedInDisplayName: MutableLiveData<String?> by lazy {
        MutableLiveData<String?>()
    }
}
