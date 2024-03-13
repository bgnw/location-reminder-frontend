package com.bgnw.locationreminder

import AccountApi
import TaskListApi
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.bgnw.locationreminder.api.Requests
import com.bgnw.locationreminder.data.TaskList
import com.bgnw.locationreminder.overpass_api.queryOverpassApi
import com.bgnw.locationreminder.taginfo_api.TagInfoResponse
import com.bgnw.locationreminder.taginfo_api.procGetSuggestionsFromKeyword
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Thread.sleep
import kotlin.coroutines.CoroutineContext


class DeveloperOptions : Fragment(), CoroutineScope {


    // coroutine boilerplate from https://stackoverflow.com/questions/53928668/
    private var job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }


    private var notifButton: Button? = null
    private var reqButton: Button? = null

    private var reqQueue: RequestQueue? = null //gson
    private var tvOutput: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_developer_options, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvOutput = getView()?.findViewById(R.id.text_DEV_output)


        notifButton = getView()?.findViewById(R.id.button_DEV_test_notif)
        notifButton?.setOnClickListener {
            Log.d("bgnw_NOTIF", "notif btn pressed")
            tvOutput?.text = "notif btn pressed"
            showNotification("Test", "Hello world")
        }

        reqQueue = Volley.newRequestQueue(context)

        reqButton = getView()?.findViewById(R.id.button_DEV_test_request)
        reqButton?.setOnClickListener {
            Log.d("bgnw_req", "req btn pressed")
            tvOutput?.text = "req btn pressed"


//            sendRq()
//            initialiseApi()


//            launch {
//                getListItemsTest()
//            }


//            val resTask = GlobalScope.async {
//                overpassPlayground()
//            }


            val keysOI = listOf(
                "amenity", "shop", "place", "leisure", "education", "tourism",
                "public_transport", "building", "sport", "product", "vending", "cuisine"
            )

            var res: TagInfoResponse? = null
            val resTask = GlobalScope.async {
                procGetSuggestionsFromKeyword("chicken")
            }
            GlobalScope.launch {
                res = resTask.await()
            }
            while (res == null) {
                sleep(1000)
            }

            res!!.data = res!!.data.filter { el ->
                (el.count_all > 1000) and (el.key in keysOI)
            }
            tvOutput?.text = res.toString()

            Log.d("bgnw_req", "functions done.")
        }
    }


    private suspend fun overpassPlayground() {
        val overpassQuery = """
        [out:json];
        (
          node(around:1000,55.95351, -3.19146)["cuisine"="asian"];
          
        );
        out geom;

    """.trimIndent()

        try {
            val response = queryOverpassApi(overpassQuery)
            Log.d("bgnw_overpass", "$response")

        } catch (e: Exception) {
            Log.d("bgnw_overpass", "Error: ${e.message}")
        }
    }

    private fun showNotification(title: String, message: String) {
        val builder = NotificationCompat.Builder(requireContext(), R.string.channel_id.toString())
            .setSmallIcon(R.drawable.baseline_info_24).setContentTitle(title)
            .setContentText(message).setPriority(NotificationCompat.PRIORITY_HIGH)

        with(NotificationManagerCompat.from(requireContext())) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0
                    )
                }
                return
            }
            notify(1, builder.build())
        }
    }


    @OptIn(DelicateCoroutinesApi::class)
    private fun initialiseApi() {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(getString(R.string.API_URL))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val accountApi: AccountApi = retrofit.create(AccountApi::class.java)
        val tasklApi: TaskListApi = retrofit.create(TaskListApi::class.java)

//        *************** LOOKUP ACCOUNT ba *************************
//        GlobalScope.launch(Dispatchers.IO) {
//            var call = accountApi.getAccount("ba", "json")
//
//            call.enqueue(object : Callback<AccountFragment> {
//                override fun onFailure(call: Call<AccountFragment>, t: Throwable) {
//                    Log.d("bgnw_DJA API", "ERROR: $t")
//                    tvOutput?.text = "ERROR: $t"
//                }
//
//                override fun onResponse(call: Call<AccountFragment>, response: Response<AccountFragment>) {
//                    Log.d("bgnw_DJA API", "RESPONSE: ${response.body().toString()}")
//                    tvOutput?.text = "RESPONSE: ${response.body().toString()}"
//                }
//
//            })
//        }

//        *********************** CREATE ACCOUNT sim **************************
//        GlobalScope.launch(Dispatchers.IO) {
//            var call = accountApi.createAccount( com.bgnw.locationreminder.api.AccountFragment(
//                username = "sim",
//                display_name = "simran",
//                password = "pass",
//                biography = "hello",
//                profile_img_path = "none"
//            ))
//
//            call.enqueue(object : Callback<AccountFragment> {
//                override fun onFailure(call: Call<AccountFragment>, t: Throwable) {
//                    Log.d("bgnw_DJA API", "ERROR: $t")
//                    tvOutput?.text = "ERROR: $t"
//                }
//
//                override fun onResponse(call: Call<AccountFragment>, response: Response<AccountFragment>) {
//                    Log.d("bgnw_DJA API", "RESPONSE: ${response.body().toString()}")
//                    tvOutput?.text = "RESPONSE: ${response.body().toString()}"
//                }
//
//            })
//        }


//        GlobalScope.launch(Dispatchers.IO) {
//            val obj = TaskList(
//                title = "test3",
//                icon_name = "none",
//                created_at = Calendar.getInstance(),
//                owner = "sim",
//                sort_by = "name",
//                visibility = 0
//            )
//            Log.d("bgnw_DJA API", obj.toString())
//            var call = tasklApi.createList(obj)
//
//            call.enqueue(object : Callback<TaskList> {
//                override fun onFailure(call: Call<TaskList>, t: Throwable) {
//                    Log.d("bgnw_DJA API", "ERROR: $t")
//                    tvOutput?.text = "ERROR: $t"
//                }
//
//                override fun onResponse(call: Call<TaskList>, response: Response<TaskList>) {
//                    Log.d("bgnw_DJA API", "RESPONSE: ${response.body().toString()}")
//                    tvOutput?.text = "RESPONSE: ${response.body().toString()}"
//                }
//
//            })
//        }


        GlobalScope.launch(Dispatchers.IO) {

            var call = tasklApi.getList(5, "json")

            call.enqueue(object : Callback<TaskList> {
                override fun onFailure(call: Call<TaskList>, t: Throwable) {
                    Log.d("bgnw_DJA API", "ERROR: $t")
                    tvOutput?.text = "ERROR: $t"
                }

                override fun onResponse(
                    call: Call<TaskList>,
                    response: Response<TaskList>
                ) {
                    Log.d("bgnw_DJA API", "RESPONSE: ${response.body().toString()}")
                    tvOutput?.text = "RESPONSE: ${response.body().toString()}"
                }

            })
        }


    }


}
