package com.bgnw.locationreminder.frag

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.bgnw.locationreminder.ApplicationState
import com.bgnw.locationreminder.R
import com.bgnw.locationreminder.api.Requests
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AccountFragment : Fragment() {

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

        val toggleCreate = getView()?.findViewById<Button>(R.id.account_create_mode_toggle)
        val toggleLogin = getView()?.findViewById<Button>(R.id.account_login_mode_toggle)
        val wrapperCreate = getView()?.findViewById<LinearLayout>(R.id.create_account_dialog_wrapper)
        val wrapperLogin = getView()?.findViewById<LinearLayout>(R.id.login_dialog_wrapper)

        val blue = ContextCompat.getColor(requireContext(), R.color.blue)
        val grey = ContextCompat.getColor(requireContext(), R.color.grey)
        toggleCreate?.backgroundTintList = ColorStateList.valueOf(grey)
        toggleLogin?.backgroundTintList = ColorStateList.valueOf(blue)

        toggleCreate?.setOnClickListener {
            toggleCreate?.backgroundTintList = ColorStateList.valueOf(blue)
            toggleLogin?.backgroundTintList = ColorStateList.valueOf(grey)
            wrapperCreate?.visibility = View.VISIBLE
            wrapperLogin?.visibility = View.GONE
        }

        toggleLogin?.setOnClickListener {
            toggleCreate?.backgroundTintList = ColorStateList.valueOf(grey)
            toggleLogin?.backgroundTintList = ColorStateList.valueOf(blue)
            wrapperCreate?.visibility = View.GONE
            wrapperLogin?.visibility = View.VISIBLE
        }



        viewModel.loggedInUsername.observe(viewLifecycleOwner, Observer { username ->
            Log.d("OBSERVER", "(in frag) username changed to $username")
            devtextview?.text = username
        })

        loginSubmitButton?.setOnClickListener {
            val username: String = loginUsername?.text.toString()
            if (username.isEmpty()) {
                return@setOnClickListener
            }

            GlobalScope.launch(Dispatchers.Main) {
                try {
                    val account = Requests.lookupUser(username, devtextview)
                    viewModel.loggedInUsername.value = account.username
                    viewModel.loggedInDisplayName.value =
                        account.display_name // TEMP change to display name

                } catch (e: Exception) {
                    // Handle exception
                    Log.e("DJA API", "Error: $e \n${e.printStackTrace()}")
                }
            }
        }
    }

    private fun showLogin(){

    }
}