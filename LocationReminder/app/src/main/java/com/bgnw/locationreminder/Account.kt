package com.bgnw.locationreminder

import AccountApi
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.bgnw.locationreminder.api.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class Account : Fragment() {

    private val viewModel: ApplicationState by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Requests.initialiseApi()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_account, container, false)
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val devtextview = getView()?.findViewById<TextView>(R.id.account_temp_text)
        val loginUsername = getView()?.findViewById<EditText>(R.id.account_login_username)
        val loginPassword = getView()?.findViewById<EditText>(R.id.account_login_password)
        val loginSubmitButton = getView()?.findViewById<Button>(R.id.account_login_submit)

        viewModel.loggedInUsername.observe(viewLifecycleOwner, Observer {
                username ->
            Log.d("OBSERVER", "(in frag) username changed to $username");
            devtextview?.text = username
        })

        loginSubmitButton?.setOnClickListener{
            val username: String = loginUsername?.text.toString()
            if (username.isEmpty()) { return@setOnClickListener }

            GlobalScope.launch(Dispatchers.Main) {
                try {
                    val account = Requests.lookupUser(username, devtextview)
                    viewModel.loggedInUsername.value = account.username
                    viewModel.loggedInDisplayName.value = account.display_name // TEMP change to display name

                } catch (e: Exception) {
                    // Handle exception
                    Log.e("DJA API", "Error: $e \n${e.printStackTrace()}")
                }
            }
        }
    }
}