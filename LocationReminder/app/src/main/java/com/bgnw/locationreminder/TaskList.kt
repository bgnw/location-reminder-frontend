package com.bgnw.locationreminder

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Calendar

@Parcelize
data class TaskList(
    var title: String,
    var created: Calendar,
    var items: ArrayList<TaskItem> = ArrayList()
    // TODO sharing with collaborators
) : Parcelable