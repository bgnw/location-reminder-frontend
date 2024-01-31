package com.bgnw.locationreminder

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.bgnw.locationreminder.databinding.FragmentNearbyBinding
import java.time.format.DateTimeFormatter

@Suppress("DEPRECATION") // suppress deprecation warning for getParcelableExtra, handled.
class ViewTaskListActivity : AppCompatActivity() {
    private lateinit var binding: FragmentNearbyBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_task_list)

        val list : TaskList? = if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra("selected_list", TaskList::class.java)
        } else {
            intent.getParcelableExtra<TaskList>("selected_list")
        }
        title = list?.name
//        findViewById<TextView>(R.id.list_name).text = list?.toString() ?: "null sent"


        binding = FragmentNearbyBinding.inflate(layoutInflater)

        val lv = this.findViewById(R.id.lv_viewing_list) as ListView
        val adapter = TaskItemListAdapter(this, list!!.items) // TODO better approach than non-null guarantee?
        lv.adapter = adapter
    }
}