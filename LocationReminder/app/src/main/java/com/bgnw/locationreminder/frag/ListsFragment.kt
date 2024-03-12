package com.bgnw.locationreminder.frag

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import com.bgnw.locationreminder.ApplicationState
import com.bgnw.locationreminder.MainActivity
import com.bgnw.locationreminder.R
import com.bgnw.locationreminder.activity.ViewTaskListActivity
import com.bgnw.locationreminder.adapter.TaskListListAdapter
import com.bgnw.locationreminder.api.Requests
import com.bgnw.locationreminder.data.TaskList
import com.bgnw.locationreminder.databinding.FragmentListsBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter


class ListsFragment : Fragment() {

    private val viewModel: ApplicationState by activityViewModels()

    private lateinit var binding: FragmentListsBinding
    private lateinit var samples: List<TaskList> // TEMP
    private val dtFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm") // TODO remove if not used here

    private val itemClickListener = object : TaskListListAdapter.OnItemClickListener {
        override fun onItemClick(position: TaskList) {
            Log.d("bgnw_Data: ", position.toString())

            val intent = Intent(context, ViewTaskListActivity::class.java)
            Log.d("bgnw_PASSING LIST:", position.toString())
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
        val adapter =
            viewModel.lists.value?.let { TaskListListAdapter(context, it, itemClickListener) }
        lv.adapter = adapter


        viewModel.lists.observe(viewLifecycleOwner, {
            adapter?.notifyDataSetInvalidated()
        })

        val addListButton: FloatingActionButton? = context.findViewById(R.id.fab_add_list)
        addListButton?.setOnClickListener { _ -> // https://www.digitalocean.com/community/tutorials/android-alert-dialog-using-kotlin
            val editText = EditText(context)
            editText.hint = "Provide a list name"

            val dialog = AlertDialog.Builder(context)
                .setTitle("Create new list")
                .setView(editText)

                .setPositiveButton("OK") { _, _ ->
                    val title = editText.text.toString()
                    val username = viewModel.loggedInUsername.value
                    if (title.isNotEmpty() && username != null) {

                        val reqIsDone: MutableLiveData<Boolean> = MutableLiveData(false)
                        val newList: MutableLiveData<TaskList> = MutableLiveData()

                        reqIsDone.observe(viewLifecycleOwner) {
                            Log.d("bgnw", "donEEEE!!!!!!!!!!")
                            viewModel.changeNeeded.value = true
                        }

                        newList.observe(viewLifecycleOwner) { newList ->
                            if (newList != null) {
                                Log.d("bgnw", "newlist is not null")

                                adapter?.add(newList)
                            } else {
                                Log.d("bgnw", "newlist IS null")

                            }
                        }

                        val reqResult = CoroutineScope(Dispatchers.IO).async {
                            Requests.createList(
                                title, null, username,
                                "created_at", 0, null
                            )
                        }

                        CoroutineScope(Dispatchers.IO).launch {
                            val listResult = reqResult.await()

                            // Process the result
                            Log.d("bgnw", "Async operation result: $listResult")

                            newList.postValue(listResult)
                            reqIsDone.postValue(true)
                        }
                    }
                }
                .setNegativeButton("Cancel") { _, _ ->
                    Toast.makeText(context, "CANCEL", Toast.LENGTH_SHORT).show()
                }
                .create()

            dialog.show()
        }


        viewModel.changeNeeded.observe(viewLifecycleOwner) {
            Log.d("bgnw", "change needed run")
        }



        context.findViewById<Button>(R.id.changefalse).setOnClickListener {
            viewModel.changeNeeded.value = false
        }
        context.findViewById<Button>(R.id.changetrue).setOnClickListener {
            viewModel.changeNeeded.value = true
        }


    }

}