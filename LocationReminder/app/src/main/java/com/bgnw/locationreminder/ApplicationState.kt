package com.bgnw.locationreminder

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bgnw.locationreminder.api.TaskList_ApiStruct

class ApplicationState : ViewModel() {
    val loggedInUsername: MutableLiveData<String?> by lazy {
        MutableLiveData<String?>()
    }
    val loggedInDisplayName: MutableLiveData<String?> by lazy {
        MutableLiveData<String?>()
    }
    val lists: MutableLiveData<List<TaskList_ApiStruct>?> by lazy {
        MutableLiveData<List<TaskList_ApiStruct>?>()
    }
}
