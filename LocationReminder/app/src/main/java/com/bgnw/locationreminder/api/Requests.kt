package com.bgnw.locationreminder.api

import AccountApi
import TaskItemApi
import TaskListApi
import android.util.Log
import com.bgnw.locationreminder.activity.CreateTaskItemActivity
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
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
        ): TaskList {
            // *************** CREATE TASK LIST  *************************
            return suspendCoroutine { continuation ->

                var dateFormatZulu: DateTimeFormatter =
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")

                GlobalScope.launch(Dispatchers.IO) {
                    val obj = TaskList(
                        list_id = null,
                        title = title,
                        icon_name = icon_name,
                        created_at = (LocalDateTime.now().format(dateFormatZulu).toString()),
                        owner = owner_username,
                        sort_by = sort_by,
                        visibility = visibility
                    )
                    Log.d("bgnw_DJA API", obj.toString())
                    var call = taskListApi.createList(obj)

                    call.enqueue(object : Callback<TaskList> {
                        override fun onFailure(call: Call<TaskList>, t: Throwable) {
                            continuation.resumeWith(Result.failure(Exception("Django REST API call failed")))
                        }

                        override fun onResponse(
                            call: Call<TaskList>,
                            response: Response<TaskList>
                        ) {
                            if (response.body() == null) {
                                continuation.resumeWith(Result.failure(Exception("Null TaskList object found when looking at Django REST API response")))
                            } else {
                                continuation.resumeWith(Result.success(response.body()!!))

                            }
                        }


                    })
                }
            }
        }



        @OptIn(DelicateCoroutinesApi::class)
        suspend fun createItem(
            list: Int,
            owner: String,
            title: String,
            body_text: String,
            remind_method: String?,
            attachment_img_path: String?,
            snooze_until: String?,
            completed: Boolean,
            due_at: String?,
            is_sub_task: Boolean,
            parent_task: Int?,
            filters: Collection<CreateTaskItemActivity.TagValuePair>,
        ): TaskItem {
            // *************** CREATE TASK ITEM  *************************
            return suspendCoroutine { continuation ->
                GlobalScope.launch(Dispatchers.IO) {
                    val obj = TaskItem(
                        item_id = null,
                        list = list,
                        owner = owner,
                        title = title,
                        body_text = body_text,
                        remind_method = remind_method,
                        attachment_img_path = attachment_img_path,
                        is_sub_task = is_sub_task,
                        parent_task = parent_task,
                        completed = completed,
                        snooze_until = snooze_until,
                        due_at = due_at,
                        filters = TaskItem.convertFiltersToMap(filters)
                    )
                    var call = taskItemApi.createItem(obj)

                    call.request().body

                    Log.d("bgnw", "createitem call:")
                    Log.d("bgnw", call.request().body.toString())

                    call.enqueue(object : Callback<TaskItem> {
                        override fun onFailure(call: Call<TaskItem>, t: Throwable) {
                            continuation.resumeWith(Result.failure(Exception("Django REST API call failed")))
                        }

                        override fun onResponse(
                            call: Call<TaskItem>,
                            response: Response<TaskItem>
                        ) {
                            if (response.body() == null) {
                                continuation.resumeWith(Result.failure(Exception("Null TaskItem object found when looking at Django REST API response")))
                            } else {
                                continuation.resumeWith(Result.success(response.body()!!))
                            }
                        }
                    })
                }
            }
        }





        @OptIn(DelicateCoroutinesApi::class)
        suspend fun getListItemsById(
            listId: Int,
        ): List<TaskItem>? = withContext(Dispatchers.IO) {
            // *************** LOOKUP LIST ITEMS  *************************
            return@withContext suspendCoroutine { continuation ->
                GlobalScope.launch(Dispatchers.IO) {

                    var call = taskItemApi.getListItems(listId, "json")

                    call.enqueue(object : Callback<List<TaskItem>> {
                        override fun onFailure(call: Call<List<TaskItem>>, t: Throwable) {
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
                        override fun onFailure(
                            call: Call<MutableList<ItemOpportunity>>,
                            t: Throwable
                        ) {
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
                                continuation.resume(null)
                            }
                        }
                    })
                }
            }
        }

        @OptIn(DelicateCoroutinesApi::class)
        suspend fun getTaskListsByUsername(username: String): MutableList<TaskList>? =
            withContext(Dispatchers.IO) {
                return@withContext suspendCoroutine { continuation ->
                    val call = taskListApi.getOwnedLists(username, "json")

                    call.enqueue(object : Callback<List<TaskList>> {
                        override fun onFailure(call: Call<List<TaskList>>, t: Throwable) {
                            continuation.resume(null)
                        }

                        override fun onResponse(
                            call: Call<List<TaskList>>,
                            response: Response<List<TaskList>>
                        ) {
                            if (response.isSuccessful) {
                                val responseBody = response.body()
                                continuation.resume(
                                    responseBody?.toMutableList() ?: mutableListOf()
                                )
                            } else {
                                continuation.resume(null)
                            }
                        }
                    })
                }
            }


        @OptIn(DelicateCoroutinesApi::class)
        suspend fun getFiltersForUser(username: String): List<String>? =
            withContext(Dispatchers.IO) {
                return@withContext suspendCoroutine { continuation ->
                    val call = taskItemApi.getFiltersForUser(username, "json")

                    call.enqueue(object : Callback<List<Map<String, String>>?> {
                        override fun onFailure(call: Call<List<Map<String, String>>?>, t: Throwable) {
                            continuation.resume(null)
                        }

                        override fun onResponse(
                            call: Call<List<Map<String, String>>?>,
                            response: Response<List<Map<String, String>>?>
                        ) {
                            if (response.isSuccessful) {
                                val responseBody = response.body()

                                val filtersAsStrings: MutableList<String> = mutableListOf()

                                if (responseBody != null) {
                                    responseBody.forEach { el ->
                                        el.get("filters")?.let { filtersAsStrings.add(it) }
                                    }
                                    continuation.resume(filtersAsStrings)
                                }
                                else {
                                    continuation.resume(null)
                                }
                            } else {
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
                 }

                 override fun onResponse(
                     call: Call<List<TaskList>>,
                     response: Response<List<TaskList>>
                 ) {
                     val responseBody = response.body()


                     GlobalScope.launch(Dispatchers.IO) {
                         if (responseBody != null) {
                             for (list: TaskList in responseBody) {
                                 if (list.list_id == null) continue
                                 val items = getListItemsById(list.list_id, null)
                                 list.items = items
                             }
                         } else {
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
//                        }
//
//                        override fun onResponse(
//                            call: Call<List<TaskList>>,
//                            response: Response<List<TaskList>>
//                        ) {
//                            val responseBody = response.body()
//
//
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