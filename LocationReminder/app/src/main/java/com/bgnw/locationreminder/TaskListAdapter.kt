package com.bgnw.locationreminder

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import java.time.format.DateTimeFormatter

class TaskListAdapter(
    private val context : Activity,
    private val tasks : ArrayList<Task>
) : ArrayAdapter<Task>(context, R.layout.list_task_item, tasks) {

    val dtFormatter : DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM 'at' HH:mm")

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater : LayoutInflater = LayoutInflater.from(context)
        val view : View = inflater.inflate(R.layout.list_task_item, null)

        val task = tasks[position]

        val liTitle : TextView = view.findViewById(R.id.li_title)
        val liSubtitle : TextView = view.findViewById(R.id.li_subtitle)

        liTitle.text = tasks[position].title
        liSubtitle.text = "${task.distance}m away, due ${task.due.format(dtFormatter)}"

        println("Adding a list item.")

        return view
    }
}