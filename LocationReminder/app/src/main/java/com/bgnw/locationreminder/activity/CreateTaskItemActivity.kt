package com.bgnw.locationreminder.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.bgnw.locationreminder.ApplicationState
import com.bgnw.locationreminder.MainActivity
import com.bgnw.locationreminder.R
import com.bgnw.locationreminder.api.Requests
import com.bgnw.locationreminder.data.TaskItem
import com.bgnw.locationreminder.taginfo_api.TagInfoElement
import com.bgnw.locationreminder.taginfo_api.TagInfoResponse
import com.bgnw.locationreminder.taginfo_api.procGetSuggestionsFromKeyword
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


class CreateTaskItemActivity : AppCompatActivity() {

    private val viewModel: ApplicationState by viewModels()

    class CategoryAdapter(
        private val context: Activity,
        private val elements: List<TagInfoElement>
    ) : ArrayAdapter<TagInfoElement>(context, R.layout.list_categories_checkbox, elements) {

        private val vowels = listOf('a', 'e', 'i', 'o', 'u')
        private val selectedCategories: MutableList<TagInfoElement> = mutableListOf()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val inflater: LayoutInflater = LayoutInflater.from(context)
            val view: View = inflater.inflate(R.layout.list_categories_checkbox, null)

            val currentElement = elements[position]

            val checkBox: CheckBox = view.findViewById(R.id.lcc_checkbox)
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


            checkBox.setOnCheckedChangeListener{_, isChecked ->
                if (isChecked) { selectedCategories.add(currentElement) }
                else { selectedCategories.remove(currentElement) }
            }


            return view
        }

        fun getSelectedCategories(): MutableList<TagInfoElement> { return selectedCategories }

    }

    private val keysOI = listOf(
        "amenity", "shop", "place", "leisure", "education", "tourism",
        "public_transport", "building", "sport", "product", "vending", "cuisine"
    )


    private fun closeActivityWithResult(item: TaskItem){
        val resultIntent = Intent()
        resultIntent.putExtra("NEW_ITEM", item)
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_task_item)


        val listID = intent.getIntExtra("listID", -1)
        val username = intent.getStringExtra("username")

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
        var catListAdapter: CategoryAdapter? = null
        val keywordSearchBtn: Button? = findViewById(R.id.cti_keyword_search_btn)
        val keywordBox: EditText? = findViewById(R.id.cti_category_search_keyword_edittext)
        keywordSearchBtn?.setOnClickListener {
            if (keywordBox == null || keywordBox.text.isEmpty()) {
                return@setOnClickListener
            }

            var outputTextView: TextView? = findViewById(R.id.cti_reminder_type_extra)
            var res: TagInfoResponse? = null


            val resTask = GlobalScope.async {
                procGetSuggestionsFromKeyword(keywordBox.text.toString())
            }

            fun displayError() {
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

                res!!.data = res!!.data.filter { el ->
                    (el.count_all > 1000) and (el.key in keysOI)
                }

                if (res!!.data.isEmpty()) {
                    outputTextView?.text =
                        "No results found for this keyword, please try similar terms."
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

                catListAdapter = CategoryAdapter(this@CreateTaskItemActivity, res!!.data)
                catListView?.adapter = catListAdapter
            }
        }



        val submitBtn: Button? = findViewById(R.id.cti_create_task_btn)
        submitBtn?.setOnClickListener{
            val title = findViewById<EditText>(R.id.cti_task_name).text.toString().trim()
            val body = findViewById<EditText>(R.id.cti_body_text).text.toString().trim()
            val categories = catListAdapter?.getSelectedCategories()

            if (
                username.isNullOrBlank()
                || title.isBlank()
                || categories.isNullOrEmpty()
            ) {
                // TODO show user a message
                return@setOnClickListener
            }

            val categoriesKV = mutableListOf<String>()
            categories.forEach { el ->
                categoriesKV.add("\'${el.key}\'=\'${el.value}\'")
            }
//
//            val reqIsDone: MutableLiveData<Boolean> = MutableLiveData(false)
//            val newItem: MutableLiveData<TaskItem> = MutableLiveData()
////
//            reqIsDone.observe(this) {
//                Log.d("bgnw", "donEEEE!!!!!!!!!!")
////                viewModel.changeNeeded.postValue(true)
//            }
//
//            newItem.observe(this) { newItem ->
//                if (newItem != null) {
//                    Log.d("bgnw", "newlist is not null")
////                    viewModel.lists.value?.get(listID)?.items?.add(newItem)
//                } else {
//                    Log.d("bgnw", "newlist IS null")
//
//                }
//            }

            val reqResult = CoroutineScope(Dispatchers.IO).async {
                Requests.createItem(
                    list = listID,
                    owner = username!!,
                    title = title,
                    body_text = body, //(body.ifBlank { null }),
                    remind_method = "category", // TODO don't hardcode
                    poi_filters = categoriesKV.toString(),
                    attachment_img_path = null,
                    snooze_until = null,
                    completed = false,
                    due_at = null,
                    is_sub_task = false,
                    parent_task = null
                )
            }

            CoroutineScope(Dispatchers.IO).launch {
                val itemResult = reqResult.await()

                // Process the result
                Log.d("bgnw", "Async operation result: $itemResult")

                closeActivityWithResult(itemResult)
//                newItem.postValue(itemResult)
//                reqIsDone.postValue(true)
            }
        }
    }
}
