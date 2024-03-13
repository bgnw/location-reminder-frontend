package com.bgnw.locationreminder

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bgnw.locationreminder.data.TaskList
import java.time.Instant

class ApplicationState : ViewModel() {
    val loggedInUsername: MutableLiveData<String?> by lazy {
        MutableLiveData<String?>()
    }
    val loggedInDisplayName: MutableLiveData<String?> by lazy {
        MutableLiveData<String?>()
    }
    val lists: MutableLiveData<MutableList<TaskList>?> by lazy {
        MutableLiveData<MutableList<TaskList>?>()
    }
//    val changeNeeded: MutableLiveData<Boolean> by lazy {
//        MutableLiveData<Boolean>()
//    }
//    val changesMade: MutableLiveData<Boolean> by lazy {
//        MutableLiveData<Boolean>()
//    }
//    val lastUpdate: MutableLiveData<Instant> by lazy {
//        MutableLiveData<Instant>()
//    }
}
