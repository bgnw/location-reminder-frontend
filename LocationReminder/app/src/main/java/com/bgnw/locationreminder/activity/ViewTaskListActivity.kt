package com.bgnw.locationreminder.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
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
    private var adapter: TaskItemListAdapter? = null
    private var lv: ListView? = null



    private fun closeActivityWithResult(){
        val resultIntent = Intent()
        resultIntent.putExtra("MUTATED_LIST", this.origList)
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
                if (this.origList?.items == null) {
                    this.origList?.items = mutableListOf(item)
                }
                else {
                    this.origList!!.items?.add(item)
                }

                adapter = this.origList!!.items?.let { items ->
                    TaskItemListAdapter(
                        this,
                        items
                    )
                }

                lv!!.adapter = adapter



                viewModel;
                viewModel.lists;
                viewModel.lists.value;

                adapter?.notifyDataSetChanged()
                adapter?.notifyDataSetInvalidated()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_task_list)

        this.origList = if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra("selected_list", TaskList::class.java)
        } else {
            intent.getParcelableExtra<TaskList>("selected_list")
        }

        if (this.origList == null) {
            Log.d("bgnw-data", "ORIG LIST IS NULL")
        }

        title = this.origList?.title

        val unpackedUsername = intent.getStringExtra("username")
        if (unpackedUsername.isNullOrBlank()) {
            throw Exception("username cannot be blank here")
        }
        username = unpackedUsername

        binding = FragmentNearbyBinding.inflate(layoutInflater)

        lv = this.findViewById(R.id.lv_viewing_list) as ListView
        val items = this.origList!!.items
        adapter = if (items != null) {
            TaskItemListAdapter(
                this,
                this.origList!!.items!!
            )
        } else {
            TaskItemListAdapter(
                this,
                mutableListOf<TaskItem>()
            )
        }
        lv!!.adapter = adapter

        viewModel.lists.observe(this) {
            adapter?.notifyDataSetChanged()
        }


        val addTaskButton: FloatingActionButton? = this.findViewById(R.id.fab_add_task)
        addTaskButton?.setOnClickListener { _ ->
            val intent = Intent(this, CreateTaskItemActivity::class.java)
            intent.putExtra("listID", this.origList!!.list_id)
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