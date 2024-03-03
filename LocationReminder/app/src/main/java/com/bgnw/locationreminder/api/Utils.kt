package com.bgnw.locationreminder.api

import android.util.Log
import com.bgnw.locationreminder.data.ItemOpportunity
import com.bgnw.locationreminder.data.TaskItem
import com.bgnw.locationreminder.data.TaskList
import org.osmdroid.util.GeoPoint

class Utils {
    companion object Factory {
        fun getOppsFromLists(lists: MutableList<TaskList>?): MutableList<ItemOpportunity>? {
            if (lists == null) {
                return null
            }

            val opps: MutableList<ItemOpportunity> = mutableListOf();

            for (list: TaskList in lists) {
                if (list.items == null) {
                    return null
                }
                for (item: TaskItem in list.items!!) {
                    if (item.opportunities == null) {
                        return null
                    }
                    for (opp: ItemOpportunity in item.opportunities!!) {
                        opps.add(opp)
                    }
                }
            }

            return opps
        }
    }
}