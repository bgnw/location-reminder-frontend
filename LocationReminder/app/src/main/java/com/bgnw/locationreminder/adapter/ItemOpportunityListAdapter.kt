package com.bgnw.locationreminder.adapter

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.bgnw.locationreminder.R
import com.bgnw.locationreminder.data.ItemOpportunity
import java.time.format.DateTimeFormatter

class ItemOpportunityListAdapter(
    private val context: Activity,
    private val opps: List<ItemOpportunity>
) : ArrayAdapter<ItemOpportunity>(context, R.layout.list_task_item, opps) {


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val inflater: LayoutInflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.list_task_item, null)

        val opp = opps[position]
        val liTitle: TextView = view.findViewById(R.id.li_title)
        val liSubtitle: TextView = view.findViewById(R.id.li_subtitle)

        liTitle.text = opp.place_name
        liSubtitle.text = opp.category

        return view
    }
}