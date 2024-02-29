package com.bgnw.locationreminder

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Parcelize
data class TaskItem(
    var title: String,
    var distance: Int?,
//    var due: LocalDateTime,
    var due: LocalDateTime,
    var completed: Boolean = false
) : Parcelable {
    fun getHumanDuration(): String {
        val dtFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM 'at' HH:mm")
        val dueInFuture: Boolean
        lateinit var durationUntilDue: String

        val units = arrayOf(
            ChronoUnit.YEARS,
            ChronoUnit.MONTHS,
            ChronoUnit.WEEKS,
            ChronoUnit.DAYS,
            ChronoUnit.HOURS,
            ChronoUnit.MINUTES,
        )

        val secToDue = LocalDateTime.now().until(this.due, ChronoUnit.SECONDS)
        if (secToDue > 59) { // if more than 59 sec in future
            dueInFuture = true
        } else if (secToDue < -59) { // if more than 59 sec in past
            dueInFuture = false
        } else {
            return "now"
        }

        for (unit: ChronoUnit in units) {
            val thisUnitDuration =
                if (dueInFuture)
                    LocalDateTime.now().until(this.due, unit)
                else
                    this.due.until(LocalDateTime.now(), unit)
            if (thisUnitDuration > 0) {
                durationUntilDue = thisUnitDuration.toString() + " ${unit.toString().lowercase()}"
                break
            }
        }



        return if (dueInFuture)
            "in $durationUntilDue"
        else
            "$durationUntilDue ago"
    }

}