package com.bgnw.locationreminder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.bgnw.locationreminder.databinding.FragmentNearbyBinding
import java.time.LocalDateTime
import java.util.Calendar

class Nearby : Fragment() {

    private lateinit var binding: FragmentNearbyBinding
    private lateinit var samples: ArrayList<TaskItem>
//    private var dtFormatter: DateTimeFormatter =
//        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm") // TODO remove if not used here

    private fun makeSamples() {

        val listSamples = ArrayList<TaskList>()

        val list1 = TaskList("Italy 2024", Calendar.getInstance())
        list1.items.add(
            TaskItem(
                "Renew passport",
                200,
                Calendar.getInstance()
            )
        )
        list1.items.add(
            TaskItem(
                "Buy toiletries",
                18,
                Calendar.getInstance()
            )
        )
        listSamples.add(list1)

        val list2 = TaskList("Personal to-do", Calendar.getInstance())
        list2.items.add(
            TaskItem(
                "Buy milk",
                5,
                Calendar.getInstance()
            )
        )
        list2.items.add(
            TaskItem(
                "Collect prescription",
                21,
                Calendar.getInstance()
            )
        )
        listSamples.add(list2)


//         ---------------------

        samples = ArrayList()

        for (list: TaskList in listSamples) {
            for (task: TaskItem in list.items) {
                samples.add(task)
            }
        }

        samples.sortBy { item -> item.distance }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_nearby, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        makeSamples()

        binding = FragmentNearbyBinding.inflate(layoutInflater)

        val context = context as MainActivity
        val lv = context.findViewById(R.id.lv_nearby_tasks) as ListView
        val adapter = TaskItemListAdapter(context, samples)
        lv.adapter = adapter
    }
}
