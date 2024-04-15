package com.bgnw.locationreminder.frag

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
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
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.bgnw.locationreminder.ApplicationState
import com.bgnw.locationreminder.MainActivity
import com.bgnw.locationreminder.R
import com.bgnw.locationreminder.api.Requests
import com.bgnw.locationreminder.api.TagValuePair
import com.bgnw.locationreminder.data.Collab
import com.bgnw.locationreminder.data.TaskItem
import com.bgnw.locationreminder.data.TaskList
import com.bgnw.locationreminder.taginfo_api.TagInfoElement
import com.bgnw.locationreminder.taginfo_api.TagInfoResponse
import com.bgnw.locationreminder.taginfo_api.procGetSuggestionsFromKeyword
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.config.IConfigurationProvider
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow
import java.io.File
import java.math.RoundingMode
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CreateTaskItemFragment : Fragment() {

    class CategoryAdapter(
        private val context: Activity,
        private val elements: List<TagValuePair>
    ) : ArrayAdapter<TagValuePair>(context, R.layout.list_categories_checkbox, elements) {

        private val vowels = listOf('a', 'e', 'i', 'o', 'u')
        private val selectedCategories: MutableList<TagValuePair> = mutableListOf()

        private fun decideAOrAn(char: Char): String {
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

        fun getSelectedCategories(): MutableList<TagValuePair> {
            return selectedCategories
        }

    }

    class UserAdapter(
        private val context: Activity,
        private val elements: List<String>
    ) : ArrayAdapter<String>(context, R.layout.list_categories_checkbox, elements) {

        private var selectedUsername: String? = null
        private var selectedPosition = -1

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val inflater: LayoutInflater = LayoutInflater.from(context)
            val view: View = inflater.inflate(R.layout.list_usernames_radio, null)

            val currentElement = elements[position]

            val checkBox: RadioButton = view.findViewById(R.id.lvr_checkbox)
            val textView: TextView = view.findViewById(R.id.lvr_textview)


            val label = buildSpannedString {

                if (currentElement != null) {
                    bold { append(currentElement) }
                }
            }
            textView.text = label

            checkBox.isChecked = (selectedPosition == position)
            checkBox.setOnClickListener {
                selectedPosition = position
                selectedUsername = elements[position]
                notifyDataSetChanged()
            }
            return view
        }

        fun getSelectedUsernames(): String? {
            return selectedUsername
        }
    }

    private val viewModel: ApplicationState by activityViewModels()

    private lateinit var redMarkerDrawable: BitmapDrawable
    private lateinit var userLocationMarkerDrawable: BitmapDrawable
    private lateinit var mapView: MapView
    private lateinit var username: String
    private var locationPoint: GeoPoint? = null
    private var listObj: TaskList? = null
    private var listId: Int? = null
    private val df = DecimalFormat("###.########")

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


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_create_task_item, container, false)

        df.roundingMode = RoundingMode.HALF_UP

        val context = context as MainActivity
        listId = arguments?.getInt("LIST_ID", -1)
        listObj = viewModel.lists.value?.find { list -> list.list_id == listId }
        username = viewModel.loggedInUsername.value!!

        if (listId == null || listObj == null || username == null) {
            throw Exception("None of (listId, listObj, username) can be null.")
        }

        redMarkerDrawable =
            BitmapDrawable(
                resources,
                BitmapFactory.decodeResource(resources, R.drawable.marker_red).let {
                    Bitmap.createScaledBitmap(it, 74, 120, false)
                })

        userLocationMarkerDrawable =
            BitmapDrawable(
                resources,
                BitmapFactory.decodeResource(resources, R.drawable.user_location_marker).let {
                    Bitmap.createScaledBitmap(it, 85, 85, false)
                })

        Configuration.getInstance().load(
            context,
            androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
        )

        var selectedMethod: String? = null
        var dueTimestamp: LocalDateTime? = null

        // hide the category layout initially
        val categoryLayout: LinearLayout? =
            rootView.findViewById(R.id.cti_category_selection_layout)
        val userLayout: LinearLayout? = rootView.findViewById(R.id.cti_user_selection_layout)
        val locationPointLayout: LinearLayout? =
            rootView.findViewById(R.id.cti_coordinate_selection_layout)
        categoryLayout?.visibility = View.GONE
        userLayout?.visibility = View.GONE
        locationPointLayout?.visibility = View.GONE

        // setup of category search/selection flow
        val catListView: ListView? = rootView.findViewById(R.id.cti_list_categories)

        val results: MutableList<TagValuePair> = mutableListOf()
        val catListAdapter = CategoryAdapter(requireActivity(), results)
        catListView?.adapter = catListAdapter

        val keywordSearchBtn: Button? = rootView.findViewById(R.id.cti_keyword_search_btn)
        val keywordBox: EditText? = rootView.findViewById(R.id.cti_category_search_keyword_edittext)
        var outputTextView: TextView? = rootView.findViewById(R.id.cti_reminder_type_extra)
        keywordSearchBtn?.setOnClickListener {

            hideKeyboard()
            if (keywordBox == null || keywordBox.text.isEmpty()) {
                return@setOnClickListener
            }

            results.clear()
            catListAdapter.notifyDataSetChanged()

            lookupTagsAndValues(keywordBox.text.toString().trim(), results, catListAdapter)
        }


        // setup of user selection flow
        val userListView: ListView? = rootView.findViewById(R.id.cti_list_usernames)

        val usernameResults: MutableList<String> = mutableListOf()
        val userListAdapter = UserAdapter(requireActivity(), usernameResults)
        userListView?.adapter = userListAdapter

        val configuration: IConfigurationProvider = Configuration.getInstance()
        val path = requireContext().filesDir
        val osmdroidBasePath = File(path, "osmdroid")
        osmdroidBasePath.mkdirs()
        val osmdroidTilePath = File(osmdroidBasePath, "tiles")
        osmdroidTilePath.mkdirs()
        configuration.osmdroidBasePath = osmdroidBasePath
        configuration.osmdroidTileCache = osmdroidTilePath

        mapView = rootView.findViewById(R.id.osm_map_point_selection)
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.mapCenter
        mapView.getLocalVisibleRect(Rect())

        mapView.minZoomLevel = 16.0
        mapView.maxZoomLevel = 19.0
        mapView.controller.setZoom(17.0)

        val zoomControl = mapView.zoomController
        zoomControl.setVisibility(CustomZoomButtonsController.Visibility.ALWAYS)
        zoomControl.setZoomInEnabled(true)
        zoomControl.setZoomOutEnabled(true)

        mapView.setMultiTouchControls(true)

        val userLocationMarker = Marker(mapView)
        userLocationMarker.icon = userLocationMarkerDrawable
        userLocationMarker.id = "USER_CURRENT_LOC_MARKER"

        val marker = Marker(mapView)
        marker.icon = redMarkerDrawable
        marker.infoWindow = null
        mapView.overlays.add(marker)

        val sharedUserLocation = viewModel.userLocation.value
        if (sharedUserLocation != null) {
            val point = GeoPoint(
                sharedUserLocation.first.latitude, sharedUserLocation.first.longitude
            )
            userLocationMarker.position = point
            marker.position = point

            mapView.overlays.add(userLocationMarker)
            mapView.controller.setCenter(point)
            mapView.controller.animateTo(point)

        } else {
            Toast.makeText(
                context,
                "Location services are not available - try again later",
                Toast.LENGTH_LONG
            ).show()
        }

        class MyMapEventsReceiver : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                if (p != null) {
                    marker.position = p
                    mapView.invalidate()
                    locationPoint = p
                }

                return true
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                return true
            }
        }

        val mapEventsReceiver: MyMapEventsReceiver = MyMapEventsReceiver()
        val eventsOverlay: MapEventsOverlay = MapEventsOverlay(mapEventsReceiver)
        mapView.overlays.add(0, eventsOverlay)

        viewModel.userLocation.observe(viewLifecycleOwner) { pair ->
            if (pair != null) {
                val loc = pair.first
                val diff = pair.second

                if (diff > 0.5) {
                    userLocationMarker.position = GeoPoint(loc.latitude, loc.longitude)
                    mapView.invalidate()
                }
            }
        }

        val submitBtn: Button? = rootView.findViewById(R.id.cti_create_task_btn)
        submitBtn?.setOnClickListener {
            val title = rootView.findViewById<EditText>(R.id.cti_task_name).text.toString().trim()
            val body = rootView.findViewById<EditText>(R.id.cti_body_text).text.toString().trim()

            val categories = catListAdapter.getSelectedCategories()
            val peerUser = userListAdapter.getSelectedUsernames()

            if (
                username.isNullOrBlank()
                || title.isBlank()
                || (selectedMethod == "LOCATION_CATEGORY" && categories.isEmpty())
                || (selectedMethod == "PEER_USER" && peerUser == null)
                || (selectedMethod == "LOCATION_POINT" && locationPoint == null)
            ) {
                Toast.makeText(
                    context,
                    "Sorry, something went wrong. Please check the provided information.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val newItem: MutableLiveData<TaskItem> = MutableLiveData()
            newItem.observe(viewLifecycleOwner) { newItem ->
                if (newItem != null) {
                    Log.d("bgnw-viewresume", "viewModel.lists.value ${viewModel.lists.value}")

                    viewModel.lists.value
                        ?.find { list -> list.list_id == listId!! }
                        ?.items
                        ?.add(newItem)

                    closeThisFragment()
                } else {
                    throw Exception("item should not be null once observer gets activated here")
                }
            }

            val reqResult =
                when (selectedMethod) {
                    "LOCATION_CATEGORY" -> {
                        CoroutineScope(Dispatchers.IO).async {
                            Requests.createItem(
                                list = listId!!,
                                owner = username,
                                title = title,
                                body_text = body,
                                remind_method = selectedMethod,
                                attachment_img_path = null,
                                snooze_until = null,
                                completed = false,
                                due_at = dueTimestamp?.format(dtZulu),
                                is_sub_task = false,
                                parent_task = null,
                                filters = categories
                            )
                        }
                    }

                    "PEER_USER" -> {
                        CoroutineScope(Dispatchers.IO).async {
                            Requests.createItem(
                                list = listId!!,
                                owner = username,
                                title = title,
                                body_text = body,
                                remind_method = selectedMethod,
                                user_peer = peerUser!!,
                                attachment_img_path = null,
                                snooze_until = null,
                                completed = false,
                                due_at = dueTimestamp?.format(dtZulu),
                                is_sub_task = false,
                                parent_task = null,
                                filters = null
                            )
                        }
                    }

                    "LOCATION_POINT" -> {
                        CoroutineScope(Dispatchers.IO).async {
                            Requests.createItem(
                                list = listId!!,
                                owner = username,
                                title = title,
                                body_text = body,
                                remind_method = selectedMethod,
                                user_peer = null,
                                attachment_img_path = null,
                                snooze_until = null,
                                completed = false,
                                due_at = dueTimestamp?.format(dtZulu),
                                lati = df.format(locationPoint?.latitude).toDouble(),
                                longi = df.format(locationPoint?.longitude).toDouble(),
                                is_sub_task = false,
                                parent_task = null,
                                filters = null
                            )
                        }
                    }

                    else -> {
                        throw Exception("Illegal selectedMethod state")
                    }
                }

            CoroutineScope(Dispatchers.IO).launch {
                val itemResult = reqResult.await()
                newItem.postValue(itemResult)
            }
        }

        // set function to run when remind method changes
        val radioGroup: RadioGroup? = rootView.findViewById(R.id.cti_radio_group)
        radioGroup?.setOnCheckedChangeListener { group, _ ->
            if (group.checkedRadioButtonId == R.id.cti_radio_opt_category) {
                categoryLayout?.visibility = View.VISIBLE
                userLayout?.visibility = View.GONE
                locationPointLayout?.visibility = View.GONE
                selectedMethod = "LOCATION_CATEGORY"
            } else if (group.checkedRadioButtonId == R.id.cti_radio_opt_person) {
                categoryLayout?.visibility = View.GONE
                userLayout?.visibility = View.VISIBLE
                locationPointLayout?.visibility = View.GONE
                findFriendUsernames(usernameResults, userListAdapter)
                selectedMethod = "PEER_USER"
            } else if (group.checkedRadioButtonId == R.id.cti_radio_opt_locationpoint) {
                categoryLayout?.visibility = View.GONE
                userLayout?.visibility = View.GONE
                locationPointLayout?.visibility = View.VISIBLE
                selectedMethod = "LOCATION_POINT"
            }
        }

        val editDueDate = rootView.findViewById<Button>(R.id.cti_due_edit_date)
        val editDueTime = rootView.findViewById<Button>(R.id.cti_due_edit_time)
        val loadingBg = rootView.findViewById<LinearLayout>(R.id.cti_loading_bg)
        val timePicker = rootView.findViewById<TimePicker>(R.id.cti_timepicker)
        val datePicker = rootView.findViewById<DatePicker>(R.id.cti_datepicker)
        val pickTimePopup = rootView.findViewById<LinearLayout>(R.id.cti_timepicker_popup)
        val pickDatePopup = rootView.findViewById<LinearLayout>(R.id.cti_datepicker_popup)
        val pickTimeDoneBtn = rootView.findViewById<Button>(R.id.cti_pick_time_done_btn)
        val pickDateDoneBtn = rootView.findViewById<Button>(R.id.cti_pick_date_done_btn)
        val dueMsg = rootView.findViewById<TextView>(R.id.cti_due_status_msg)

        editDueDate.setOnClickListener {
            loadingBg.visibility = View.VISIBLE
            pickDatePopup.visibility = View.VISIBLE
        }

        editDueTime.setOnClickListener {
            loadingBg.visibility = View.VISIBLE
            pickTimePopup.visibility = View.VISIBLE
        }

        pickDateDoneBtn.setOnClickListener {
            loadingBg.visibility = View.GONE
            pickDatePopup.visibility = View.GONE

            dueTimestamp = LocalDateTime.of(
                datePicker.year,
                datePicker.month,
                datePicker.dayOfMonth,
                dueTimestamp?.hour ?: 0,
                dueTimestamp?.minute ?: 0
            )

            dueMsg.text = dueTimestamp!!.format(dtHuman)
        }

        pickTimeDoneBtn.setOnClickListener {
            loadingBg.visibility = View.GONE
            pickTimePopup.visibility = View.GONE

            dueTimestamp = LocalDateTime.of(
                dueTimestamp?.year ?: LocalDateTime.now().year,
                dueTimestamp?.month ?: LocalDateTime.now().month,
                dueTimestamp?.dayOfMonth ?: LocalDateTime.now().dayOfMonth,
                timePicker.hour,
                timePicker.minute
            )

            dueMsg.text = dueTimestamp!!.format(dtHuman)
        }

        return rootView
    }

    // https://stackoverflow.com/questions/21504088
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as? AppCompatActivity)?.supportActionBar?.hide()
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as? AppCompatActivity)?.supportActionBar?.hide()
    }

    override fun onDestroy() {
        super.onDestroy()
        (requireActivity() as? AppCompatActivity)?.supportActionBar?.show()
    }

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
            }
        }
    }

    private fun findFriendUsernames(
        resultsDataset: MutableList<String>,
        adapter: UserAdapter
    ) {
        val queryResult: MutableLiveData<List<Collab>?> = MutableLiveData()

        CoroutineScope(Dispatchers.IO).launch {
            queryResult.postValue(Requests.getCollabs(viewModel.loggedInUsername.value!!))
        }

        queryResult.observe(viewLifecycleOwner) { data ->

            resultsDataset.clear()

            if (data.isNullOrEmpty()) {
                Toast.makeText(
                    context,
                    "Please connect with friends first. See the Friends tab for more",
                    Toast.LENGTH_SHORT
                ).show()
            }

            data?.forEach { d ->
                if (viewModel.loggedInUsername.value != d.user_master)
                    resultsDataset.add(d.user_master)
                else
                    resultsDataset.add(d.user_peer)
            }
            adapter.notifyDataSetChanged()
        }

    }

    private fun hideKeyboard() {
        if (view != null) {
            val imm =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(View(requireContext()).windowToken, 0)
        }
    }

    private fun closeThisFragment() {
        val fragmentManager: FragmentManager = this.requireActivity().supportFragmentManager
        fragmentManager.popBackStack()
    }
}