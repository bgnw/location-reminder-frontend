package com.bgnw.locationreminder.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Log(
    val lati: Double,
    val longi: Double,
    val notes: String,
) : Parcelable