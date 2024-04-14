package com.bgnw.locationreminder

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.bgnw.locationreminder.api.Requests
import com.bgnw.locationreminder.data.Collab
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CollabListAdapter(
    private val context: Activity,
    private val items: MutableList<Collab>
) : ArrayAdapter<Collab>(context, R.layout.user_item, items) {

    fun addItem(collab: Collab) {
        items.add(collab)
        this.notifyDataSetChanged()
        this.notifyDataSetInvalidated()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val inflater: LayoutInflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.user_item, null)

        val liTitle: TextView = view.findViewById(R.id.ui_title)
        val action1Button: Button = view.findViewById(R.id.ui_action1_btn)
        val action2Button: Button = view.findViewById(R.id.ui_action2_btn)

        val username = context.findViewById<NavigationView>(R.id.nav_view).getHeaderView(0)
            .findViewById<TextView>(R.id.nav_user_username).text

        val otherUser =
            if (items[position].user_master != username)
                items[position].user_master
            else
                items[position].user_peer
        liTitle.text = buildSpannedString {
            bold { append(otherUser) }
        }

        action1Button.visibility = View.GONE
        action2Button.text = "Remove"
        var selectedItem: Collab

        action2Button.setOnClickListener {
            selectedItem = items[position]
            val returnedCollab: MutableLiveData<Collab> = MutableLiveData()
            CoroutineScope(Dispatchers.IO).launch {
                returnedCollab.postValue(Requests.deleteCollab(selectedItem.collab_id!!))
            }
            returnedCollab.observe(parent.context as LifecycleOwner) { data ->
                val x = items.removeIf { item ->
                    item.user_master == selectedItem.user_master
                            && item.user_peer == selectedItem.user_peer
                }
                this.notifyDataSetChanged()
                this.notifyDataSetInvalidated()
            }
        }

        return view
    }

}