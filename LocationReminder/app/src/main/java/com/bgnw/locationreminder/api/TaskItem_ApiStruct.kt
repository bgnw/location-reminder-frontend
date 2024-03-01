package com.bgnw.locationreminder.api

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TaskItem_ApiStruct(
    var item_id: Int?,
    var list: Int,
    var body_text: String,
    var remind_method: String?,
    var attachment_img_path: String?,
    var snooze_until: String?,
    var completed: Boolean,
    var due_at: String?,
    var is_sub_task: Boolean,
    var parent_task: Int?,
): Parcelable {
    override fun toString(): String {
        return """
            {
                "item_id": "$item_id",
                "list": "$list",
                "body_text": "$body_text",
                "remind_method": "$remind_method",
                "attachment_img_path": "$attachment_img_path",
                "snooze_until": "$snooze_until",
                "completed": "$completed",
                "due_at": "$due_at",
                "is_sub_task": "$is_sub_task",
                "parent_task": "$parent_task",
            }
        """.trimIndent()
    }
}