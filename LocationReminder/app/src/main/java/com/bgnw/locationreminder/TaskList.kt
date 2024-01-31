package com.bgnw.locationreminder

import android.os.Parcelable
import java.time.LocalDateTime
import kotlinx.parcelize.Parcelize

@Parcelize
data class TaskList(
    var name: String,
    var created: LocalDateTime,
    var items: ArrayList<TaskItem> = ArrayList()
    // TODO sharing with collaborators
) : Parcelable