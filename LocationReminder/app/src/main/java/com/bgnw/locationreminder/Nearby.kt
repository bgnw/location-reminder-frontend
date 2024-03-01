package com.bgnw.locationreminder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.bgnw.locationreminder.databinding.FragmentNearbyBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Nearby : Fragment() {

    private lateinit var binding: FragmentNearbyBinding
    private var dtFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm") // TODO remove if not used here

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_nearby, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentNearbyBinding.inflate(layoutInflater)

        val context = context as MainActivity
        val lv = context.findViewById(R.id.lv_nearby_tasks) as ListView
//        val adapter = TaskItemListAdapter(context, samples)
//        lv.adapter = adapter
    }
}
