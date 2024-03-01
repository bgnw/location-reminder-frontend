package com.bgnw.locationreminder

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bgnw.locationreminder.api.TaskList

class ApplicationState : ViewModel() {
    val loggedInUsername: MutableLiveData<String?> by lazy {
        MutableLiveData<String?>()
    }
    val loggedInDisplayName: MutableLiveData<String?> by lazy {
        MutableLiveData<String?>()
    }
    val lists: MutableLiveData<List<TaskList>?> by lazy {
        MutableLiveData<List<TaskList>?>()
    }
}
