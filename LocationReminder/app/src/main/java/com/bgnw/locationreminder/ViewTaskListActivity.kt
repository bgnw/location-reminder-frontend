package com.bgnw.locationreminder

import android.os.Build
import android.os.Bundle
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.bgnw.locationreminder.databinding.FragmentNearbyBinding

@Suppress("DEPRECATION") // suppress deprecation warning for getParcelableExtra, handled.
class ViewTaskListActivity : AppCompatActivity() {
    private lateinit var binding: FragmentNearbyBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_task_list)

        val list: TaskList? = if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra("selected_list", TaskList::class.java)
        } else {
            intent.getParcelableExtra<TaskList>("selected_list")
        }
        title = list?.title
//        findViewById<TextView>(R.id.list_name).text = list?.toString() ?: "null sent"


        binding = FragmentNearbyBinding.inflate(layoutInflater)

        val lv = this.findViewById(R.id.lv_viewing_list) as ListView
        val adapter =
            TaskItemListAdapter(this, list!!.items) // TODO better approach than non-null guarantee?
        lv.adapter = adapter
    }
}