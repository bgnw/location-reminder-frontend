package com.bgnw.locationreminder

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.bgnw.locationreminder.api.Requests
import com.bgnw.locationreminder.data.Collab
import com.bgnw.locationreminder.data.CollabReq
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CollabReqListAdapter(
    private val context: Activity,
    private val items: MutableList<CollabReq>
) : ArrayAdapter<CollabReq>(context, R.layout.user_item, items) {

    private val dtHuman: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM 'at' HH:mm")
    private val dtZulu: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val inflater: LayoutInflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.user_item, null)

        val collabsLv = context.findViewById<ListView>(R.id.ui_collabs_lv)

        val liTitle: TextView = view.findViewById(R.id.ui_title)
        val liSubtitle: TextView = view.findViewById(R.id.ui_subtitle)
        val action1Button: Button = view.findViewById(R.id.ui_action1_btn)
        val action2Button: Button = view.findViewById(R.id.ui_action2_btn)

        liTitle.text = buildSpannedString {
            bold { append(items[position].user_sender) }
            append(" sent you a friend request ")
        }
        liSubtitle.text = "Requested at ${
            LocalDateTime.parse(items[position].datetime_sent, dtZulu).format(dtHuman)
        }"

        var selectedItem: CollabReq

        action1Button.setOnClickListener {
            selectedItem = items[position]
            val returnedCollab: MutableLiveData<Collab> = MutableLiveData()
            CoroutineScope(Dispatchers.IO).launch {
                returnedCollab.postValue(Requests.acceptCollabReq(selectedItem))
            }
            returnedCollab.observe(parent.context as LifecycleOwner) { data ->
                items.remove(selectedItem)
                this.notifyDataSetChanged()
                this.notifyDataSetInvalidated()
            }

            selectedItem = items[position]
            CoroutineScope(Dispatchers.IO).launch {
            }
            returnedCollab.observe(parent.context as LifecycleOwner) { data ->
                items.remove(selectedItem)
                this.notifyDataSetChanged()
                this.notifyDataSetInvalidated()
                (collabsLv.adapter as CollabListAdapter).addItem(data)
            }
        }

        action2Button.setOnClickListener {
            selectedItem = items[position]
            val result: MutableLiveData<CollabReq?> = MutableLiveData()
            CoroutineScope(Dispatchers.IO).launch {
                result.postValue(Requests.rejectCollabReq(selectedItem))
            }
            result.observe(parent.context as LifecycleOwner) { data ->
                items.remove(selectedItem)
                this.notifyDataSetChanged()
                this.notifyDataSetInvalidated()
            }

            selectedItem = items[position]
            CoroutineScope(Dispatchers.IO).launch {
                result.postValue(Requests.rejectCollabReq(selectedItem))
            }
            result.observe(parent.context as LifecycleOwner) { data ->
                items.remove(selectedItem)
                this.notifyDataSetChanged()
                this.notifyDataSetInvalidated()
            }
        }

        return view
    }
}