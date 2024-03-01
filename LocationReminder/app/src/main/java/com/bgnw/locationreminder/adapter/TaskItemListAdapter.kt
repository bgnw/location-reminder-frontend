package com.bgnw.locationreminder.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.bgnw.locationreminder.R
import com.bgnw.locationreminder.data.TaskItem
import java.time.format.DateTimeFormatter

class TaskItemListAdapter(
    private val context: Activity,
    private val taskItems: List<TaskItem>
) : ArrayAdapter<TaskItem>(context, R.layout.list_task_item, taskItems) {

    val dtFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM 'at' HH:mm")

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater: LayoutInflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.list_task_item, null)

        val task = taskItems[position]

        val liTitle: TextView = view.findViewById(R.id.li_title)
        val liSubtitle: TextView = view.findViewById(R.id.li_subtitle)

        liTitle.text = taskItems[position].body_text
//        liSubtitle.text = "${task.distance}m away${
//            if (task.due_at != null) ", due " + task.due_at?.format(dtFormatter) else ""
//        }"
//        liSubtitle.text = "${task.distance}m away, due ${task.getHumanDuration()}"

        return view
    }
}