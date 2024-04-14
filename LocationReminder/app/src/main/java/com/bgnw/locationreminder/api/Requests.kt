package com.bgnw.locationreminder.api

import AccountApi
import CollabApi
import LogApi
import TaskItemApi
import TaskListApi
import com.bgnw.locationreminder.data.Account
import com.bgnw.locationreminder.data.AccountPartialForLocation
import com.bgnw.locationreminder.data.Collab
import com.bgnw.locationreminder.data.CollabReq
import com.bgnw.locationreminder.data.TaskItem
import com.bgnw.locationreminder.data.TaskList
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.math.RoundingMode
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import com.bgnw.locationreminder.data.Log as Log1


class Requests {

    companion object Factory {
        private var initialised = false
        private lateinit var retrofit: Retrofit
        private lateinit var accountApi: AccountApi
        private lateinit var taskListApi: TaskListApi
        private lateinit var taskItemApi: TaskItemApi
        private lateinit var logApi: LogApi
        private lateinit var collabApi: CollabApi

        fun initialiseApi() {
            if (!initialised) {
                val client = OkHttpClient.Builder()
                    .addInterceptor(JsonHeaderInterceptor())
                    .build()

                retrofit = Retrofit.Builder()
                    .baseUrl("http://13.51.162.189/")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                accountApi = retrofit.create(AccountApi::class.java)
                taskListApi = retrofit.create(TaskListApi::class.java)
                taskItemApi = retrofit.create(TaskItemApi::class.java)
                logApi = retrofit.create(LogApi::class.java)
                collabApi = retrofit.create(CollabApi::class.java)

                initialised = true
            }
        }

        @OptIn(DelicateCoroutinesApi::class)
        suspend fun updateItem(
            itemId: Int,
            itemWithUpdates: TaskItem,
            callback: (success: Boolean) -> Unit
        ) {
            return suspendCoroutine { continuation ->
                GlobalScope.launch(Dispatchers.IO) {

                    val call = taskItemApi.updateItem(item_id = itemId, body = itemWithUpdates)

                    call.enqueue(object : Callback<Void> {
                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            callback.invoke(false)
                        }

                        override fun onResponse(
                            call: Call<Void>,
                            response: Response<Void>
                        ) {
                            if (response.isSuccessful) {
                                callback.invoke(true)
                            } else {
                                callback.invoke(false)
                            }
                        }
                    })
                }
            }
        }

        @OptIn(DelicateCoroutinesApi::class)
        suspend fun deleteItem(
            itemId: Int,
            callback: (success: Boolean) -> Unit
        ) {
            return suspendCoroutine { continuation ->
                GlobalScope.launch(Dispatchers.IO) {

                    val call = taskItemApi.deleteItem(itemId = itemId, format = "json")

                    call.enqueue(object : Callback<Void> {
                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            callback.invoke(false)
                        }

                        override fun onResponse(
                            call: Call<Void>,
                            response: Response<Void>
                        ) {
                            if (response.isSuccessful) {
                                callback.invoke(true)
                            } else {
                                callback.invoke(false)
                            }
                        }
                    })
                }
            }
        }

        @OptIn(DelicateCoroutinesApi::class)
        suspend fun lookupUser(username: String): Account {
            return suspendCoroutine { continuation ->
                GlobalScope.launch(Dispatchers.IO) {
                    val call = accountApi.getAccount(username, "json")

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

        suspend fun authenticateUser(
            username: String,
            password: String,
        ): AuthResponse? = withContext(Dispatchers.IO) {
            return@withContext suspendCoroutine { continuation ->
                val call = accountApi.authenticate(username, password, "json")

                call.enqueue(object : Callback<AuthResponse> {
                    override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
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
            return suspendCoroutine { continuation ->

                val dateFormatZulu: DateTimeFormatter =
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
                    val call = taskListApi.createList(obj)

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
            user_peer: String? = null,
            attachment_img_path: String?,
            snooze_until: String?,
            completed: Boolean,
            due_at: String?,
            lati: Double? = null,
            longi: Double? = null,
            is_sub_task: Boolean,
            parent_task: Int?,
            filters: Collection<TagValuePair>?,
        ): TaskItem {
            return suspendCoroutine { continuation ->
                GlobalScope.launch(Dispatchers.IO) {
                    val obj = TaskItem(
                        item_id = null,
                        list = list,
                        owner = owner,
                        title = title,
                        body_text = body_text,
                        remind_method = remind_method,
                        user_peer = user_peer,
                        attachment_img_path = attachment_img_path,
                        is_sub_task = is_sub_task,
                        parent_task = parent_task,
                        completed = completed,
                        snooze_until = snooze_until,
                        due_at = due_at,
                        lati = lati,
                        longi = longi,
                        filters = TaskItem.convertFiltersToMap(filters)
                    )
                    val call = taskItemApi.createItem(obj)

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
            return@withContext suspendCoroutine { continuation ->
                GlobalScope.launch(Dispatchers.IO) {

                    val call = taskItemApi.getListItems(listId, "json")

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

        suspend fun getFiltersForUser(username: String): List<String>? =
            withContext(Dispatchers.IO) {
                return@withContext suspendCoroutine { continuation ->
                    val call = taskItemApi.getFiltersForUser(username, "json")

                    call.enqueue(object : Callback<List<Map<String, String>>?> {
                        override fun onFailure(
                            call: Call<List<Map<String, String>>?>,
                            t: Throwable
                        ) {
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
                                } else {
                                    continuation.resume(null)
                                }
                            } else {
                                continuation.resume(null)
                            }
                        }
                    })
                }
            }

        suspend fun getFiltersForItem(itemId: Int): List<String>? =
            withContext(Dispatchers.IO) {
                return@withContext suspendCoroutine { continuation ->
                    val call = taskItemApi.getFiltersForItem(itemId, "json")

                    call.enqueue(object : Callback<List<Map<String, String>>?> {
                        override fun onFailure(
                            call: Call<List<Map<String, String>>?>,
                            t: Throwable
                        ) {
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
                                } else {
                                    continuation.resume(null)
                                }
                            } else {
                                continuation.resume(null)
                            }
                        }
                    })
                }
            }

        suspend fun addLog(lati: Double, longi: Double, notes: String): Log1? =
            withContext(Dispatchers.IO) {
                return@withContext suspendCoroutine { continuation ->
                    val df = DecimalFormat("###.#########")
                    df.roundingMode = RoundingMode.HALF_UP

                    val call = logApi.sendLog(
                        Log1(
                            df.format(lati).toDouble(),
                            df.format(longi).toDouble(),
                            notes
                        )
                    )

                    call.enqueue(object : Callback<Log1?> {
                        override fun onFailure(call: Call<Log1?>, t: Throwable) {

                            continuation.resume(null)
                        }

                        override fun onResponse(
                            call: Call<Log1?>,
                            response: Response<Log1?>
                        ) {
                            if (response.isSuccessful) {
                                continuation.resume(response.body())
                            } else {
                                continuation.resume(null)
                            }
                        }
                    })
                }
            }

        suspend fun updateLocation(account: AccountPartialForLocation) =
            withContext(Dispatchers.IO) {
                return@withContext suspendCoroutine { continuation ->
                    val call = accountApi.updateAccountLocation(account.username, account)

                    val df = DecimalFormat("###.########")
                    df.roundingMode = RoundingMode.HALF_UP

                    account.lati = df.format(account.lati!!).toDouble()
                    account.longi = df.format(account.longi!!).toDouble()

                    call.enqueue(object : Callback<AccountPartialForLocation> {
                        override fun onFailure(
                            call: Call<AccountPartialForLocation>,
                            t: Throwable
                        ) {
                            continuation.resume(null)
                        }

                        override fun onResponse(
                            call: Call<AccountPartialForLocation>,
                            response: Response<AccountPartialForLocation>
                        ) {
                            if (response.isSuccessful) {
                                continuation.resume(response.body())
                            } else {
                                continuation.resume(null)
                            }
                        }
                    })
                }
            }

        suspend fun addClRequest(clRequest: CollabReq) =
            withContext(Dispatchers.IO) {
                return@withContext suspendCoroutine { continuation ->
                    val call = collabApi.addRequest(clRequest)
                    call.enqueue(object : Callback<CollabReq> {
                        override fun onFailure(call: Call<CollabReq>, t: Throwable) {
                            continuation.resume(null)
                        }

                        override fun onResponse(
                            call: Call<CollabReq>,
                            response: Response<CollabReq>
                        ) {
                            if (response.isSuccessful) {
                                continuation.resume(response.body())
                            } else {
                                continuation.resume(null)
                            }
                        }
                    })
                }
            }

        suspend fun deleteClRequest(requestId: Int) =
            withContext(Dispatchers.IO) {
                return@withContext suspendCoroutine { continuation ->
                    val call = collabApi.deleteRequest(requestId)
                    call.enqueue(object : Callback<CollabReq> {
                        override fun onFailure(call: Call<CollabReq>, t: Throwable) {
                            continuation.resume(null)
                        }

                        override fun onResponse(
                            call: Call<CollabReq>,
                            response: Response<CollabReq>
                        ) {
                            if (response.isSuccessful) {
                                continuation.resume(response.body())
                            } else {
                                continuation.resume(null)
                            }
                        }
                    })
                }
            }

        suspend fun getSentRequests(usernameSender: String) =
            withContext(Dispatchers.IO) {
                return@withContext suspendCoroutine { continuation ->
                    val call = collabApi.getSentRequests(usernameSender)
                    call.enqueue(object : Callback<List<CollabReq>?> {
                        override fun onFailure(call: Call<List<CollabReq>?>, t: Throwable) {
                            continuation.resume(null)
                        }

                        override fun onResponse(
                            call: Call<List<CollabReq>?>,
                            response: Response<List<CollabReq>?>
                        ) {
                            if (response.isSuccessful) {
                                continuation.resume(response.body())
                            } else {
                                continuation.resume(null)
                            }
                        }
                    })
                }
            }

        suspend fun getReceivedRequests(usernameRecipient: String) =
            withContext(Dispatchers.IO) {
                return@withContext suspendCoroutine { continuation ->
                    val call = collabApi.getReceivedRequests(usernameRecipient)
                    call.enqueue(object : Callback<List<CollabReq>?> {
                        override fun onFailure(call: Call<List<CollabReq>?>, t: Throwable) {
                            continuation.resume(null)
                        }

                        override fun onResponse(
                            call: Call<List<CollabReq>?>,
                            response: Response<List<CollabReq>?>
                        ) {
                            if (response.isSuccessful) {
                                continuation.resume(response.body())
                            } else {
                                continuation.resume(null)
                            }
                        }
                    })
                }
            }

        suspend fun addCollab(clRequest: Collab) =
            withContext(Dispatchers.IO) {
                return@withContext suspendCoroutine { continuation ->
                    val call = collabApi.addCollab(clRequest)
                    call.enqueue(object : Callback<Collab> {
                        override fun onFailure(call: Call<Collab>, t: Throwable) {
                            continuation.resume(null)
                        }

                        override fun onResponse(call: Call<Collab>, response: Response<Collab>) {
                            if (response.isSuccessful) {
                                continuation.resume(response.body())
                            } else {
                                continuation.resume(null)
                            }
                        }
                    })
                }
            }

        suspend fun deleteCollab(requestId: Int) =
            withContext(Dispatchers.IO) {
                return@withContext suspendCoroutine { continuation ->
                    val call = collabApi.deleteCollab(requestId)
                    call.enqueue(object : Callback<Collab> {
                        override fun onFailure(call: Call<Collab>, t: Throwable) {
                            continuation.resume(null)
                        }

                        override fun onResponse(call: Call<Collab>, response: Response<Collab>) {
                            if (response.isSuccessful) {
                                continuation.resume(response.body())
                            } else {
                                continuation.resume(null)
                            }
                        }
                    })
                }
            }

        suspend fun getCollabs(usernameSender: String) =
            withContext(Dispatchers.IO) {
                return@withContext suspendCoroutine { continuation ->
                    val call = collabApi.getCollabsForUser(usernameSender)
                    call.enqueue(object : Callback<List<Collab>?> {
                        override fun onFailure(call: Call<List<Collab>?>, t: Throwable) {
                            continuation.resume(null)
                        }

                        override fun onResponse(
                            call: Call<List<Collab>?>,
                            response: Response<List<Collab>?>
                        ) {
                            if (response.isSuccessful) {
                                continuation.resume(response.body())
                            } else {
                                continuation.resume(null)
                            }
                        }
                    })
                }
            }

        suspend fun rejectCollabReq(collabReq: CollabReq) =
            withContext(Dispatchers.IO) {
                return@withContext suspendCoroutine { continuation ->
                    val call = collabApi.deleteRequest(collabReq.request_id!!)
                    call.enqueue(object : Callback<CollabReq> {
                        override fun onFailure(call: Call<CollabReq>, t: Throwable) {
                            continuation.resume(null)
                        }

                        override fun onResponse(
                            call: Call<CollabReq>,
                            response: Response<CollabReq>
                        ) {
                            if (response.isSuccessful) {
                                continuation.resume(response.body())
                            } else {
                                continuation.resume(null)
                            }
                        }
                    })
                }
            }

        suspend fun acceptCollabReq(collabReq: CollabReq) =
            withContext(Dispatchers.IO) {
                return@withContext suspendCoroutine { continuation ->
                    val deleteCall = collabApi.deleteRequest(collabReq.request_id!!)
                    val addCall = collabApi.addCollab(
                        Collab(
                            collab_id = null,
                            user_master = collabReq.user_sender,
                            user_peer = collabReq.user_recipient
                        )
                    )

                    deleteCall.enqueue(object : Callback<CollabReq> {
                        override fun onFailure(call: Call<CollabReq>, t: Throwable) {}
                        override fun onResponse(
                            call: Call<CollabReq>,
                            response: Response<CollabReq>
                        ) {
                        }
                    })
                    addCall.enqueue(object : Callback<Collab> {
                        override fun onFailure(call: Call<Collab>, t: Throwable) {
                            continuation.resume(null)
                        }

                        override fun onResponse(call: Call<Collab>, response: Response<Collab>) {
                            if (response.isSuccessful) {
                                continuation.resume(response.body())
                            } else {
                                continuation.resume(null)
                            }
                        }
                    })
                }
            }

        suspend fun removeCollab(collab: Collab) =
            withContext(Dispatchers.IO) {
                return@withContext suspendCoroutine { continuation ->
                    val call = collabApi.deleteCollab(collab.collab_id!!)
                    call.enqueue(object : Callback<Collab> {
                        override fun onFailure(call: Call<Collab>, t: Throwable) {
                            continuation.resume(null)
                        }

                        override fun onResponse(call: Call<Collab>, response: Response<Collab>) {
                            if (response.isSuccessful) {
                                continuation.resume(response.body())
                            } else {
                                continuation.resume(null)
                            }
                        }
                    })
                }
            }

    }
}