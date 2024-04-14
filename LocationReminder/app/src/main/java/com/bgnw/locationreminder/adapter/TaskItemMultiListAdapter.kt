package com.bgnw.locationreminder

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.bgnw.locationreminder.data.TaskItem
import com.bgnw.locationreminder.nominatim_api.queryNominatimApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.lang.Thread.sleep


class TaskItemMultiListAdapter(
    private val context: Activity,
    private val taskItems: MutableList<Pair<String, TaskItem>>,
) : ArrayAdapter<Pair<String, TaskItem>>(context, R.layout.list_task_item, taskItems) {

    private val locationTagPattern = Regex("""([^=]+)="([^"]+)"""")

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater: LayoutInflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.list_task_item, null)

        val liTitle: TextView = view.findViewById(R.id.li_title)
        val liSubtitle: TextView = view.findViewById(R.id.li_subtitle)

        val item = taskItems[position]

        liTitle.text = item.second.title


        if (item.second.remind_method == "LOCATION_CATEGORY") {
            if (item.second.applicable_filters!!.isNotEmpty()) {
                val first2Places = item.second.applicable_filters!!.take(2)
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
                val remainingPlacesCount = item.second.applicable_filters!!.size - first2Places.size
                var finalMessage = humanPlaceNames.joinToString(", ")
                if (remainingPlacesCount > 0) {
                    finalMessage += " ($remainingPlacesCount more)..."
                }
                liSubtitle.text = "When near locations: $finalMessage"
            } else {
                liSubtitle.text = ""
            }
        } else if (item.second.remind_method == "PEER_USER") {
            liSubtitle.text = "When near user: ${item.second.user_peer}"
        } else if (item.second.remind_method == "LOCATION_POINT") {
            val nominatimResp = CoroutineScope(Dispatchers.IO).async {
                queryNominatimApi(item.second.lati!!, item.second.longi!!)
            }
            while (!nominatimResp.isCompleted) {
                sleep(500)
            }
            val addressObj = nominatimResp.getCompleted().address
            val primary = addressObj.neighbourhood ?: addressObj.city
            val secondary =
                if (primary == addressObj.neighbourhood && addressObj.city != null) {
                    addressObj.city
                } else {
                    addressObj.county ?: addressObj.state ?: addressObj.country
                }
            val locality = "${if (primary != null) "$primary, " else ""}${secondary}"
            liSubtitle.text = "When near location in $locality"

        } else {
            liSubtitle.text = ""
        }

        liSubtitle.text = liSubtitle.text.toString() + "\n(From list: ${item.first})"

        return view
    }
}