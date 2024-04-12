package com.bgnw.locationreminder.frag

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bgnw.locationreminder.ApplicationState
import com.bgnw.locationreminder.MainActivity
import com.bgnw.locationreminder.R
import com.bgnw.locationreminder.TaskItemListAdapter
import com.bgnw.locationreminder.activity.CreateTaskItemActivity
import com.bgnw.locationreminder.data.TaskItem
import com.bgnw.locationreminder.data.TaskList
import com.bgnw.locationreminder.databinding.FragmentNearbyBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ViewTaskListFragment : Fragment() {

    private val viewModel: ApplicationState by activityViewModels()

    private lateinit var username: String

    private var listObj: TaskList? = null
    private var listId: Int? = null
    
    private var adapter: TaskItemListAdapter? = null
    private var lv: ListView? = null

    private val itemInfoClickListener = object : TaskItemListAdapter.OnInfoClickListener{
        override fun onInfoClick(item: TaskItem?) {
            val selectedItem = item ?: return
//            val intent = Intent(context, ViewEditTaskItemActivity::class.java)
//            intent.putExtra("item_to_view", selectedItem)
//            requireContext().startActivity(intent)

            val viewEditTaskItemFragment = ViewEditTaskItemFragment()
            val bundle = Bundle()
            listId?.let { bundle.putInt("LIST_ID", it) }
            bundle.putParcelable("ITEM", selectedItem)
            viewEditTaskItemFragment.arguments = bundle
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, viewEditTaskItemFragment)
                .addToBackStack(null)
                .commit()

        }
    }

//    private val request = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
//        if (it.resultCode == AppCompatActivity.RESULT_OK) {
//            val data = it.data
//            // TODO !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//            Log.d("bgnw-data", data.toString())
//            val item: TaskItem? = data?.extras?.getParcelable<TaskItem>("NEW_ITEM")
//
//            if (item != null ) {
//                if (listObj?.items == null) {
//                    listObj?.items = mutableListOf(item)
//                }
//                else {
//                    listObj!!.items?.add(item)
//                }
//
//                adapter = listObj!!.items?.let { items ->
//                    TaskItemListAdapter(
//                        this,
//                        items
//                    )
//                }
//
//                lv!!.adapter = adapter
//
//
//
//                adapter?.notifyDataSetChanged()
//                adapter?.notifyDataSetInvalidated()
//            }
//        }
//    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_view_task_list, container, false)

        val context = context as MainActivity
        listId = arguments?.getInt("LIST_ID", -1)
        listObj = viewModel.lists.value?.find { list -> list.list_id == listId }
        username = viewModel.loggedInUsername.value!!

        if (listId == null || listObj == null || username == null) {
            throw Exception("None of (listId, listObj, username) can be null.")
        }

        val listTitleView = rootView.findViewById<TextView>(R.id.lv_list_name)
        listTitleView?.text = "List: ${listObj!!.title}"

//        viewModel.lists.value
//
//        listObj = if (Build.VERSION.SDK_INT >= 33) {
//            intent.getParcelableExtra("selected_list", TaskList::class.java)
//        } else {
//            intent.getParcelableExtra<TaskList>("selected_list")
//        }
//
//        if (listObj == null) {
//            Log.d("bgnw-data", "ORIG LIST IS NULL")
//        }
//
//        title = listObj?.title
//
//        val unpackedUsername = intent.getStringExtra("username")
//        if (unpackedUsername.isNullOrBlank()) {
//            throw Exception("username cannot be blank here")
//        }
//        username = unpackedUsername


        lv = rootView.findViewById<ListView>(R.id.lv_viewing_list)

        val items = listObj!!.items

        adapter = if (items != null) {
            TaskItemListAdapter(context, items, itemInfoClickListener)
        } else {
            TaskItemListAdapter(context, mutableListOf<TaskItem>(), itemInfoClickListener)
        }
        lv!!.adapter = adapter

        viewModel.lists.observe(context) {
            adapter?.notifyDataSetChanged()
        }


        val addTaskButton: FloatingActionButton? = rootView.findViewById(R.id.fab_add_task)
        addTaskButton?.setOnClickListener { _ ->
            val createTaskItemFragment = CreateTaskItemFragment()
            val bundle = Bundle()
            bundle.putInt("LIST_ID", listId!!)
            createTaskItemFragment.arguments = bundle
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, createTaskItemFragment)
                .addToBackStack(null)
                .commit()
        }

        return rootView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    // https://stackoverflow.com/questions/21504088
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as? AppCompatActivity)?.supportActionBar?.hide()

    }

    override fun onDestroy() {
        super.onDestroy()
        (requireActivity() as? AppCompatActivity)?.supportActionBar?.show()
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as? AppCompatActivity)?.supportActionBar?.hide()
        adapter?.notifyDataSetChanged()
//        adapter = listObj!!.items?.let { items ->
//            TaskItemListAdapter(
//                context,
//                items
//            )
//        }
//
//        lv!!.adapter = adapter
//        adapter?.notifyDataSetChanged()
//        adapter?.notifyDataSetInvalidated()
    }

//    override fun onDestroy() {
//        closeActivityWithResult()
//        super.onDestroy()
//    }

//    private fun closeActivityWithResult(){
//        val resultIntent = Intent()
//        resultIntent.putExtra("MUTATED_LIST", listObj)
//        setResult(AppCompatActivity.RESULT_OK, resultIntent)
//    }
}