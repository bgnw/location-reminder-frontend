package com.bgnw.locationreminder.frag

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import com.bgnw.locationreminder.ApplicationState
import com.bgnw.locationreminder.CollabListAdapter
import com.bgnw.locationreminder.CollabReqListAdapter
import com.bgnw.locationreminder.R
import com.bgnw.locationreminder.api.Requests
import com.bgnw.locationreminder.data.CollabReq
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SharingFragment : Fragment() {

    private val viewModel: ApplicationState by activityViewModels()
    private val dtZulu: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val layout = inflater.inflate(R.layout.fragment_sharing, container, false)

        val reqLv = layout.findViewById<ListView>(R.id.ui_collab_rq_lv)
        val collabLv = layout.findViewById<ListView>(R.id.ui_collabs_lv)

        val newReqBtn = layout.findViewById<Button>(R.id.new_req_btn)
        var reqAdapter: CollabReqListAdapter?
        var collabAdapter: CollabListAdapter?

        viewModel.receivedRequests.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                reqAdapter = CollabReqListAdapter(requireActivity(), data.toMutableList())
                reqLv.adapter = reqAdapter
                reqAdapter?.notifyDataSetChanged()
            }
        }

        viewModel.sentRequests.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                layout.findViewById<TextView>(R.id.friend_request_msg)?.text =
                    "You've sent ${data.size} pending friend request${if (data.size == 1) "" else "s"}."
            }
        }

        viewModel.collabs.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                collabAdapter = CollabListAdapter(requireActivity(), data.toMutableList())
                collabLv.adapter = collabAdapter
                collabAdapter?.notifyDataSetChanged()
            }
        }

        val editText = EditText(context)
        editText.hint = "Enter the username for the other user"

        val dialog = AlertDialog.Builder(context)
            .setTitle("New friend request")
            .setView(editText)

            .setPositiveButton("Send") { _, _ ->
                val recipientUser = editText.text.toString()
                val senderUser = viewModel.loggedInUsername.value
                if (recipientUser.isNotEmpty() && senderUser != null) {

                    val newRequestObj = CollabReq(
                        request_id = null,
                        datetime_sent = LocalDateTime.now().format(dtZulu),
                        user_sender = senderUser,
                        user_recipient = recipientUser
                    )

                    val returnedReq: MutableLiveData<CollabReq> = MutableLiveData()
                    CoroutineScope(Dispatchers.IO).async {
                        returnedReq.postValue(Requests.addClRequest(newRequestObj))
                        viewModel.sentRequests.postValue(Requests.getSentRequests(viewModel.loggedInUsername.value!!))
                    }

                    returnedReq.observe(viewLifecycleOwner) { data ->
                        if (data != null) {
                            newRequestObj.request_id = data.request_id
                            Toast.makeText(
                                context,
                                "Friend request sent to ${data.user_recipient}",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Toast.makeText(
                                context,
                                "Something went wrong while processing your request",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
            .setNegativeButton("Cancel") { _, _ -> }
            .create()

        newReqBtn.setOnClickListener { dialog.show() }

        return layout
    }
}