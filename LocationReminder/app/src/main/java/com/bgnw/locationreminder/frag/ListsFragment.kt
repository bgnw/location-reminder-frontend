package com.bgnw.locationreminder.frag

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bgnw.locationreminder.ApplicationState
import com.bgnw.locationreminder.MainActivity
import com.bgnw.locationreminder.R
import com.bgnw.locationreminder.adapter.TaskListListAdapter
import com.bgnw.locationreminder.activity.ViewTaskListActivity
import com.bgnw.locationreminder.data.TaskList
import com.bgnw.locationreminder.databinding.FragmentListsBinding
import java.time.format.DateTimeFormatter

class ListsFragment : Fragment() {

    private val viewModel: ApplicationState by activityViewModels()

    private lateinit var binding: FragmentListsBinding
    private lateinit var samples: List<TaskList> // TEMP
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_lists, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        makeSamples()

        binding = FragmentListsBinding.inflate(layoutInflater)

        val context = context as MainActivity
        val lv = context.findViewById(R.id.lv_tasklist_list) as ListView
//        val adapter = TaskListListAdapter(context, samples, itemClickListener)
        val adapter =
            viewModel.lists.value?.let { TaskListListAdapter(context, it, itemClickListener) }
        lv.adapter = adapter
    }

}