package com.bgnw.locationreminder

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime

@Parcelize
data class TaskList(
    var title: String,
    var created: LocalDateTime,
    var items: ArrayList<TaskItem> = ArrayList()
    // TODO sharing with collaborators
) : Parcelable