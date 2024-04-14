package com.bgnw.locationreminder.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.lifecycle.LiveData
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority


data class LocationModel(
    val latitude: Double,
    val longitude: Double
)

// https://proandroiddev.com/android-tutorial-on-location-update-with-livedata-774f8fcc9f15

class LocationLiveData(context: Context) : LiveData<LocationModel>() {

    private var fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    private fun setLocationData(location: Location) {
        value = LocationModel(
            latitude = location.latitude,
            longitude = location.longitude
        )
    }

    companion object {
        val locationRequest: LocationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10000,
        ).build()

        fun changeUpdateInterval(seconds: Int) {
            locationRequest.setInterval((seconds * 1000).toLong())
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations) {
                setLocationData(location)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null
        )
    }

    @SuppressLint("MissingPermission")
    override fun onActive() {
        super.onActive()
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.also {
                    setLocationData(it)
                }
            }
        startLocationUpdates()
    }
}