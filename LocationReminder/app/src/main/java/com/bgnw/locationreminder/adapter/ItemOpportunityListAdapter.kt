package com.bgnw.locationreminder.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import com.bgnw.locationreminder.R
import com.bgnw.locationreminder.data.ItemOpportunity

class ItemOpportunityListAdapter(
    private val context: Activity,
    private val opps: MutableList<ItemOpportunity>
) : ArrayAdapter<ItemOpportunity>(context, R.layout.list_task_item, opps) {


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val inflater: LayoutInflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.list_task_item, null)

        val opp = opps[position]
        val liTitle: TextView = view.findViewById(R.id.li_title)
        val liSubtitle: TextView = view.findViewById(R.id.li_subtitle)
        val liButton: Button = view.findViewById(R.id.item_info_btn)

        liTitle.text = opp.place_name ?: opp.category
        liTitle.isSingleLine = false
        liTitle.maxLines = 2
        liSubtitle.text =
            "Matches ${opp.matchingItemCount} ${if (opp.matchingItemCount == 1) "item" else "items"}, ${opp.metresFromUser}m away"
        liButton.visibility = View.GONE

        return view
    }
}