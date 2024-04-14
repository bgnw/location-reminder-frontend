package com.bgnw.locationreminder.frag

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.DatePicker
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.view.iterator
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.bgnw.locationreminder.ApplicationState
import com.bgnw.locationreminder.MainActivity
import com.bgnw.locationreminder.R
import com.bgnw.locationreminder.api.Requests
import com.bgnw.locationreminder.api.TagValuePair
import com.bgnw.locationreminder.data.TaskItem
import com.bgnw.locationreminder.taginfo_api.TagInfoElement
import com.bgnw.locationreminder.taginfo_api.TagInfoResponse
import com.bgnw.locationreminder.taginfo_api.procGetSuggestionsFromKeyword
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ViewEditTaskItemFragment : Fragment() {

    private val viewModel: ApplicationState by activityViewModels()

    private var listId: Int? = null
    private var itemObj: TaskItem? = null
    private var username: String? = null

    private val dtHuman: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM 'at' HH:mm")
    private val dtZulu: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")

    private val tags = listOf(
        mapOf(
            "tag" to "amenity",
            "values" to listOf(
                "parking",
                "parking_space",
                "bench",
                "place_of_worship",
                "restaurant",
                "school",
                "waste_basket",
                "bicycle_parking",
                "fast_food",
                "cafe",
                "fuel",
                "shelter",
                "recycling",
                "toilets",
                "bank",
                "pharmacy",
                "post_box",
                "kindergarten",
                "drinking_water"
            )
        ),
        mapOf(
            "tag" to "shop",
            "values" to listOf(
                "convenience",
                "supermarket",
                "clothes",
                "hairdresser",
                "car_repair",
                "bakery",
                "car",
                "beauty",
                "kiosk",
                "mobile_phone",
                "hardware",
                "butcher",
                "furniture",
                "car_parts",
                "alcohol",
                "florist",
                "electronics",
                "variety_store",
                "shoes",
                "mall",
                "optician",
                "jewelry",
                "doityourself",
                "gift"
            )
        ),
        mapOf(
            "tag" to "leisure",
            "values" to listOf(
                "pitch",
                "swimming_pool",
                "park",
                "garden",
                "playground",
                "picnic_table",
                "sports_centre",
                "nature_reserve",
                "track"
            )
        ),
        mapOf(
            "tag" to "education",
            "values" to listOf(
                "kindergarten",
                "school",
                "facultative_school",
                "centre",
                "courses",
                "college",
                "music",
                "university",
                "coaching"
            )
        ),
        mapOf(
            "tag" to "tourism",
            "values" to listOf(
                "information",
                "hotel",
                "artwork",
                "attraction",
                "viewpoint",
                "guest_house",
                "picnic_site",
                "camp_site",
                "museum",
                "chalet",
                "camp_pitch",
                "apartment",
                "hostel",
                "motel",
                "caravan_site"
            )
        ),
        mapOf(
            "tag" to "sport",
            "values" to listOf(
                "soccer",
                "tennis",
                "basketball",
                "baseball",
                "multi",
                "swimming",
                "equestrian",
                "golf",
                "fitness",
                "running",
                "athletics",
                "table_tennis",
                "beachvolleyball",
                "climbing",
                "volleyball",
                "boules"
            )
        ),
        mapOf(
            "tag" to "product",
            "values" to listOf("food", "charcoal", "oil", "bricks", "wine", "fuel", "beer", "gas")
        ),
        mapOf(
            "tag" to "vending",
            "values" to listOf(
                "parking_tickets",
                "excrement_bags",
                "drinks",
                "public_transport_tickets",
                "cigarettes",
                "fuel",
                "sweets",
                "newspapers",
                "food",
                "coffee",
                "condoms",
                "water"
            )
        ),
        mapOf(
            "tag" to "cuisine",
            "values" to listOf(
                "pizza",
                "burger",
                "coffee_shop",
                "chinese",
                "italian",
                "sandwich",
                "chicken",
                "mexican",
                "japanese",
                "american",
                "kebab",
                "indian",
                "asian",
                "sushi",
                "thai",
                "french",
                "ice_cream",
                "seafood",
                "greek",
                "german"
            )
        ),
        mapOf("tag" to "landuse", "values" to listOf("vineyard", "cemetery", "commercial")),
        mapOf(
            "tag" to "healthcare",
            "values" to listOf(
                "pharmacy",
                "doctor",
                "hospital",
                "clinic",
                "dentist",
                "centre",
                "physiotherapist",
                "laboratory",
                "alternative"
            )
        ),
        mapOf(
            "tag" to "place_of_worship",
            "values" to listOf(
                "wayside_chapel",
                "chapel",
                "musalla",
                "holy_well",
                "mosque",
                "cross",
                "lourdes_grotto",
                "church",
                "shrine",
                "monastery",
                "husayniyyah",
                "mission_station",
                "wayside_shrine",
                "temple",
                "cemetery_chapel"
            )
        ),
        mapOf("tag" to "restaurant", "values" to listOf("fast_food")),
        mapOf(
            "tag" to "beauty",
            "values" to listOf(
                "nails",
                "tanning",
                "cosmetics",
                "spa",
                "skin_care",
                "hair",
                "waxing",
                "hair_removal"
            )
        )
    )

    private fun lookupTagsAndValues(
        term: String,
        resultsDataset: MutableList<TagValuePair>,
        adapter: CategoryAdapter
    ) {
        resultsDataset.addAll(
            tags.flatMap { tagMap ->
                val tag = tagMap["tag"] as String
                val values = tagMap["values"] as List<String>

                val matchingTags =
                    if (tag.contains(term, ignoreCase = true)) listOf(tag) else emptyList()

                val matchingValues = values.filter { it.contains(term, ignoreCase = true) }
                    .map { tag to it }

                val out: MutableList<TagValuePair> = mutableListOf()

                matchingTags.forEach { tag -> out.add(TagValuePair(tag, null)) }
                matchingValues.forEach { pair -> out.add(TagValuePair(pair.first, pair.second)) }

                out
            }.toMutableList()
        )
        adapter.notifyDataSetChanged()

        if (resultsDataset.size < 3) {

            var res: TagInfoResponse?

            val resTask = CoroutineScope(Dispatchers.IO).async {
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
            }
        }
    }

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
                } else {
                    append("Places that are/have ")
                    append(decideAOrAn(currentElement.tag[0]))
                    append(" ")
                    bold { append(currentElement.tag) }
                }
            }
            textView.text = label


            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedCategories.add(currentElement)
                } else {
                    selectedCategories.remove(currentElement)
                }
            }


            return view
        }
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

    private fun hideKeyboard() {
        if (view != null) {
            val imm =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(View(requireContext()).windowToken, 0)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_view_edit_task_item, container, false)
        val context = context as MainActivity


        listId = arguments?.getInt("LIST_ID")
        itemObj = arguments?.getParcelable("ITEM") as TaskItem?
        username = viewModel.loggedInUsername.value

        if (listId == null || itemObj == null || username == null) {
            throw Exception("None of (listId, itemObj, username) can be null.")
        }

        val titleBox = rootView.findViewById<EditText>(R.id.vti_task_name)
        val bodyBox = rootView.findViewById<EditText>(R.id.vti_body_text)
        val snoozeMsg = rootView.findViewById<TextView>(R.id.vti_snooze_status_msg)
        val dueMsg = rootView.findViewById<TextView>(R.id.vti_due_status_msg)
        val completionMsg = rootView.findViewById<TextView>(R.id.vti_completion_status_msg)

        val timePicker = rootView.findViewById<TimePicker>(R.id.vti_timepicker)
        val datePicker = rootView.findViewById<DatePicker>(R.id.vti_datepicker)


        val editTitleBtn = rootView.findViewById<Button>(R.id.vti_edit_name_btn)
        val editBodyBtn = rootView.findViewById<Button>(R.id.vti_edit_body_btn)
        val editSnoozeDate = rootView.findViewById<Button>(R.id.vti_snooze_edit_date)
        val editSnoozeTime = rootView.findViewById<Button>(R.id.vti_snooze_edit_time)
        val editDueDate = rootView.findViewById<Button>(R.id.vti_due_edit_date)
        val editDueTime = rootView.findViewById<Button>(R.id.vti_due_edit_time)
        val toggleCompleteBtn = rootView.findViewById<Button>(R.id.vti_toggle_completion_btn)
        val deleteItemBtn = rootView.findViewById<Button>(R.id.vti_delete_task_btn)

        val pickTimeDoneBtn = rootView.findViewById<Button>(R.id.vti_pick_time_done_btn)
        val pickDateDoneBtn = rootView.findViewById<Button>(R.id.vti_pick_date_done_btn)

        val loadingBg = rootView.findViewById<LinearLayout>(R.id.loading_bg)
        val loadingPopup = rootView.findViewById<LinearLayout>(R.id.loading_popup)

        val pickTimePopup = rootView.findViewById<LinearLayout>(R.id.timepicker_popup)
        val pickDatePopup = rootView.findViewById<LinearLayout>(R.id.datepicker_popup)

        fun updateTitleBox() {
            titleBox.text = Editable.Factory.getInstance().newEditable(itemObj!!.title)
        }

        fun updateBodyBox() {
            bodyBox.text = Editable.Factory.getInstance().newEditable(itemObj!!.body_text)
        }

        fun updateSnoozeMsg() {
            snoozeMsg.text =
                if (itemObj!!.snooze_until == null)
                    "Not currently snoozed"
                else
                    LocalDateTime.parse(itemObj!!.snooze_until!!, dtZulu).format(dtHuman)
        }

        fun updateDueMsg() {
            dueMsg.text =
                if (itemObj!!.due_at == null)
                    "No due date"
                else
                    LocalDateTime.parse(itemObj!!.due_at!!, dtZulu).format(dtHuman)
        }

        fun updateCompletionMsg() {
            completionMsg.text = if (itemObj!!.completed) "Completed" else "Not completed"
            toggleCompleteBtn.text =
                if (itemObj!!.completed) "Mark not complete" else "Mark complete"
        }

        updateTitleBox()
        updateBodyBox()
        updateSnoozeMsg()
        updateDueMsg()
        updateCompletionMsg()

        // hide the category layout initially
        val categoryLayout: LinearLayout? =
            rootView.findViewById(R.id.vti_category_selection_layout)
        categoryLayout?.visibility = View.GONE

        // set function to run when remind method changes
        val radioGroup: RadioGroup? = rootView.findViewById(R.id.vti_radio_group)
        radioGroup?.setOnCheckedChangeListener { group, _ ->

            categoryLayout?.visibility = View.GONE

            if (group.checkedRadioButtonId == R.id.vti_radio_opt_category) {
                categoryLayout?.visibility = View.VISIBLE
            }
        }

        // setup of category search/selection flow
        val catListView: ListView? = rootView.findViewById(R.id.vti_list_categories)

        val results: MutableList<TagValuePair> = mutableListOf()
        val catListAdapter: CategoryAdapter = CategoryAdapter(context, results)
        catListView?.adapter = catListAdapter

        val keywordSearchBtn: Button? = rootView.findViewById(R.id.vti_keyword_search_btn)
        val keywordBox: EditText? = rootView.findViewById(R.id.vti_category_search_keyword_edittext)
        var outputTextView: TextView? = rootView.findViewById(R.id.vti_reminder_type_extra)
        keywordSearchBtn?.setOnClickListener {

            hideKeyboard()
            if (keywordBox == null || keywordBox.text.isEmpty()) {
                return@setOnClickListener
            }

            results.clear()
            catListAdapter.notifyDataSetChanged()

            lookupTagsAndValues(keywordBox.text.toString().trim(), results, catListAdapter)
        }

        val interactElements: List<View> = listOf(
            titleBox as View,
            bodyBox as View,
            keywordSearchBtn as View,
            keywordBox as View,
            editTitleBtn as View,
            editBodyBtn as View,
            editSnoozeDate as View,
            editSnoozeTime as View,
            editDueDate as View,
            editDueTime as View,
            toggleCompleteBtn as View,
            radioGroup as View,
            deleteItemBtn as View
        )

        fun stateAll(enableButtonsEtc: Boolean) {
            for (el in interactElements) {
                if (el is RadioGroup) {
                    for (btn in el) {
                        btn.isEnabled = enableButtonsEtc
                    }
                } else if (el is EditText) {
                    el.isEnabled = false
                } else {
                    el.isEnabled = enableButtonsEtc
                }
            }
        }

        var activeInteraction: View? = null

        fun doUpdate(itemId: Int, item: TaskItem) {
            loadingBg.visibility = View.VISIBLE
            loadingPopup.visibility = View.VISIBLE

            CoroutineScope(Dispatchers.IO).launch {
                Requests.updateItem(
                    itemId,
                    item
                ) { success ->
                    loadingBg.visibility = View.GONE
                    loadingPopup.visibility = View.GONE
                    if (!success) {
                        // todo: revert value shown
                        Toast.makeText(
                            context,
                            "Update could not be made at this time",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            viewModel.lists.value
        }

        fun doDelete(itemId: Int, item: TaskItem) {
            loadingBg.visibility = View.VISIBLE
            loadingPopup.visibility = View.VISIBLE

            CoroutineScope(Dispatchers.IO).launch {
                Requests.deleteItem(
                    itemId,

                    ) { success ->
                    loadingBg.visibility = View.GONE
                    loadingPopup.visibility = View.GONE
                    var theItem: TaskItem? = null
                    val theLists = viewModel.lists.value

                    theLists?.forEach { list ->
                        list.items?.removeIf { item -> item.item_id == itemId }
                    }

                    viewModel.lists.postValue(theLists)

                    if (!success) {
                        Toast.makeText(
                            context,
                            "Item could not be deleted at this time",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

        }

        editTitleBtn.setOnClickListener {
            if (activeInteraction == null) {
                stateAll(enableButtonsEtc = false)
                titleBox.isEnabled = true
                editTitleBtn.isEnabled = true
                editTitleBtn.text = "Save"
                activeInteraction = editTitleBtn
            } else if (activeInteraction == editTitleBtn) {
                if (titleBox.text.isBlank()) {
                    return@setOnClickListener
                }

                itemObj!!.title = titleBox.text.toString()

                doUpdate(
                    itemObj!!.item_id!!,
                    itemObj!!
                )

                stateAll(enableButtonsEtc = true)
                editTitleBtn.text = "Edit"
                activeInteraction = null
            }
        }

        editBodyBtn.setOnClickListener {
            if (activeInteraction == null) {
                stateAll(enableButtonsEtc = false)
                bodyBox.isEnabled = true
                editBodyBtn.isEnabled = true
                editBodyBtn.text = "Save"
                activeInteraction = editBodyBtn
            } else if (activeInteraction == editBodyBtn) {
                itemObj!!.body_text = bodyBox.text.toString()

                doUpdate(
                    itemObj!!.item_id!!,
                    itemObj!!
                )

                stateAll(enableButtonsEtc = true)
                editBodyBtn.text = "Edit"
                activeInteraction = null
            }

        }

        editSnoozeDate.setOnClickListener {
            loadingBg.visibility = View.VISIBLE
            pickDatePopup.visibility = View.VISIBLE
            activeInteraction = editSnoozeDate
        }

        editSnoozeTime.setOnClickListener {
            loadingBg.visibility = View.VISIBLE
            pickTimePopup.visibility = View.VISIBLE
            activeInteraction = editSnoozeTime
        }

        editDueDate.setOnClickListener {
            loadingBg.visibility = View.VISIBLE
            pickDatePopup.visibility = View.VISIBLE
            activeInteraction = editDueDate
        }

        editDueTime.setOnClickListener {
            loadingBg.visibility = View.VISIBLE
            pickTimePopup.visibility = View.VISIBLE
            activeInteraction = editDueTime
        }

        pickDateDoneBtn.setOnClickListener {
            loadingBg.visibility = View.GONE
            pickDatePopup.visibility = View.GONE

            val currentSnoozeString = itemObj!!.snooze_until
            val currentDT =
                if (currentSnoozeString == null)
                    null
                else
                    LocalDateTime.parse(currentSnoozeString, dtZulu)
            var newDT = LocalDateTime.of(
                datePicker.year,
                datePicker.month,
                datePicker.dayOfMonth,
                currentDT?.hour ?: 0,
                currentDT?.minute ?: 0
            )

            if (activeInteraction == editSnoozeDate) {
                itemObj!!.snooze_until = newDT.format(dtZulu)
                doUpdate(
                    itemObj!!.item_id!!,
                    itemObj!!
                )
                updateSnoozeMsg()

            } else if (activeInteraction == editDueDate) {
                itemObj!!.due_at = newDT.format(dtZulu)
                doUpdate(
                    itemObj!!.item_id!!,
                    itemObj!!
                )
                updateDueMsg()
            }
            activeInteraction = null
        }

        pickTimeDoneBtn.setOnClickListener {
            loadingBg.visibility = View.GONE
            pickTimePopup.visibility = View.GONE

            val currentSnoozeString =
                when (activeInteraction) {
                    editSnoozeTime -> itemObj!!.snooze_until
                    editDueTime -> itemObj!!.due_at
                    else -> throw Exception("Illegal activeInteraction")
                }
            val currentDT =
                if (currentSnoozeString == null)
                    null
                else
                    LocalDateTime.parse(currentSnoozeString, dtZulu)
            var newDT = LocalDateTime.of(
                currentDT?.year ?: LocalDateTime.now().year,
                currentDT?.month ?: LocalDateTime.now().month,
                currentDT?.dayOfMonth ?: LocalDateTime.now().dayOfMonth,
                timePicker.hour,
                timePicker.minute
            )

            if (activeInteraction == editSnoozeTime) {
                itemObj!!.snooze_until = newDT.format(dtZulu)
                doUpdate(
                    itemObj!!.item_id!!,
                    itemObj!!
                )
                updateSnoozeMsg()

            } else if (activeInteraction == editDueTime) {
                itemObj!!.due_at = newDT.format(dtZulu)
                doUpdate(
                    itemObj!!.item_id!!,
                    itemObj!!
                )
                updateDueMsg()
            }
            activeInteraction = null
        }

        toggleCompleteBtn.setOnClickListener {
            if (activeInteraction == null) {

                stateAll(enableButtonsEtc = false)

                itemObj!!.completed = !itemObj!!.completed

                doUpdate(
                    itemObj!!.item_id!!,
                    itemObj!!
                )

                updateCompletionMsg()

                stateAll(enableButtonsEtc = true)
            }
        }

        deleteItemBtn.setOnClickListener {
            viewModel.listIdToOpen.postValue(listId)

            doDelete(
                itemObj!!.item_id!!,
                itemObj!!
            )
        }

        return rootView
    }

    // https://stackoverflow.com/questions/21504088
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as? AppCompatActivity)?.supportActionBar?.hide()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (requireActivity() as? AppCompatActivity)?.supportActionBar?.show()
    }
}