package com.bgnw.locationreminder.api

import android.util.Log
import com.bgnw.locationreminder.data.ItemOpportunity
import com.bgnw.locationreminder.data.TaskItem
import com.bgnw.locationreminder.data.TaskList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class Utils {
    companion object Factory {
        fun getUpdatedTLs(username: String): Deferred<MutableList<TaskList>?> {
            // retrieve all task lists, items, etc associated with this user
            val resultTLDeferred = CoroutineScope(Dispatchers.IO).async {
                val resultTL = Requests.getTaskListsByUsername(username)
                if (resultTL != null) {
                    for (list: TaskList in resultTL) {
                        if (list.list_id == null) continue
                        val items = Requests.getListItemsById(list.list_id)
                        list.items = items?.toMutableList() ?: mutableListOf()

//                        if (items != null) {
//                            for (item: TaskItem in items) {
//                                if (item.item_id != null) {
//                                    val results =
//                                        Requests.getItemOpportunitiesByItemId(item.item_id!!)
//                                    Log.d("bgnw_DJA API", "[opps] results: $results")
//                                    item.opportunities = results
//
//                                }
//                            }
//                        }
                    }
                }

                Log.d("bgnw_DJA API", "UPDATED TLS")
                Log.d("bgnw_DJA API", resultTL.toString())
                return@async resultTL
            }
            return resultTLDeferred
        }


//        fun getOppsFromLists(lists: List<TaskList>?): MutableList<ItemOpportunity>? {
//            if (lists == null) {
//                return null
//            }
//
//            val opps: MutableList<ItemOpportunity> = mutableListOf()
//
//            for (list: TaskList in lists) {
//                if (list.items == null) {
//                    return null
//                }
//                for (item: TaskItem in list.items!!) {
//                    if (item.opportunities == null) {
//                        return null
//                    }
//                    for (opp: ItemOpportunity in item.opportunities!!) {
//                        opps.add(opp)
//                    }
//                }
//            }
//
//            return opps
//        }

        fun getFiltersForUserDeferred(username: String): Deferred<List<String>?> {
            // retrieve all task lists, items, etc associated with this user
            val resultsDeferred = CoroutineScope(Dispatchers.IO).async {
                val results = Requests.getFiltersForUser(username)
                return@async results
            }
            return resultsDeferred
        }
    }
}