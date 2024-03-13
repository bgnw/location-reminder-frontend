package com.bgnw.locationreminder.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import com.bgnw.locationreminder.ApplicationState
import com.bgnw.locationreminder.R
import com.bgnw.locationreminder.TaskItemListAdapter
import com.bgnw.locationreminder.data.TaskItem
import com.bgnw.locationreminder.data.TaskList
import com.bgnw.locationreminder.databinding.FragmentNearbyBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton

@Suppress("DEPRECATION") // suppress deprecation warning for getParcelableExtra, handled.
class ViewTaskListActivity : AppCompatActivity() {
    private lateinit var binding: FragmentNearbyBinding
    private lateinit var username: String


    private val viewModel: ApplicationState by viewModels()
    private var origList: TaskList? = null
    private var list: TaskList? = null
    private var adapter: TaskItemListAdapter? = null


    private fun closeActivityWithResult(){
        val resultIntent = Intent()
        resultIntent.putExtra("MUTATED_LIST", list)
        setResult(RESULT_OK, resultIntent)
        finish()
    }


    private val request = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            val data = it.data
            // TODO !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            Log.d("bgnw-data", "lemme addddddd")
            Log.d("bgnw-data", data.toString())
            val item: TaskItem? = data?.extras?.getParcelable<TaskItem>("NEW_ITEM")

            if (item != null ) {
                list?.items?.add(item)
                adapter?.notifyDataSetChanged()
                adapter?.notifyDataSetInvalidated()
                viewModel.lists.value?.get(item.list)?.items?.add(item)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_task_list)

        list = if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra("selected_list", TaskList::class.java)
        } else {
            intent.getParcelableExtra<TaskList>("selected_list")
        }

        if (list == null) { origList = null }
        else { origList = TaskList(list!!) }

        title = list?.title

        val unpackedUsername = intent.getStringExtra("username")
        if (unpackedUsername.isNullOrBlank()) {
            throw Exception("username cannot be blank here")
        }
        username = unpackedUsername

        binding = FragmentNearbyBinding.inflate(layoutInflater)

        val lv = this.findViewById(R.id.lv_viewing_list) as ListView
        adapter = list!!.items?.let {
            TaskItemListAdapter(
                this,
                it
            )
        }
        lv.adapter = adapter


        val addTaskButton: FloatingActionButton? = this.findViewById(R.id.fab_add_task)
        addTaskButton?.setOnClickListener { _ ->
            val intent = Intent(this, CreateTaskItemActivity::class.java)
            intent.putExtra("listID", list!!.list_id)
            intent.putExtra("username", username)
            request.launch(intent)
        }
    }

    override fun onBackPressed() {
        closeActivityWithResult()
        super.onBackPressed()
    }

    override fun onDestroy() {
        closeActivityWithResult()
        super.onDestroy()
    }

}