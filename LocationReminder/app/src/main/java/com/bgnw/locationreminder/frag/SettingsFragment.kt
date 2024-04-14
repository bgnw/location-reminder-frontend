package com.bgnw.locationreminder.frag

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Switch
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bgnw.locationreminder.ApplicationState
import com.bgnw.locationreminder.R
import com.bgnw.locationreminder.api.AccountDeviceTools.Factory.saveDebug
import com.bgnw.locationreminder.api.AccountDeviceTools.Factory.saveRemindRadius
import com.bgnw.locationreminder.api.AccountDeviceTools.Factory.saveUpdateFreq

class SettingsFragment : Fragment() {

    private val viewModel: ApplicationState by activityViewModels()
    private lateinit var switchDebug: Switch
    private lateinit var boxUpdateFreq: EditText
    private lateinit var boxRemindRadius: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_settings, container, false)

        switchDebug = view.findViewById(R.id.set_enable_logging_switch)
        boxUpdateFreq = view.findViewById(R.id.set_update_frequency_box)
        boxRemindRadius = view.findViewById(R.id.set_radius_alert_box)

        switchDebug.isChecked = (viewModel.enableDebug.value == true)
        boxUpdateFreq.text = Editable.Factory.getInstance().newEditable(
            if (viewModel.updateFrequency.value != null)
                viewModel.updateFrequency.value.toString()
            else
                "30"
        )
        boxRemindRadius.text = Editable.Factory.getInstance().newEditable(
            if (viewModel.remindRadius.value != null)
                viewModel.remindRadius.value.toString()
            else
                "20"
        )

        switchDebug.setOnCheckedChangeListener { button, isOn ->
            viewModel.enableDebug.postValue(isOn)
            saveDebug(requireContext(), isOn)
        }
        boxUpdateFreq.setOnFocusChangeListener { view, isFocused ->
            if (!isFocused) {
                try {
                    val newFreq = boxUpdateFreq.text.toString().toInt()
                    viewModel.updateFrequency.postValue(newFreq)
                    saveUpdateFreq(requireContext(), newFreq)
                } catch (_: Exception) {
                    return@setOnFocusChangeListener
                }
            }
        }
        boxRemindRadius.setOnFocusChangeListener { view, isFocused ->
            if (!isFocused) {
                try {
                    val newRadius = boxRemindRadius.text.toString().toInt()
                    viewModel.remindRadius.postValue(newRadius)
                    saveRemindRadius(requireContext(), newRadius)
                } catch (_: Exception) {
                    return@setOnFocusChangeListener
                }
            }
        }

        return view
    }
}