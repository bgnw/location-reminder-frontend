package com.bgnw.locationreminder.api

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter

@Parcelize
data class TaskList_ApiStruct(
    val list_id: Int?,
    val title: String,
    val icon_name: String?,
    val created_at: String,
    val owner: String,
    val sort_by: String?,
    val visibility: Int,
    var items: List<TaskItem_ApiStruct>? = null
): Parcelable {
    override fun toString(): String {

        var dateFormatZulu: DateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
        val dateFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        return """
            {
                "list_id": $list_id,
                "title": "$title",
                "icon_name": "$icon_name",
                "created_at": "${dateFormatZulu.parse(created_at)}",
                "owner": "$owner",
                "sort_by": "$sort_by",
                "visibility": $visibility
            }
        """.trimIndent()
    }
}