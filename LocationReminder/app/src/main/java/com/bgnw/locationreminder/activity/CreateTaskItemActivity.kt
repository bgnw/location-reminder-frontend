package com.bgnw.locationreminder.activity

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bgnw.locationreminder.ApplicationState
import com.bgnw.locationreminder.R
import com.bgnw.locationreminder.data.TaskList
import com.bgnw.locationreminder.taginfo_api.TagInfoElement
import com.bgnw.locationreminder.taginfo_api.TagInfoResponse
import com.bgnw.locationreminder.taginfo_api.procGetSuggestionsFromKeyword
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


class CreateTaskItemActivity : AppCompatActivity() {


    class CategoryAdapter(
        private val context: Activity,
        private val elements: List<TagInfoElement>
    ): ArrayAdapter<TagInfoElement>(context, R.layout.list_categories_checkbox, elements) {

        private val vowels = listOf('a','e','i','o','u')

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val inflater: LayoutInflater = LayoutInflater.from(context)
            val view: View = inflater.inflate(R.layout.list_categories_checkbox, null)

            val currentElement = elements[position]

            // val checkBox: CheckBox = view.findViewById(R.id.lcc_checkbox)
            val textView: TextView = view.findViewById(R.id.lcc_textview)


            val label = buildSpannedString {
                append("Places that have ")
                if (currentElement.key[0] in vowels) {
                    append("an ")
                } else {
                    append("a ")
                }
                bold { append(currentElement.key) }
                append(" of ")
                bold { append(currentElement.value) }
            }

            textView.text = label

            return view
        }

    }

    private val keysOI = listOf("amenity", "shop", "place", "leisure", "education", "tourism",
        "public_transport", "building", "sport", "product", "vending", "cuisine")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_task_item)


        val listID = intent.getIntExtra("listID", -1)

        // hide the category layout initially
        val categoryLayout: LinearLayout? = findViewById(R.id.cti_category_selection_layout)
        categoryLayout?.visibility = View.GONE


        // set function to run when remind method changes
        val radioGroup: RadioGroup? = findViewById(R.id.cti_radio_group)
        radioGroup?.setOnCheckedChangeListener { group, _ ->

            categoryLayout?.visibility = View.GONE

            if (group.checkedRadioButtonId == R.id.cti_radio_opt_category) {
               categoryLayout?.visibility = View.VISIBLE
            }
        }


        // setup of category search/selection flow
        val catListView: ListView? = findViewById(R.id.cti_list_categories)
        val keywordSearchBtn: Button? = findViewById(R.id.cti_keyword_search_btn)
        val keywordBox: EditText? = findViewById(R.id.cti_category_search_keyword_edittext)
        keywordSearchBtn?.setOnClickListener {
            if (keywordBox == null || keywordBox.text.isEmpty()) {
                return@setOnClickListener
            }

            var outputTextView: TextView? = findViewById(R.id.cti_reminder_type_extra)
            var res: TagInfoResponse? = null


            val resTask = GlobalScope.async {
                procGetSuggestionsFromKeyword(keywordBox!!.text.toString())
            }

            fun displayError(){
                outputTextView?.text = "Something went wrong while fetching results."
            }

            lifecycleScope.launch {


                try {
                    res = resTask.await()
                } catch (e: Exception) {
                    return@launch displayError()
                }

                if (res == null) {
                    return@launch displayError()
                }

                res!!.data = res!!.data.filter {
                        el -> (el.count_all > 1000) and (el.key in keysOI)
                }

                if (res!!.data.isEmpty()) {
                    outputTextView?.text = "No results found for this keyword, please try similar terms."
                } else {
                    val resultMsg = buildSpannedString {
                        bold { append(res!!.data.size.toString()) }
                        if (res!!.data.size > 1) {
                            append(" suggestions found:")
                        } else {
                            append(" suggestion found:")
                        }
                    }
                    outputTextView?.text = resultMsg
                }

                val catListAdapter = CategoryAdapter(this@CreateTaskItemActivity, res!!.data)
                catListView?.adapter = catListAdapter
            }
        }

    }
}
