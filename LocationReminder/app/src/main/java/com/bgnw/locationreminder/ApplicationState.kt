package com.bgnw.locationreminder

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bgnw.locationreminder.activity.CreateTaskItemActivity
import com.bgnw.locationreminder.data.Account
import com.bgnw.locationreminder.data.ItemOpportunity
import com.bgnw.locationreminder.data.TaskList
import com.bgnw.locationreminder.location.LocationModel
import org.osmdroid.util.GeoPoint
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
    val listIdToOpen: MutableLiveData<Int?> by lazy {
        MutableLiveData<Int?>()
    }
    val peerLocations: MutableLiveData<MutableMap<String, GeoPoint>> by lazy {
        MutableLiveData<MutableMap<String, GeoPoint>>()
    }
}
