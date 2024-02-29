package com.bgnw.locationreminder

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.bgnw.locationreminder.databinding.FragmentListsBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Lists : Fragment() {

    private lateinit var binding: FragmentListsBinding
    private lateinit var samples: ArrayList<TaskList> // TEMP
    private val dtFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm") // TODO remove if not used here

    private val itemClickListener = object : TaskListListAdapter.OnItemClickListener {
        override fun onItemClick(position: TaskList) {
            Log.d("Data: ", position.toString())

            val intent = Intent(context, ViewTaskListActivity::class.java)
            Log.d("PASSING LIST:", position.toString())
            intent.putExtra("selected_list", position) // TODO pass whole list obj
            startActivity(intent)
        }
    }

    private fun makeSamples() {

        samples = ArrayList()

        val list1 = TaskList("Italy 2024", LocalDateTime.now())
        list1.items.add(
            TaskItem(
                "Renew passport",
                200,
                LocalDateTime.parse("2024-04-01 11:00", dtFormatter)
            )
        )
        list1.items.add(
            TaskItem(
                "Buy toiletries",
                18,
                LocalDateTime.parse("2024-04-29 18:00", dtFormatter)
            )
        )
        samples.add(list1)

        val list2 = TaskList("Personal to-do", LocalDateTime.now())
        list2.items.add(
            TaskItem(
                "Buy milk",
                5,
                LocalDateTime.parse("2024-01-20 11:00", dtFormatter)
            )
        )
        list2.items.add(
            TaskItem(
                "Collect prescription",
                21,
                LocalDateTime.parse("2024-01-31 12:00", dtFormatter)
            )
        )
        samples.add(list2)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_lists, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        makeSamples()

        binding = FragmentListsBinding.inflate(layoutInflater)

        val context = context as MainActivity
        val lv = context.findViewById(R.id.lv_tasklist_list) as ListView
        val adapter = TaskListListAdapter(context, samples, itemClickListener)
        lv.adapter = adapter
    }

}