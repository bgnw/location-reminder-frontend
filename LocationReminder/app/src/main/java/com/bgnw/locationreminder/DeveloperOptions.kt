package com.bgnw.locationreminder

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.Volley
import com.bgnw.locationreminder.api.GsonRequest
import com.bgnw.locationreminder.api.SampleError
import com.bgnw.locationreminder.api.SampleResponse
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset

class DeveloperOptions : Fragment() {

    private var notifButton: Button? = null
    private var reqButton: Button? = null

    private var reqQueue: RequestQueue? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_developer_options, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        notifButton = getView()?.findViewById(R.id.button_DEV_test_notif)
        notifButton?.setOnClickListener {
            Log.d("NOTIF", "notif btn pressed")
            showNotification("Test", "Hello world")
        }

        reqQueue = Volley.newRequestQueue(context)

        reqButton = getView()?.findViewById(R.id.button_DEV_test_request)
        reqButton?.setOnClickListener {
            Log.d("BGNW-req", "req btn pressed")
            sendRq()
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

    private fun sendRq() {
        val payload = JSONObject()
        payload.put("id", 2)
        val request = GsonRequest(

            url = "http://13.51.162.189/testapi",
            clazz = SampleResponse::class.java,
            method = Request.Method.GET,

            listener = {
                Log.i(
                    "BGNW-SENDRQ",
                    "request : $it.",
                )
//                onSuccess()
            },
            errorListener = {
                val response = it.networkResponse
                try {
                    val errorJson = String(
                        response?.data ?: byteArrayOf(),
                        Charset.forName(HttpHeaderParser.parseCharset(response.headers))
                    )
                    val errorObj = Gson().fromJson(errorJson, SampleError::class.java)
                    Log.i(
                        "BGNW-SENDRQ",
                        "request : ${errorObj.error}",
                    )
//                    onError()
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                } catch (e: JsonSyntaxException) {
                    e.printStackTrace()
                }
            },
            headers = null,
            jsonPayload = null
        )

        reqQueue?.add(request)
    }

//    private fun sendRq(){
//
//    }

}