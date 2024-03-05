package com.bgnw.locationreminder.api

import AccountApi
import TaskItemApi
import TaskListApi
import android.util.Log
import android.widget.TextView
import com.bgnw.locationreminder.data.Account
import com.bgnw.locationreminder.data.ItemOpportunity
import com.bgnw.locationreminder.data.TaskItem
import com.bgnw.locationreminder.data.TaskList
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class Requests {

    companion object Factory {
        val initialised = false
        lateinit var retrofit: Retrofit
        lateinit var accountApi: AccountApi
        lateinit var taskListApi: TaskListApi
        lateinit var taskItemApi: TaskItemApi

        @OptIn(DelicateCoroutinesApi::class)
        fun initialiseApi() {
            if (!initialised) {
                retrofit = Retrofit.Builder()
                    .baseUrl("http://13.51.162.189/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                accountApi = retrofit.create(AccountApi::class.java)
                taskListApi = retrofit.create(TaskListApi::class.java)
                taskItemApi = retrofit.create(TaskItemApi::class.java)
            }
        }

        @OptIn(DelicateCoroutinesApi::class)
        suspend fun lookupUser(username: String): Account {
            // *************** LOOKUP ACCOUNT  *************************
            return suspendCoroutine { continuation ->
                GlobalScope.launch(Dispatchers.IO) {
                    var call = accountApi.getAccount(username, "json")

                    call.enqueue(object : Callback<Account> {
                        override fun onFailure(call: Call<Account>, t: Throwable) {
                            continuation.resumeWithException(t)
                        }

                        override fun onResponse(
                            call: Call<Account>,
                            response: Response<Account>
                        ) {
                            if (response.isSuccessful) {
                                val account = response.body()
                                if (account != null) {
                                    continuation.resume(account)
                                } else {
                                    continuation.resumeWithException(NullPointerException("Response body is null"))
                                }
                            } else {
                                continuation.resumeWithException(HttpException(response))
                            }
                        }
                    })
                }
            }
        }

        @OptIn(DelicateCoroutinesApi::class)
        suspend fun authenticateUser(
            username: String,
            password: String,
        ): AuthResponse? = withContext(Dispatchers.IO) {
            return@withContext suspendCoroutine { continuation ->
                val call = accountApi.authenticate(username, password, "json")

                call.enqueue(object : Callback<AuthResponse> {
                    override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                        Log.d("bgnw_DJA API", "ERROR: $t")
                        continuation.resume(null)
                    }

                    override fun onResponse(
                        call: Call<AuthResponse>,
                        response: Response<AuthResponse>
                    ) {
                        if (response.isSuccessful) {
                            val responseBody = response.body()
                            continuation.resume(responseBody)
                        } else {
                            Log.d("bgnw_DJA API", "body is null")
                            continuation.resume(null)
                        }
                    }
                })
            }
            }

        @OptIn(DelicateCoroutinesApi::class)
        suspend fun createList(
            title: String,
            icon_name: String?,
            owner_username: String,
            sort_by: String,
            visibility: Int,
            tv: TextView?
        ): Account {
            // *************** CREATE TASK LIST  *************************
            return suspendCoroutine { continuation ->
                GlobalScope.launch(Dispatchers.IO) {
                    val obj = TaskList(
                        list_id = 4, // TODO real data
                        title = "test3",
                        icon_name = "none",
                        created_at = "xx TODO",
                        owner = "sim",
                        sort_by = "name",
                        visibility = 0
                    )
                    Log.d("bgnw_DJA API", obj.toString())
                    var call = taskListApi.createList(obj)

                    call.enqueue(object : Callback<TaskList> {
                        override fun onFailure(call: Call<TaskList>, t: Throwable) {
                            Log.d("bgnw_DJA API", "ERROR: $t")
                            tv?.text = "ERROR: $t"
                        }

                        override fun onResponse(
                            call: Call<TaskList>,
                            response: Response<TaskList>
                        ) {
                            Log.d("bgnw_DJA API", "RESPONSE: ${response.body().toString()}")
                            tv?.text = "RESPONSE: ${response.body().toString()}"
                        }

                    })
                }
            }
        }


        @OptIn(DelicateCoroutinesApi::class)
        suspend fun getListItemsById(
            listId: Int,
            tv: TextView?
        ): List<TaskItem>? = withContext(Dispatchers.IO) {
            // *************** LOOKUP LIST ITEMS  *************************
            return@withContext suspendCoroutine { continuation ->
                GlobalScope.launch(Dispatchers.IO) {

                    var call = taskItemApi.getListItems(listId, "json")

                    call.enqueue(object : Callback<List<TaskItem>> {
                        override fun onFailure(call: Call<List<TaskItem>>, t: Throwable) {
                            Log.d("bgnw_DJA API", "ERROR: $t")
                            continuation.resume(null)
                        }

                        override fun onResponse(
                            call: Call<List<TaskItem>>,
                            response: Response<List<TaskItem>>
                        ) {
                            if (response.isSuccessful) {
                                val responseBody = response.body()
                                continuation.resume(responseBody)
                            } else {
                                Log.d("bgnw_DJA API", "body is null")
                                continuation.resume(null)
                            }
                        }
                    })
                }
            }
        }

        @OptIn(DelicateCoroutinesApi::class)
        suspend fun getItemOpportunitiesByItemId(
            itemId: Int,
        ): MutableList<ItemOpportunity>? = withContext(Dispatchers.IO) {
            // *************** LOOKUP LIST ITEMS  *************************
            return@withContext suspendCoroutine { continuation ->
                GlobalScope.launch(Dispatchers.IO) {

                    var call = taskItemApi.getItemOpps(itemId, "json")

                    call.enqueue(object : Callback<MutableList<ItemOpportunity>> {
                        override fun onFailure(call: Call<MutableList<ItemOpportunity>>, t: Throwable) {
                            Log.d("bgnw_DJA API", "[opps] ERROR: $t")
                            continuation.resume(null)
                        }

                        override fun onResponse(
                            call: Call<MutableList<ItemOpportunity>>,
                            response: Response<MutableList<ItemOpportunity>>
                        ) {
                            if (response.isSuccessful) {
                                val responseBody = response.body()
                                continuation.resume(responseBody)
                            } else {
                                Log.d("bgnw_DJA API", "[opps] body is null")
                                continuation.resume(null)
                            }
                        }
                    })
                }
            }
        }

        @OptIn(DelicateCoroutinesApi::class)
        suspend fun getTaskListsByUsername(username: String): List<TaskList>? = withContext(Dispatchers.IO) {
            return@withContext suspendCoroutine { continuation ->
                val call = taskListApi.getOwnedLists(username, "json")

                call.enqueue(object : Callback<List<TaskList>> {
                    override fun onFailure(call: Call<List<TaskList>>, t: Throwable) {
                        Log.d("bgnw_DJA API", "ERROR: $t")
                        continuation.resume(null)
                    }

                    override fun onResponse(
                        call: Call<List<TaskList>>,
                        response: Response<List<TaskList>>
                    ) {
                        if (response.isSuccessful) {
                            val responseBody = response.body()
                            continuation.resume(responseBody)
                            } else {
                                Log.d("bgnw_DJA API", "body is null")
                                continuation.resume(null)
                            }
                        }
                })
            }
        }


        /* @OptIn(DelicateCoroutinesApi::class)
         suspend fun getTaskListsByUsername(username: String): List<TaskList>? = withContext(Dispatchers.IO) {
             var call = taskListApi.getOwnedLists(username, "json")

             call.enqueue(object : Callback<List<TaskList>> {
                 override fun onFailure(call: Call<List<TaskList>>, t: Throwable) {
                     Log.d("bgnw_DJA API", "ERROR: $t")
                 }

                 override fun onResponse(
                     call: Call<List<TaskList>>,
                     response: Response<List<TaskList>>
                 ) {
                     val responseBody = response.body()


                     Log.d("bgnw_DJA API", "RESPONSE: ${responseBody.toString()}")
                     GlobalScope.launch(Dispatchers.IO) {
                         if (responseBody != null) {
                             for (list: TaskList in responseBody) {
                                 if (list.list_id == null) continue
                                 val items = getListItemsById(list.list_id, null)
                                 list.items = items
                             }
                         } else {
                             Log.d("bgnw_DJA API", "body is null")
                         }
                     }
                 }
             })
             return@withContext null
         } */



//            // *************** LOOKUP LIST ITEMS  *************************
//            return suspendCoroutine { continuation ->
//                GlobalScope.launch(Dispatchers.IO) {
//                    var call = taskListApi.getOwnedLists(username, "json")
//
//                    call.enqueue(object : Callback<List<TaskList>> {
//                        override fun onFailure(call: Call<List<TaskList>>, t: Throwable) {
//                            Log.d("bgnw_DJA API", "ERROR: $t")
//                        }
//
//                        override fun onResponse(
//                            call: Call<List<TaskList>>,
//                            response: Response<List<TaskList>>
//                        ) {
//                            val responseBody = response.body()
//
//
//                            Log.d("bgnw_DJA API", "RESPONSE: ${responseBody.toString()}")
//                            GlobalScope.launch(Dispatchers.IO) {
//                                if (responseBody != null) {
//                                    for (list: TaskList in responseBody) {
//                                        if (list.list_id == null) continue
//                                        val items = getListItemsById(list.list_id, null)
//                                        list.items = items
//
//                                        richLists.add(list)
//                                    }
//                                }
//                            }
//                            return richLists.toList()
//                        }
//
//                    })
//                }
//            }
//        }




    }
}