package com.bgnw.locationreminder.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TaskItem(
    var item_id: Int?,
    var list: Int,
    var owner: String,
    var title: String,
    var body_text: String,
    var remind_method: String?,
    var poi_filters: String?,
    var attachment_img_path: String?,
    var snooze_until: String?,
    var completed: Boolean,
    var due_at: String?,
    var is_sub_task: Boolean,
    var parent_task: Int?,
    var opportunities: MutableList<ItemOpportunity>? = mutableListOf(),
) : Parcelable {
    override fun toString(): String {
        return """
            {
                "item_id": $item_id,
                "list": "$list",
                "owner": "$owner",
                "title": "$title",
                "body_text": ${if (body_text == null) "null" else "\"$body_text\""},
                "remind_method": ${if (remind_method == null) "null" else "\"$remind_method\""},
                "poi_filters": ${if (poi_filters == null) "null" else "\"$poi_filters\""},
                "attachment_img_path": ${if (attachment_img_path == null) "null" else "\"$attachment_img_path\""},
                "snooze_until": ${if (snooze_until == null) "null" else "\"$snooze_until\""},
                "completed": $completed,
                "due_at": ${if (due_at == null) "null" else "\"$due_at\""},
                "is_sub_task": $is_sub_task,
                "parent_task": $parent_task
            }
        """.trimIndent()
    }

    /*
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

        val secToDue = LocalDateTime.now().until(this.due_at, ChronoUnit.SECONDS)
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
                    LocalDateTime.now().until(this.due_at, unit)
                else
                    this.due_at.until(LocalDateTime.now(), unit)
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
     */
}