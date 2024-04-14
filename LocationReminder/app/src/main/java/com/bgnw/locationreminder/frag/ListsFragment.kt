package com.bgnw.locationreminder.frag

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import com.bgnw.locationreminder.ApplicationState
import com.bgnw.locationreminder.MainActivity
import com.bgnw.locationreminder.R
import com.bgnw.locationreminder.TaskItemMultiListAdapter
import com.bgnw.locationreminder.adapter.TaskListListAdapter
import com.bgnw.locationreminder.api.Requests
import com.bgnw.locationreminder.data.TaskItem
import com.bgnw.locationreminder.data.TaskList
import com.bgnw.locationreminder.databinding.FragmentListsBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter


class ListsFragment : Fragment() {

    private val viewModel: ApplicationState by activityViewModels()

    private lateinit var binding: FragmentListsBinding

    private var adapter: TaskListListAdapter? = null
    private var adapterDigest: TaskItemMultiListAdapter? = null
    private var request: ActivityResultLauncher<Intent>? = null

    private val dtHuman: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM 'at' HH:mm")
    private val dtZulu: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")

    private var digestShown = false
    private var digestNoItemsToast: Toast? = null

    private val itemClickListener = object : TaskListListAdapter.OnItemClickListener {
        override fun onItemClick(position: TaskList) {

            val viewTaskListFragment = ViewTaskListFragment()
            val bundle = Bundle()
            bundle.putInt("LIST_ID", position.list_id!!)
            viewTaskListFragment.arguments = bundle
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, viewTaskListFragment)
                .addToBackStack(null)
                .commit()
            return
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        digestNoItemsToast = Toast.makeText(
            context,
            "There are no items due today, or overdue.",
            Toast.LENGTH_SHORT
        )

        request = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == AppCompatActivity.RESULT_OK) {
                val data = it.data
                val mutatedList: TaskList? = data?.extras?.getParcelable<TaskList>("MUTATED_LIST")

                if (mutatedList != null) {

                    viewModel.lists.value?.removeIf { list -> list.list_id == mutatedList.list_id }
                    viewModel.lists.value?.add(mutatedList)
                    viewModel.lists.value?.sortBy { list -> list.title }


                    adapter?.notifyDataSetChanged()
                    adapter?.notifyDataSetInvalidated()
                }

                viewModel.lists.postValue(viewModel.lists.value)

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        request = null
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_lists, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentListsBinding.inflate(layoutInflater)

        val context = context as MainActivity
        val lv = context.findViewById(R.id.lv_tasklist_list) as ListView
        val lvDigest = context.findViewById(R.id.lv_daily_digest) as ListView
        adapter = viewModel.lists.value?.let { TaskListListAdapter(context, it, itemClickListener) }
        lv.adapter = adapter

        if (viewModel.listIdToOpen.value != null && viewModel.lists.value != null) {
            val id = viewModel.listIdToOpen.value!!
            val list = viewModel.lists.value!!.find { list -> list.list_id == id }
            list?.let { itemClickListener.onItemClick(it) }
            viewModel.listIdToOpen.postValue(null)
        }

        viewModel.lists.observe(viewLifecycleOwner) {
            adapter?.notifyDataSetInvalidated()
        }

        val addListButton: FloatingActionButton? = context.findViewById(R.id.fab_add_list)
        addListButton?.setOnClickListener { _ ->

            val editText = EditText(context)
            editText.isSingleLine = true
            editText.hint = "Provide a list name"

            // https://www.digitalocean.com/community/tutorials/android-alert-dialog-using-kotlin
            val dialog = AlertDialog.Builder(context)
                .setTitle("Create new list")
                .setView(editText)

                .setPositiveButton("OK") { _, _ ->
                    val title = editText.text.toString()
                    val username = viewModel.loggedInUsername.value
                    if (title.isNotEmpty() && username != null) {
                        editText.text.clear()

                        val reqIsDone: MutableLiveData<Boolean> = MutableLiveData(false)
                        val newList: MutableLiveData<TaskList> = MutableLiveData()

                        reqIsDone.observe(viewLifecycleOwner) {
                            (requireActivity() as MainActivity).updateTLs(username)
                        }

                        newList.observe(viewLifecycleOwner) { newList ->
                            if (newList != null) {
                                if (viewModel.lists.value != null) {
                                    viewModel.lists.value!!.add(newList)
                                } else {
                                    viewModel.lists.value = mutableListOf(newList)
                                }
                                viewModel.lists.value?.sortBy { list -> list.title }
                                adapter?.notifyDataSetChanged()
                                adapter?.notifyDataSetInvalidated()
                            }
                        }

                        val reqResult = CoroutineScope(Dispatchers.IO).async {
                            Requests.createList(
                                title, null, username,
                                "created_at", 0
                            )
                        }

                        CoroutineScope(Dispatchers.IO).launch {
                            val listResult = reqResult.await()
                            newList.postValue(listResult)
                            reqIsDone.postValue(true)
                        }
                    }
                }
                .setNegativeButton("Cancel") { _, _ ->
                    editText.text.clear()
                }
                .create()

            dialog.show()
        }


        val dailyDigestButton: FloatingActionButton? = context.findViewById(R.id.fab_daily_digest)

        dailyDigestButton?.setOnClickListener {
            if (digestShown) {
                lv.visibility = View.VISIBLE
                lvDigest.visibility = View.GONE
                addListButton?.visibility = View.VISIBLE
                dailyDigestButton.setImageResource(R.drawable.baseline_today_24)
                activity?.title = "Lists"
                digestShown = false
            } else {
                val dueItems: MutableList<Pair<String, TaskItem>> = mutableListOf()
                val lists = viewModel.lists.value
                if (lists.isNullOrEmpty()) {
                    digestNoItemsToast?.show(); return@setOnClickListener; }
                for (list in lists) {
                    if (list.items == null) {
                        continue
                    }
                    for (item in list.items!!) {
                        if (
                            (!item.due_at.isNullOrBlank())
                            &&
                            (!item.completed)
                            &&
                            (LocalDateTime.parse(item.due_at, dtZulu)
                                .isBefore(LocalDateTime.now().with(LocalTime.MAX)))
                        ) {
                            dueItems.add(Pair(list.title, item))
                        }
                    }
                }

                if (dueItems.isEmpty()) {
                    digestNoItemsToast?.show(); return@setOnClickListener; }

                adapterDigest = TaskItemMultiListAdapter(context, dueItems)
                lvDigest.adapter = adapterDigest
                adapterDigest?.notifyDataSetChanged()
                lv.visibility = View.GONE
                lvDigest.visibility = View.VISIBLE
                addListButton?.visibility = View.GONE
                dailyDigestButton.setImageResource(R.drawable.baseline_close_24)
                activity?.title = "Daily Digest"
                digestShown = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.lists.removeObservers(viewLifecycleOwner)
    }

}