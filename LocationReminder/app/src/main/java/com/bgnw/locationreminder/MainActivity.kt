package com.bgnw.locationreminder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
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
import androidx.lifecycle.Observer
import com.bgnw.locationreminder.api.Requests
import com.bgnw.locationreminder.api.TaskList_ApiStruct
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext


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

    // function is run once activity created (i.e. app is loaded in fg)
    override fun onCreate(savedInstanceState: Bundle?) {
        Class.forName("org.postgresql.Driver")

        // call default onCreate function
        super.onCreate(savedInstanceState)

        // set main content view
        setContentView(R.layout.activity_main)

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
        viewModel.loggedInUsername.observe(this, Observer { username ->
            navUsername.text = username
            if (username != null) {
                launch {
                    Log.d("DJA API", "before**")
                    val resultTL = Requests.getTaskListsByUsername(username)
                    Log.d("DJA API", "after1**")
                    Log.d("DJA API", "response1: $resultTL")
                    Log.d("DJA API", "response1x: ${resultTL?.first()?.items.toString()}")


                    if (resultTL != null) {
                        for (list: TaskList_ApiStruct in resultTL) {
                            Log.d("DJA API", "running loop")

                            if (list.list_id == null) continue
                            val items = Requests.getListItemsById(list.list_id, null)
                            list.items = items
                        }
                    } else {
                        Log.d("DJA API", "body is null")
                    }
                    Log.d("DJA API", "after2**")
                    Log.d("DJA API", "response2: $resultTL")
                    Log.d("DJA API", "response2x: ${resultTL?.first()?.items.toString()}")





                }
            }
        })
        viewModel.loggedInDisplayName.observe(this, Observer { displayName ->
            navDisplayName.text = displayName
        })

        // open default fragment
        changeFragment(Nearby(), "Nearby")
        navView.setCheckedItem(R.id.nearby)

        // nav menu click handler
        navView.setNavigationItemSelectedListener {
            it.isChecked = true
            when (it.itemId) {
                R.id.nearby -> changeFragment(Nearby(), it.title.toString())
                R.id.lists -> changeFragment(Lists(), it.title.toString())
                R.id.sharing -> changeFragment(Sharing(), it.title.toString())
                R.id.account -> changeFragment(Account(), it.title.toString())
                R.id.settings -> changeFragment(Settings(), it.title.toString())
                R.id.sign_out -> Toast.makeText(this, "Clicked sign out", Toast.LENGTH_SHORT).show()
                // DEVELOPER MENU:
                R.id.DEV_MENU -> changeFragment(DeveloperOptions(), it.title.toString())
                R.id.DEV_MAP -> changeFragment(MapFragment(), it.title.toString())
            }
            true
        }

        // Check and (if needed) request permission from the user to send notifications
        // TODO check this works on >= android 13
        requestNotifPermission()

        // Create a general notification channel to send notifications from
        createNotificationChannel()

        requestLocationPermission()
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
                Log.d("NOTIF", "permission already granted") // TEMP

            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
                Log.d("NOTIF", "would show rationale then ask for perm") // TEMP
            } else {
                // Directly ask for the permission
                Log.d("NOTIF", "asking for permission") // TEMP
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            // Below Android 13 You don't need to ask for notification permission.
            Log.d("NOTIF", "below android 13, request not needed") // TEMP
        }
    }

    private fun createNotificationChannel() {
        if (SDK_INT >= Build.VERSION_CODES.O) {
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
            Log.d("LOCATION_PERMS", "permission already granted") // TEMP

        } else if (SDK_INT >= Build.VERSION_CODES.M
            && shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
        ) {
            // TODO: display an educational UI explaining to the user the features that will be enabled
            //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
            //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
            //       If the user selects "No thanks," allow the user to continue without notifications.
            Log.d("LOCATION_PERMS", "would show rationale then ask for perm") // TEMP
        } else if (SDK_INT >= Build.VERSION_CODES.M) {
            // Directly ask for the permission
            Log.d("LOCATION_PERMS", "asking for permission") // TEMP
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
}