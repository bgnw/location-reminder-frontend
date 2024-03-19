package com.bgnw.locationreminder

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bgnw.locationreminder.activity.CreateTaskItemActivity
import com.bgnw.locationreminder.data.ItemOpportunity
import com.bgnw.locationreminder.data.TaskList
import com.bgnw.locationreminder.location.LocationModel
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
    val filters: MutableLiveData<List<String>?> by lazy {
        MutableLiveData<List<String>?>()
    }
    val userLocation: MutableLiveData<Pair<LocationModel, Float>?> by lazy {
        MutableLiveData<Pair<LocationModel, Float>?>()
    }
    val reminders: MutableLiveData<MutableList<ItemOpportunity>> by lazy {
        MutableLiveData<MutableList<ItemOpportunity>>()
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
