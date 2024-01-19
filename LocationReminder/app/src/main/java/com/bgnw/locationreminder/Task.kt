package com.bgnw.locationreminder

import java.time.LocalDateTime

data class Task(
    var title: String,
    var distance: Int?,
    var due: LocalDateTime,
    var completed: Boolean = false
)