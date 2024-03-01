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
import com.bgnw.locationreminder.api.TaskList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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
            Log.d("NOTIF", "notif btn pressed")
            tvOutput?.text = "notif btn pressed"
            showNotification("Test", "Hello world")
        }

        reqQueue = Volley.newRequestQueue(context)

        reqButton = getView()?.findViewById(R.id.button_DEV_test_request)
        reqButton?.setOnClickListener {
            Log.d("BGNW-req", "req btn pressed")
            tvOutput?.text = "req btn pressed"
//            sendRq()
//            initialiseApi()
            launch {
                getListItemsTest()
            }

            Log.d("BGNW-req", "functions done.")
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
//            call.enqueue(object : Callback<Account> {
//                override fun onFailure(call: Call<Account>, t: Throwable) {
//                    Log.d("DJA API", "ERROR: $t")
//                    tvOutput?.text = "ERROR: $t"
//                }
//
//                override fun onResponse(call: Call<Account>, response: Response<Account>) {
//                    Log.d("DJA API", "RESPONSE: ${response.body().toString()}")
//                    tvOutput?.text = "RESPONSE: ${response.body().toString()}"
//                }
//
//            })
//        }

//        *********************** CREATE ACCOUNT sim **************************
//        GlobalScope.launch(Dispatchers.IO) {
//            var call = accountApi.createAccount( com.bgnw.locationreminder.api.Account(
//                username = "sim",
//                display_name = "simran",
//                password = "pass",
//                biography = "hello",
//                profile_img_path = "none"
//            ))
//
//            call.enqueue(object : Callback<Account> {
//                override fun onFailure(call: Call<Account>, t: Throwable) {
//                    Log.d("DJA API", "ERROR: $t")
//                    tvOutput?.text = "ERROR: $t"
//                }
//
//                override fun onResponse(call: Call<Account>, response: Response<Account>) {
//                    Log.d("DJA API", "RESPONSE: ${response.body().toString()}")
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
//            Log.d("DJA API", obj.toString())
//            var call = tasklApi.createList(obj)
//
//            call.enqueue(object : Callback<TaskList> {
//                override fun onFailure(call: Call<TaskList>, t: Throwable) {
//                    Log.d("DJA API", "ERROR: $t")
//                    tvOutput?.text = "ERROR: $t"
//                }
//
//                override fun onResponse(call: Call<TaskList>, response: Response<TaskList>) {
//                    Log.d("DJA API", "RESPONSE: ${response.body().toString()}")
//                    tvOutput?.text = "RESPONSE: ${response.body().toString()}"
//                }
//
//            })
//        }


        GlobalScope.launch(Dispatchers.IO) {

            var call = tasklApi.getList(5, "json")

            call.enqueue(object : Callback<TaskList> {
                override fun onFailure(call: Call<TaskList>, t: Throwable) {
                    Log.d("DJA API", "ERROR: $t")
                    tvOutput?.text = "ERROR: $t"
                }

                override fun onResponse(
                    call: Call<TaskList>,
                    response: Response<TaskList>
                ) {
                    Log.d("DJA API", "RESPONSE: ${response.body().toString()}")
                    tvOutput?.text = "RESPONSE: ${response.body().toString()}"
                }

            })
        }


    }


    private suspend fun getListItemsTest() {
        Requests.initialiseApi()
        Requests.getListItemsById(3, tvOutput)
    }
}
