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
import com.bgnw.locationreminder.api.AccountDeviceTools
import com.bgnw.locationreminder.api.AuthResponse
import com.bgnw.locationreminder.api.Requests
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AccountFragment : Fragment() {

    private val viewModel: ApplicationState by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        val wrapperCreate =
            getView()?.findViewById<LinearLayout>(R.id.create_account_dialog_wrapper)
        val wrapperLogin = getView()?.findViewById<LinearLayout>(R.id.login_dialog_wrapper)

        val loginResultText = getView()?.findViewById<TextView>(R.id.login_result_text)

        val blue = ContextCompat.getColor(requireContext(), R.color.blue)
        val grey = ContextCompat.getColor(requireContext(), R.color.grey)
        toggleCreate?.backgroundTintList = ColorStateList.valueOf(grey)
        toggleLogin?.backgroundTintList = ColorStateList.valueOf(blue)

        toggleCreate?.setOnClickListener {
            toggleCreate.backgroundTintList = ColorStateList.valueOf(blue)
            toggleLogin?.backgroundTintList = ColorStateList.valueOf(grey)
            wrapperCreate?.visibility = View.VISIBLE
            wrapperLogin?.visibility = View.GONE
        }

        toggleLogin?.setOnClickListener {
            toggleCreate?.backgroundTintList = ColorStateList.valueOf(grey)
            toggleLogin.backgroundTintList = ColorStateList.valueOf(blue)
            wrapperCreate?.visibility = View.GONE
            wrapperLogin?.visibility = View.VISIBLE
        }


        viewModel.loggedInUsername.observe(viewLifecycleOwner, Observer { username ->
            if (username != null) {
                AccountDeviceTools.saveUsername(
                    context = requireContext(),
                    username = username,
                )
            }
        })

        Log.d("bgnw", "in accountsfr ${viewModel.loggedInUsername.value}")

        viewModel.loggedInDisplayName.observe(viewLifecycleOwner, Observer { displayName ->
            if (displayName != null) {
                AccountDeviceTools.saveDisplayName(
                    context = requireContext(),
                    displayName = displayName,
                )
            }
        })

        fun clearLoginResult() {
            loginResultText?.text = ""
        }

        loginUsername?.setOnFocusChangeListener { _, _ -> clearLoginResult() }
        loginPassword?.setOnFocusChangeListener { _, _ -> clearLoginResult() }
        toggleLogin?.setOnFocusChangeListener { _, _ -> clearLoginResult() }

        loginSubmitButton?.setOnClickListener {

            clearLoginResult()

            val username: String = loginUsername?.text.toString()
            val password: String = loginPassword?.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                loginResultText?.text = "Error: You could not be authenticated."
                return@setOnClickListener
            }

            GlobalScope.launch(Dispatchers.Main) {
                try {
                    val authSuccess: AuthResponse? = Requests.authenticateUser(username, password)
                    if (authSuccess?.authentication_success != null
                        && authSuccess.authentication_success
                    ) {
                        loginResultText?.text = "Success: you're now logged in as $username."
                        val accountData = Requests.lookupUser(username)
                        viewModel.loggedInUsername.value = accountData.username
                        viewModel.loggedInDisplayName.value = accountData.display_name
                    } else {
                        loginResultText?.text = "Error: You could not be authenticated."
                    }


                } catch (e: Exception) {
                    throw e // TEMP
                    // Handle exception
                }
            }
        }
    }
}