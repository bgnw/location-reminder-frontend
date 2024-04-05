package com.bgnw.locationreminder.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.osmdroid.util.GeoPoint
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter

@Parcelize
data class Log(
    val lati: Double,
    val longi: Double,
    val notes: String,
) : Parcelable