package com.bgnw.locationreminder.frag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bgnw.locationreminder.ApplicationState
import com.bgnw.locationreminder.MainActivity
import com.bgnw.locationreminder.R
import com.bgnw.locationreminder.adapter.ItemOpportunityListAdapter

class NearbyFragment : Fragment() {

    private val viewModel: ApplicationState by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_nearby, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = context as MainActivity
        var list = viewModel.reminders.value
        val lv = context.findViewById(R.id.lv_nearby_tasks) as ListView
        val adapter = ItemOpportunityListAdapter(context, list!!)
        lv.adapter = adapter

        viewModel.reminders.observe(viewLifecycleOwner) {
            list = viewModel.reminders.value
            adapter.notifyDataSetInvalidated()
        }
    }
}
