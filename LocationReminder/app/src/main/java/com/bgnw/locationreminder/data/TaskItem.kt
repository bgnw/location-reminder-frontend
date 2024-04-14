package com.bgnw.locationreminder.data

import android.os.Parcelable
import com.bgnw.locationreminder.activity.CreateTaskItemActivity
import com.bgnw.locationreminder.api.TagValuePair
import com.google.gson.annotations.Expose
import kotlinx.parcelize.Parcelize
import java.util.Dictionary

@Parcelize
data class TaskItem(
    var item_id: Int?,
    var list: Int,
    var owner: String,
    var title: String,
    var body_text: String,
    var remind_method: String?,
    var attachment_img_path: String?,
    var user_peer: String? = null,
    var snooze_until: String?,
    var completed: Boolean,
    var due_at: String?,
    var lati: Double? = null,
    var longi: Double? = null,
    var is_sub_task: Boolean,
    var parent_task: Int?,
    var opportunities: MutableList<ItemOpportunity>? = mutableListOf(),
    var filters: List<Map<String, String>>?,

    // @Expose usage: https://stackoverflow.com/questions/49791539
    @Expose(serialize = false, deserialize = true)
    var applicable_filters: List<Map<String, String>>? = null
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (other is TaskItem) {
            return this.item_id === other.item_id
        }
        else {
            return super.equals(other)
        }
    }

    override fun toString(): String {
        return """
            {
                "item_id": $item_id,
                "list": "$list",
                "owner": "$owner",
                "title": "$title",
                "body_text": ${if (body_text == null) "null" else "\"$body_text\""},
                "remind_method": ${if (remind_method == null) "null" else "\"$remind_method\""},
                "attachment_img_path": ${if (attachment_img_path == null) "null" else "\"$attachment_img_path\""},
                "snooze_until": ${if (snooze_until == null) "null" else "\"$snooze_until\""},
                "completed": $completed,
                "due_at": ${if (due_at == null) "null" else "\"$due_at\""},
                "is_sub_task": $is_sub_task,
                "parent_task": $parent_task,
                "filters": $filters
            }
        """.trimIndent()
    }

    companion object {
        fun convertFiltersToString(filters: Collection<String>): String {
            if (filters.isEmpty()) { return "[]" }

            val filterString = buildString {
                append("[")
                filters?.forEach { filter -> append("{\"filters\": \"$filter\"},") }
            }

            return filterString.dropLast(1) + "]"
        }

        fun convertFiltersToMap(filters: Collection<TagValuePair>?): List<Map<String, String>> {
            if (filters.isNullOrEmpty()) { return listOf() }

            val list = mutableListOf<Map<String, String>>()
            filters.forEach { filter ->
                val map = mutableMapOf<String, String>()

                if (filter.value == null) {
                    map.put("filters", filter.tag)
                }
                else {
                    map.put("filters", "${filter.tag}=\"${filter.value}\"")
                }

                list.add(map)
            }

            return list.toList()
        }

        fun findItemsFromFilters(items: List<TaskItem>, filters: List<Map<String, String>>): List<TaskItem> {
            var matches = mutableListOf<TaskItem>()

            for (item in items) {
                for (filter in filters) {
                    if (item.filters?.contains(filter) == true) {
                        matches.add(item)
                        continue
                    }
                }
            }

            return matches.toList()
        }
    }
}