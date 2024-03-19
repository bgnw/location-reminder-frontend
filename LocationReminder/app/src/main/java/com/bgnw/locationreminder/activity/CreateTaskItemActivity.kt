package com.bgnw.locationreminder.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
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

    private val tags = listOf(
        mapOf("tag" to "amenity", "values" to listOf("parking", "parking_space", "bench", "place_of_worship", "restaurant", "school", "waste_basket", "bicycle_parking", "fast_food", "cafe", "fuel", "shelter", "recycling", "toilets", "bank", "pharmacy", "post_box", "kindergarten", "drinking_water")),
        mapOf("tag" to "shop", "values" to listOf("convenience", "supermarket", "clothes", "hairdresser", "car_repair", "bakery", "car", "beauty", "kiosk", "mobile_phone", "hardware", "butcher", "furniture", "car_parts", "alcohol", "florist", "electronics", "variety_store", "shoes", "mall", "optician", "jewelry", "doityourself", "gift")),
        mapOf("tag" to "leisure", "values" to listOf("pitch", "swimming_pool", "park", "garden", "playground", "picnic_table", "sports_centre", "nature_reserve", "track")),
        mapOf("tag" to "education", "values" to listOf("kindergarten", "school", "facultative_school", "centre", "courses", "college", "music", "university", "coaching")),
        mapOf("tag" to "tourism", "values" to listOf("information", "hotel", "artwork", "attraction", "viewpoint", "guest_house", "picnic_site", "camp_site", "museum", "chalet", "camp_pitch", "apartment", "hostel", "motel", "caravan_site")),
//        mapOf("tag" to "public_transport", "values" to listOf("platform", "stop_position", "stop_area", "station")),
        mapOf("tag" to "sport", "values" to listOf("soccer", "tennis", "basketball", "baseball", "multi", "swimming", "equestrian", "golf", "fitness", "running", "athletics", "table_tennis", "beachvolleyball", "climbing", "volleyball", "boules")),
        mapOf("tag" to "product", "values" to listOf("food", "charcoal", "oil", "bricks", "wine", "fuel", "beer", "gas")),
        mapOf("tag" to "vending", "values" to listOf("parking_tickets", "excrement_bags", "drinks", "public_transport_tickets", "cigarettes", "fuel", "sweets", "newspapers", "food", "coffee", "condoms", "water")),
        mapOf("tag" to "cuisine", "values" to listOf("pizza", "burger", "coffee_shop", "chinese", "italian", "sandwich", "chicken", "mexican", "japanese", "american", "kebab", "indian", "asian", "sushi", "thai", "french", "ice_cream", "seafood", "greek", "german")),
        mapOf("tag" to "landuse", "values" to listOf("vineyard", "cemetery", "commercial")),
        mapOf("tag" to "healthcare", "values" to listOf("pharmacy", "doctor", "hospital", "clinic", "dentist", "centre", "physiotherapist", "laboratory", "alternative")),
        mapOf("tag" to "place_of_worship", "values" to listOf("wayside_chapel", "chapel", "musalla", "holy_well", "mosque", "cross", "lourdes_grotto", "church", "shrine", "monastery", "husayniyyah", "mission_station", "wayside_shrine", "temple", "cemetery_chapel")),
        mapOf("tag" to "restaurant", "values" to listOf("fast_food")),
        mapOf("tag" to "beauty", "values" to listOf("nails", "tanning", "cosmetics", "spa", "skin_care", "hair", "waxing", "hair_removal"))
    )

    private fun lookupTagsAndValues(
        term: String,
        resultsDataset: MutableList<TagValuePair>,
        adapter: CategoryAdapter
    )  {
        resultsDataset.addAll(
            tags.flatMap { tagMap ->
                val tag = tagMap["tag"] as String
                val values = tagMap["values"] as List<String>

                val matchingTags = if (tag.contains(term, ignoreCase = true)) listOf(tag) else emptyList()

                val matchingValues = values.filter { it.contains(term, ignoreCase = true) }
                    .map { tag to it }

                val out: MutableList<TagValuePair> = mutableListOf()

                matchingTags.forEach{tag -> out.add(TagValuePair(tag, null))}
                matchingValues.forEach { pair -> out.add(TagValuePair(pair.first, pair.second))  }

                out
            }.toMutableList()
        )
        adapter.notifyDataSetChanged()


        Log.d("bgnw", "RDS after local search: $resultsDataset")


        if (resultsDataset.size < 3) {

            var res: TagInfoResponse? = null

            val resTask = GlobalScope.async {
                procGetSuggestionsFromKeyword(term)
            }


            lifecycleScope.launch {
                try {
                    res = resTask.await()
                } catch (e: Exception) {
                    return@launch
                }

                if (res == null) {
                    return@launch
                }

                res!!.data = res!!.data.filter { el ->
                    (el.count_all > 1000) and (el.key in tagsOfInterest)
                }

                for (item: TagInfoElement in res!!.data) {
                    val pair = TagValuePair(item.key, item.value)

                    if (!resultsDataset.contains(pair) && resultsDataset.size < 8) {
                        resultsDataset.add(pair)
                    }
                }

                adapter.notifyDataSetChanged()

                Log.d("bgnw", "RDS after API pull: $resultsDataset")
            }
        }
    }

    data class TagValuePair(
        var tag: String,
        var value: String?
    )

    class CategoryAdapter(
        private val context: Activity,
        private val elements: List<TagValuePair>
    ) : ArrayAdapter<TagValuePair>(context, R.layout.list_categories_checkbox, elements) {

        private val vowels = listOf('a', 'e', 'i', 'o', 'u')
        private val selectedCategories: MutableList<TagValuePair> = mutableListOf()

        fun decideAOrAn(char: Char): String {
            return if (char in vowels) {
                "an"
            } else {
                "a"
            }
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val inflater: LayoutInflater = LayoutInflater.from(context)
            val view: View = inflater.inflate(R.layout.list_categories_checkbox, null)

            val currentElement = elements[position]

            val checkBox: CheckBox = view.findViewById(R.id.lcc_checkbox)
            val textView: TextView = view.findViewById(R.id.lcc_textview)


            val label = buildSpannedString {

                if (currentElement.value != null) {
                    append("Places that have ")
                    append(decideAOrAn(currentElement.tag[0]))
                    append(" ")
                    bold { append(currentElement.tag) }
                    append(" of ")
                    bold { append(currentElement.value) }
                }
                else {
                    append("Places that are/have ")
                    append(decideAOrAn(currentElement.tag[0]))
                    append(" ")
                    bold { append(currentElement.tag) }
                }
            }
            textView.text = label


            checkBox.setOnCheckedChangeListener{_, isChecked ->
                if (isChecked) { selectedCategories.add(currentElement) }
                else { selectedCategories.remove(currentElement) }
            }


            return view
        }

        fun getSelectedCategories(): MutableList<TagValuePair> { return selectedCategories }

    }

    private val tagsOfInterest = listOf(
        "amenity",
        "shop",
        "leisure",
        "education",
        "tourism",
        "public_transport",
        "building",
        "sport",
        "product",
        "vending",
        "cuisine",
        "landuse",
        "healthcare",
        "place_of_worship",
        "restaurant",
        "beauty"
    )


    private fun closeActivityWithResult(item: TaskItem){
        val resultIntent = Intent()
        resultIntent.putExtra("NEW_ITEM", item)
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    private fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
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

        val results: MutableList<TagValuePair> = mutableListOf()
        val catListAdapter: CategoryAdapter = CategoryAdapter(this@CreateTaskItemActivity, results)
        catListView?.adapter = catListAdapter

        val keywordSearchBtn: Button? = findViewById(R.id.cti_keyword_search_btn)
        val keywordBox: EditText? = findViewById(R.id.cti_category_search_keyword_edittext)
        var outputTextView: TextView? = findViewById(R.id.cti_reminder_type_extra)
        keywordSearchBtn?.setOnClickListener {

            hideKeyboard()
            if (keywordBox == null || keywordBox.text.isEmpty()) {
                return@setOnClickListener
            }


            results.clear()
            catListAdapter.notifyDataSetChanged()

            lookupTagsAndValues(keywordBox.text.toString().trim(), results, catListAdapter)


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
//            categories.forEach { el ->
//
//                if (el.value != null) {
//                    categoriesKV.add("'${el.tag}'='${el.value}'")
//                }
//                else {
//                    categoriesKV.add("'${el.tag}'")
//                    categoriesKV.add("'${el.tag}'='yes'")
//                }
//            }
//
            val newItem: MutableLiveData<TaskItem> = MutableLiveData()
////
//            reqIsDone.observe(this) {
//                Log.d("bgnw", "donEEEE!!!!!!!!!!")
////                viewModel.changeNeeded.postValue(true)
//            }
//
            newItem.observe(this) { newItem ->
                if (newItem != null) {
                    Log.d("bgnw-viewresume", "viewModel.lists.value ${viewModel.lists.value}")

                    viewModel.lists.value?.get(listID)?.items?.add(newItem)
                }
                else {
                    throw Exception("item should not be null once observer gets activated here")
                }
            }
//                else {
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
                    attachment_img_path = null,
                    snooze_until = null,
                    completed = false,
                    due_at = null,
                    is_sub_task = false,
                    parent_task = null,
                    filters = categories
                )
            }

            CoroutineScope(Dispatchers.IO).launch {
                val itemResult = reqResult.await()

                // Process the result
                Log.d("bgnw", "Async operation result: $itemResult")

                closeActivityWithResult(itemResult)
                newItem.postValue(itemResult)
//                reqIsDone.postValue(true)
            }
        }
    }
}
