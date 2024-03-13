package com.bgnw.locationreminder.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter

@Parcelize
data class TaskList(
    val list_id: Int?,
    val title: String,
    val icon_name: String?,
    val created_at: String,
    val owner: String,
    val sort_by: String?,
    val visibility: Int,
    var items: MutableList<TaskItem>? = null
) : Parcelable {
    constructor(list: TaskList) : this(
        list_id = list.list_id,
        title = list.title,
        icon_name = list.icon_name,
        created_at = list.created_at,
        owner = list.owner,
        sort_by = list.sort_by,
        visibility = list.visibility,
        items = list.items,
    )

    override fun toString(): String {

        var dateFormatZulu: DateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
        val dateFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")

//        "created_at": "${dateFormatZulu.parse(created_at)}",

        return """
            {
                "list_id": $list_id,
                "title": "$title",
                "icon_name": "$icon_name",
                "created_at": "$created_at",
                "owner": "$owner",
                "sort_by": "$sort_by",
                "visibility": $visibility
            }
        """.trimIndent()
    }
}