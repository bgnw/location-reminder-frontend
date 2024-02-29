package com.bgnw.locationreminder.api

import AccountApi
import TaskItemApi
import TaskListApi
import android.util.Log
import android.widget.TextView
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
        suspend fun lookupUser(username: String, tv: TextView?): Account_ApiStruct {
            // *************** LOOKUP ACCOUNT  *************************
            return suspendCoroutine { continuation ->
                GlobalScope.launch(Dispatchers.IO) {
                    var call = accountApi.getAccount(username, "json")

                    call.enqueue(object : Callback<Account_ApiStruct> {
                        override fun onFailure(call: Call<Account_ApiStruct>, t: Throwable) {
                            continuation.resumeWithException(t)
                        }

                        override fun onResponse(
                            call: Call<Account_ApiStruct>,
                            response: Response<Account_ApiStruct>
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
            tv: TextView?
        ): Account_ApiStruct {
            // *************** LOOKUP ACCOUNT  *************************
            return suspendCoroutine { continuation ->
                GlobalScope.launch(Dispatchers.IO) {
                    var call = accountApi.getAccount(username, "json")

                    call.enqueue(object : Callback<Account_ApiStruct> {
                        override fun onFailure(call: Call<Account_ApiStruct>, t: Throwable) {
                            continuation.resumeWithException(t)
                        }

                        override fun onResponse(
                            call: Call<Account_ApiStruct>,
                            response: Response<Account_ApiStruct>
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
        suspend fun createList(
            title: String,
            icon_name: String?,
            owner_username: String,
            sort_by: String,
            visibility: Int,
            tv: TextView?
        ): Account_ApiStruct {
            // *************** CREATE TASK LIST  *************************
            return suspendCoroutine { continuation ->
                GlobalScope.launch(Dispatchers.IO) {
                    val obj = TaskList_ApiStruct(
                        list_id = 4, // TODO real data
                        title = "test3",
                        icon_name = "none",
                        created_at = "xx TODO",
                        owner_username = "sim",
                        sort_by = "name",
                        visibility = 0
                    )
                    Log.d("DJA API", obj.toString())
                    var call = taskListApi.createList(obj)

                    call.enqueue(object : Callback<TaskList_ApiStruct> {
                        override fun onFailure(call: Call<TaskList_ApiStruct>, t: Throwable) {
                            Log.d("DJA API", "ERROR: $t")
                            tv?.text = "ERROR: $t"
                        }

                        override fun onResponse(
                            call: Call<TaskList_ApiStruct>,
                            response: Response<TaskList_ApiStruct>
                        ) {
                            Log.d("DJA API", "RESPONSE: ${response.body().toString()}")
                            tv?.text = "RESPONSE: ${response.body().toString()}"
                        }

                    })
                }
            }
        }


        @OptIn(DelicateCoroutinesApi::class)
        suspend fun getListItems(
            list_id: Int,
            tv: TextView?
        ): Account_ApiStruct {
            // *************** LOOKUP LIST ITEMS  *************************
            return suspendCoroutine { continuation ->
                GlobalScope.launch(Dispatchers.IO) {

                    var call = taskItemApi.getListItems(list_id, "json")

                    call.enqueue(object : Callback<List<TaskItem_ApiStruct>> {
                        override fun onFailure(call: Call<List<TaskItem_ApiStruct>>, t: Throwable) {
                            Log.d("DJA API", "ERROR: $t")
                            tv?.text = "ERROR: $t"
                        }

                        override fun onResponse(
                            call: Call<List<TaskItem_ApiStruct>>,
                            response: Response<List<TaskItem_ApiStruct>>
                        ) {
                            Log.d("DJA API", "RESPONSE: ${response.body().toString()}")
                            tv?.text = "RESPONSE: ${response.body().toString()}"
                        }

                    })
                }
            }
        }


    }
}