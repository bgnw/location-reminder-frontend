package com.bgnw.locationreminder.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.bgnw.locationreminder.R
import com.bgnw.locationreminder.data.TaskList
import java.time.format.DateTimeFormatter

class TaskListListAdapter(
    private val context: Activity,
    private val taskLists: List<TaskList>,
    private val onItemClickListener: OnItemClickListener
) : ArrayAdapter<TaskList>(context, R.layout.list_tasklist_item, taskLists) {

    val dtFormatterDateOnly: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM")

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater: LayoutInflater = LayoutInflater.from(context)
        val taskList = taskLists[position]
        val view: View = inflater.inflate(R.layout.list_tasklist_item, null)

        view.setOnClickListener {
            onItemClickListener.onItemClick(taskList)
        }

        val ltiName: TextView = view.findViewById(R.id.lti_name)
        val ltiDescription: TextView = view.findViewById(R.id.lti_description)

        ltiName.text = taskList.title
        ltiDescription.text =
            "${taskList.items?.size ?: 0} items • Created ${
                taskList.created_at.format(
                    dtFormatterDateOnly
                )
            }"

        return view
    }

    interface OnItemClickListener {
        fun onItemClick(position: TaskList)
    }
}