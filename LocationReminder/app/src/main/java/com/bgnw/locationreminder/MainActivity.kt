package com.bgnw.locationreminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.bgnw.locationreminder.api.AccountDeviceTools
import com.bgnw.locationreminder.api.Requests
import com.bgnw.locationreminder.frag.AccountFragment
import com.bgnw.locationreminder.frag.ListsFragment
import com.bgnw.locationreminder.frag.MapFragment
import com.bgnw.locationreminder.frag.NearbyFragment
import com.bgnw.locationreminder.frag.SettingsFragment
import com.bgnw.locationreminder.frag.SharingFragment
import com.bgnw.locationreminder.overpass_api.OverpassResp
import com.bgnw.locationreminder.overpass_api.queryOverpassApi
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import java.lang.Thread.sleep
import kotlin.coroutines.CoroutineContext
import com.bgnw.locationreminder.api.AccountDeviceTools.Factory.retrieveUsername
import com.bgnw.locationreminder.api.AccountDeviceTools.Factory.retrieveDisplayName
import com.bgnw.locationreminder.api.Utils
import com.bgnw.locationreminder.location.LocationLiveData
import com.bgnw.locationreminder.location.LocationModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity(), CoroutineScope {

    // coroutine boilerplate from https://stackoverflow.com/questions/53928668/
    private var job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    lateinit var toggle: ActionBarDrawerToggle
    lateinit var drawerLayout: DrawerLayout

    private val viewModel: ApplicationState by viewModels()

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    fun updateTLs(username: String) {
        Log.d("bgnw", "updating TLs in MainActivity")
        val taskIsDone = MutableLiveData<Boolean>(false)
        val task = Utils.getUpdatedTLs(username)

        CoroutineScope(Dispatchers.IO).launch {
            task.await()
            taskIsDone.postValue(true)
        }

        taskIsDone.observe(this) {done ->
            if (done && task.isCompleted) {
                viewModel.lists.value = task.getCompleted()?.toMutableList() ?: mutableListOf()
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun updateFilters(username: String) {
        val taskIsDone = MutableLiveData<Boolean>(false)
        val task = Utils.getFiltersForUserDeferred(username)

        CoroutineScope(Dispatchers.IO).launch {
            task.await()
            taskIsDone.postValue(true)
        }

        taskIsDone.observe(this) {done ->
            if (done && task.isCompleted) {
                viewModel.filters.value = task.getCompleted() ?: listOf()
            }
        }
    }

    private fun signOut() {
        AccountDeviceTools.eraseData(this)
        viewModel.lists.value = null
        viewModel.loggedInUsername.value = null
        viewModel.loggedInDisplayName.value = null
    }

    // function is run once activity created (i.e. app is loaded in fg)
    @OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        // call default onCreate function
        super.onCreate(savedInstanceState)



        Class.forName("org.postgresql.Driver")


        Requests.initialiseApi()


        // set main content view
        setContentView(R.layout.activity_main)

//        viewModel.changeNeeded.value = false

        // initialise navigation drawer
        drawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // intialise account information in nav drawer
        val navUsername: TextView = navView.getHeaderView(0).findViewById(R.id.nav_user_username)
        val navDisplayName: TextView =
            navView.getHeaderView(0).findViewById(R.id.nav_user_display_name)







        // actions to take when user logs in
        viewModel.loggedInUsername.observe(this, Observer { username ->
            navUsername.text = username
            if (username != null) {
                updateTLs(username)
                updateFilters(username)
            }
        })


        // When user logs in, update the display name shown
        viewModel.loggedInDisplayName.observe(this, Observer { displayName ->
            runOnUiThread {
                navDisplayName.text = displayName
            }
        })

//        viewModel.changeNeeded.observe(this, Observer { changeNeeded ->
//            if (changeNeeded) {
//                Log.d("bgnw", "running changes")
//                val username = viewModel.loggedInUsername.value
//                if (username != null) {
////                    updateTLs(username)
//                }
//                viewModel.changeNeeded.value = false
//            }
//        })
//
//
//
//        viewModel.changesMade.observe(this) {changesMade ->
//            if (changesMade == null || changesMade == true) {
//                viewModel.changesMade.value = false
//                retrieveUsername(this)?.let { updateTLs(it) }
//            }
//        }
//
//        viewModel.changesMade.value = true




        var currentFragId: Int = R.id.lists
        val listFrag = ListsFragment()


        // open default fragment
        changeFragment(listFrag, "Lists")
        navView.setCheckedItem(R.id.lists)


        // nav menu click handler
        navView.setNavigationItemSelectedListener {

            // if the fragment is the same as the currently displayed one, do nothing
            if (currentFragId == it.itemId) { return@setNavigationItemSelectedListener true }

            it.isChecked = true
            when (it.itemId) {
                R.id.nearby -> {
                    currentFragId = R.id.nearby
                    changeFragment(NearbyFragment(), it.title.toString())
                }
                R.id.lists -> {
                    currentFragId = R.id.lists
                    changeFragment(listFrag, it.title.toString())
                }
                R.id.sharing -> {
                    currentFragId = R.id.sharing
                    changeFragment(SharingFragment(), it.title.toString())
                }
                R.id.account -> {
                    currentFragId = R.id.account
                    changeFragment(AccountFragment(), it.title.toString())
                }
                R.id.settings -> {
                    currentFragId = R.id.settings
                    changeFragment(SettingsFragment(), it.title.toString())
                }
                R.id.sign_out -> {
                    val text = if (viewModel.loggedInUsername.value != null) {
                        "You're logged out"
                    } else {
                        "Logged out of ${viewModel.loggedInDisplayName}'s account"
                    }

                    signOut()

                    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
                }
                // DEVELOPER MENU:
                R.id.DEV_MENU -> {
                    currentFragId = R.id.DEV_MENU
                    changeFragment(DeveloperOptions(), it.title.toString())
                }
                R.id.DEV_MAP -> {
                    currentFragId = R.id.DEV_MAP
                    changeFragment(MapFragment(), it.title.toString())
                }
            }
            true
        }

        viewModel.lists.observe(this, Observer {
            Log.d("bgnw", "Lists, running changes")
            if (currentFragId == R.id.lists) {
                reloadListsFragment()
            }
        })

        // Check and (if needed) request permission from the user to send notifications
        // TODO check this works on >= android 13
        requestNotifPermission()

        // Request permission to use location, if needed
        requestLocationPermission()

        // Create a general notification channel to send notifications from
        createNotificationChannel()


        val locationData = LocationLiveData(this)

        var lastUpdateHadContent: MutableLiveData<Boolean> = MutableLiveData(false)
        var lastLocation: LocationModel? = null
        fun getLocationData() = locationData
        getLocationData().observe(this) {loc ->

            Log.d("bgnw", "got location update")

            val diff = floatArrayOf(99f)

            if (lastLocation != null) {
                Location.distanceBetween(
                    lastLocation!!.latitude,
                    lastLocation!!.longitude,
                    loc.latitude,
                    loc.longitude,
                    diff
                )
            }

            lastLocation = loc

            if (lastUpdateHadContent.value == false || diff[0] > 4 || lastLocation == null) {
                val msg = "loc: ${loc.latitude}, ${loc.longitude} (diff: ${diff[0]})"
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                Log.d("bgnw_LOCATIONPROVIDER", msg)

                CoroutineScope(Dispatchers.IO).launch {
                    lastUpdateHadContent.postValue(
                        checkForReminders(debug = true)
                    )
                }
            }

            viewModel.userLocation.postValue(Pair(loc, diff[0]))
        }


        // Launch coroutine to check for reminders/notifications to send to user
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        val savedUser = retrieveUsername(this)
        if (savedUser != null && viewModel.loggedInUsername.value != savedUser) {
            viewModel.loggedInUsername.value = savedUser
            viewModel.loggedInDisplayName.value = retrieveDisplayName(this)
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun changeFragment(fragment: Fragment, title: String) {
        // navbar changing fragment click handler
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
        setTitle(title)
        drawerLayout.closeDrawers()
    }

    private fun reloadListsFragment() {
        val fragmentManager = supportFragmentManager
        val trans1 = fragmentManager.beginTransaction()
        trans1.replace(R.id.frame_layout, Fragment())
        trans1.commit()

        val trans2 = fragmentManager.beginTransaction()
        trans2.replace(R.id.frame_layout, ListsFragment())
        trans2.commit()
    }

    private fun requestNotifPermission() {
        // https://stackoverflow.com/questions/76490047/how-to-request-permission-to-send-notifications-android
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission Granted TODO
            } else {
                // Permission Denied / Cancel TODO
            }
        }

        if (SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Do your task on permission granted
                Log.d("bgnw_NOTIF", "permission already granted") // TEMP

            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
                Log.d("bgnw_NOTIF", "would show rationale then ask for perm") // TEMP
            } else {
                // Directly ask for the permission
                Log.d("bgnw_NOTIF", "asking for permission") // TEMP
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            // Below Android 13 You don't need to ask for notification permission.
            Log.d("bgnw_NOTIF", "below android 13, request not needed") // TEMP
        }
    }

    private fun createNotificationChannel() {
        val name = "Channel Name"
        val descriptionText = "Channel Description"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel =
            NotificationChannel(R.string.channel_id.toString(), name, importance).apply {
                description = descriptionText
            }

        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }


    private fun requestLocationPermission() {
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission Granted TODO
            } else {
                // Permission Denied / Cancel TODO
            }
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Do your task on permission granted
            Log.d("bgnw_LOCATION_PERMS", "permission already granted") // TEMP

        } else if (SDK_INT >= Build.VERSION_CODES.M
            && shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
        ) {
            // TODO: display an educational UI explaining to the user the features that will be enabled
            //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
            //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
            //       If the user selects "No thanks," allow the user to continue without notifications.
            Log.d("bgnw_LOCATION_PERMS", "would show rationale then ask for perm") // TEMP
        } else if (SDK_INT >= Build.VERSION_CODES.M) {
            // Directly ask for the permission
            Log.d("bgnw_LOCATION_PERMS", "asking for permission") // TEMP
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }


    private suspend fun getNearbyNodes(
        lat: Double,
        lon: Double,
        areaRadius: Double,
        filters: List<String>?
    ): OverpassResp? {
        Log.d("bgnw", "running getNearbyNodes")


        if (filters == null) { return null }

        val overpassQuery = buildString {
            append("[out:json][timeout:60]; (")
            filters.forEach { filter ->
                append("nw(around: $areaRadius, $lat, $lon)[$filter];")
            }
            append("); out center;")
        }


        try {
            val response = queryOverpassApi(overpassQuery)
            Log.d("bgnw", "running getNearbyNodes -> done.")

            return response
        } catch (e: Exception) {
            Log.d("bgnw_overpass", "Error: ${e.message}")
            return null
        }
    }


    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("MissingPermission")
    private suspend fun checkForReminders(debug: Boolean = false): Boolean {

        var resultsDeferred: Deferred<Pair<OverpassResp?, Pair<Double, Double>>>? = null
        var userLoc: LocationModel? = null

        fun remindersPt3(res: OverpassResp) {
            for (node in res.elements) {
                val dist = FloatArray(1)
                Location.distanceBetween(userLoc!!.latitude, userLoc!!.longitude, node.lat, node.lon, dist)
                if (dist[0] < 500) { // within 300 metres
                    Log.d("bgnw", "notifying")



                    NotificationTools.showNotification(
                        this@MainActivity,
                        "Nearby task available",
                        "${node.toString().substring(0,20)} is ${dist[0].roundToInt()}m away"
                    )
                } else {
                    Log.d("bgnw", "NOT notifying, dist is ${dist[0]}")

                }
            }
        }


        suspend fun remindersPt2(lat: Double, long: Double):
                Pair<OverpassResp?, Pair<Double, Double>> {
            return Pair(
                getNearbyNodes(lat, long, 400.0, viewModel.filters.value),
                Pair(lat, long)
            )
        }


        fun remindersPt1() {
            resultsDeferred = GlobalScope.async {
                remindersPt2(userLoc!!.latitude, userLoc!!.longitude)
            }

            /*
            val cancelToken: CancellationToken = CancellationTokenSource().token
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancelToken)
                .addOnSuccessListener { loc ->
                    if (loc != null) {
                        resultsDeferred = GlobalScope.async {
                            remindersPt2(loc.latitude, loc.longitude)
                        }
                    }
                }
             */
        }


        userLoc = viewModel.userLocation.value?.first
        if (userLoc == null) { return false }

        if (debug) Log.d("bgnw", "run remindersPt1()")
        remindersPt1()
        if (debug) Log.d("bgnw", "run remindersPt1() -> done")


        while (resultsDeferred == null) {
            if (debug) Log.d("bgnw", "wait for pt2 task")
            sleep(1000)
        }

        if (debug) Log.d("bgnw", "wait for pt2 task -> done")
        val res = resultsDeferred!!.await()
        if (res.first != null) {
            if (debug) Log.d("bgnw", "run remindersPt3()")
            remindersPt3(res.first!!)
            if (debug) Log.d("bgnw", "run remindersPt3() -> done (data is ${res.first.toString()})")
            return true
        } else {
            if (debug) Log.d("bgnw", "res empty")
            return false
        }

    }
}
