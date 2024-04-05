package com.bgnw.locationreminder

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import com.bgnw.locationreminder.activity.ViewEditTaskItemActivity
import com.bgnw.locationreminder.adapter.TaskListListAdapter
import com.bgnw.locationreminder.data.TaskItem
import com.bgnw.locationreminder.frag.CreateTaskItemFragment
import com.bgnw.locationreminder.frag.ViewEditTaskItemFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import com.bgnw.locationreminder.nominatim_api.queryNominatimApi
import kotlinx.coroutines.async
import java.lang.Thread.sleep


class TaskItemListAdapter(
    private val context: Activity,
    private val taskItems: List<TaskItem>,
    private val listener: OnInfoClickListener
) : ArrayAdapter<TaskItem>(context, R.layout.list_task_item, taskItems) {

    private val dtFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM 'at' HH:mm")
    private val locationTagPattern = Regex("""([^=]+)="([^"]+)"""")

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        Log.d("bgnw_TILA", "running getView")
        val inflater: LayoutInflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.list_task_item, null)


        val liTitle: TextView = view.findViewById(R.id.li_title)
        val liSubtitle: TextView = view.findViewById(R.id.li_subtitle)

        val item = taskItems[position]

        liTitle.text = item.title


        if (item.remind_method == "LOCATION_CATEGORY") {
            if (item.applicable_filters!!.size >= 1) {
                val first2Places = item.applicable_filters!!.take(2)
                val filtervalues = mutableListOf<String>()
                val humanPlaceNames = mutableSetOf<String>()
                first2Places.forEach { entry ->
                    var category = ""
                    var place = ""
                    val regexMatch = entry.get("filters")!!.replace(locationTagPattern) {
                        category = it.groupValues[1]
                        place = it.groupValues[2].replace('_', ' ')
                        "$category of $place"
                    }
                    filtervalues.add(regexMatch)
                    humanPlaceNames.add(place)
                }
                val remainingPlacesCount = item.applicable_filters!!.size - first2Places.size
//                var finalMessage = filtervalues.joinToString(", ")
                var finalMessage = humanPlaceNames.joinToString(", ")
                if (remainingPlacesCount > 0) {
                    finalMessage += " ($remainingPlacesCount more)..."
                }
                liSubtitle.text = "When near locations: ${finalMessage}"
            }
            else {
                liSubtitle.text = taskItems[position].body_text
            }
        }
        else if (item.remind_method == "PEER_USER") {
            liSubtitle.text = "When near user: ${item.user_peer}"
        }
        else if (item.remind_method == "LOCATION_POINT") {
            val nominatimResp = CoroutineScope(Dispatchers.IO).async {
                queryNominatimApi(item.lati!!, item.longi!!)
            }
            while (!nominatimResp.isCompleted) {
                sleep(500)
            }
            var addressObj = nominatimResp.getCompleted().address
            var primary = addressObj.neighbourhood ?: addressObj.city
            var secondary =
                if (primary == addressObj.neighbourhood && addressObj.city != null) {
                    addressObj.city
                } else {
                    addressObj.county ?: addressObj.state ?: addressObj.country
                }
            var locality = "${if (primary != null) "$primary, " else ""}${secondary}"
            liSubtitle.text = "When near location in $locality"

        }
        else {
            liSubtitle.text = taskItems[position].body_text
        }
//        liSubtitle.text = "${task.distance}m away${
//            if (task.due_at != null) ", due " + task.due_at?.format(dtFormatter) else ""
//        }"
//        liSubtitle.text = "${task.distance}m away, due ${task.getHumanDuration()}"


        val infoBtn: Button = view.findViewById(R.id.item_info_btn)


        infoBtn.setOnClickListener {
            listener.onInfoClick(item)
        }

        return view
    }


    interface OnInfoClickListener {
        fun onInfoClick(item: TaskItem?)
    }
}