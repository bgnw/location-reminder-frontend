package com.bgnw.locationreminder.frag

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import com.bgnw.locationreminder.ApplicationState
import com.bgnw.locationreminder.MainActivity
import com.bgnw.locationreminder.R
import com.bgnw.locationreminder.data.ItemOpportunity
import com.bgnw.locationreminder.map_aux.MapInfoBox
import com.bgnw.locationreminder.nominatim_api.queryNominatimApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
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

class MapFragment : Fragment() {

    private val viewModel: ApplicationState by activityViewModels()

    private lateinit var mapView: MapView
    private lateinit var redMarkerDrawable: BitmapDrawable
    private lateinit var userLocationMarkerDrawable: BitmapDrawable
    private lateinit var peerMarkerDrawable: Drawable

    private var markers = mutableSetOf<Marker>()
    private var markerSetLocked = MutableLiveData<Boolean>(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        val basePeerMarkerDrawable =
            ContextCompat.getDrawable(requireContext(), R.drawable.baseline_person_pin_circle_24)!!
        val yellowGreen = ContextCompat.getColor(requireContext(), R.color.yellow_green)
        peerMarkerDrawable = DrawableCompat.wrap(basePeerMarkerDrawable)
        DrawableCompat.setTint(peerMarkerDrawable, yellowGreen)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Configuration.getInstance().load(
            context,
            androidx.preference.PreferenceManager.getDefaultSharedPreferences(context as MainActivity)
        )

        val configuration: IConfigurationProvider = Configuration.getInstance()
        val path = requireContext().filesDir
        val osmdroidBasePath = File(path, "osmdroid")
        osmdroidBasePath.mkdirs()
        val osmdroidTilePath = File(osmdroidBasePath, "tiles")
        osmdroidTilePath.mkdirs()
        configuration.osmdroidBasePath = osmdroidBasePath
        configuration.osmdroidTileCache = osmdroidTilePath

        mapView = requireView().findViewById(R.id.osm_map)
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.mapCenter
        mapView.getLocalVisibleRect(Rect())

        mapView.minZoomLevel = 16.0
        mapView.maxZoomLevel = 22.0
        mapView.controller.setZoom(19.0)

        mapView.zoomController.setVisibility(CustomZoomButtonsController.Visibility.ALWAYS)
        mapView.setMultiTouchControls(true);

        val centerButton: Button = requireView().findViewById(R.id.btn_center_map)

        val sharedUserLocation = viewModel.userLocation.value

        val userLocationMarker = Marker(mapView)
        userLocationMarker.icon = userLocationMarkerDrawable
        userLocationMarker.id = "USER_CURRENT_LOC_MARKER"
        userLocationMarker.title = "Your location"
        userLocationMarker.snippet = ""
        userLocationMarker.infoWindow = MapInfoBox(mapView)

        if (sharedUserLocation != null) {
            val point = GeoPoint(
                sharedUserLocation.first.latitude, sharedUserLocation.first.longitude
            )
            userLocationMarker.position = point
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

        val mapLocalityMsg: TextView = view.findViewById(R.id.map_locality_msg)
        if (sharedUserLocation != null) {
            val locality =
                getLocality(sharedUserLocation.first.latitude, sharedUserLocation.first.longitude)
            mapLocalityMsg.text = "You're in $locality"
        }

        viewModel.userLocation.observe(viewLifecycleOwner) { pair ->
            if (pair != null) {
                val loc = pair.first
                val diff = pair.second

                if (diff > 0.5) {
                    userLocationMarker.position = GeoPoint(loc.latitude, loc.longitude)
                    mapView.invalidate()

                    val locality = getLocality(loc.latitude, loc.longitude)
                    mapLocalityMsg.text = "You're in $locality"
                }
            }
        }

        viewModel.reminders.value?.let { updateMarkers(it) }
        viewModel.peerLocations.value?.let { updatePeerMarkers(it) }


        /* MAP LOADING MESSAGE
        val mapLoadingMessage = getView()?.findViewById<TextView>(R.id.map_loading_message)

        val tileStates = mapView.overlayManager.tilesOverlay.tileStates
        // evaluate the tile states
        while (tileStates.total != tileStates.upToDate) {
            Thread.sleep(1000)
            Log.d("bgnw_MAP", "not ready")
        }
        mapLoadingMessage?.visibility = View.GONE
        Log.d("bgnw_MAP", "ready")
        */

        class MyMapEventsReceiver : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                InfoWindow.closeAllInfoWindowsOn(mapView)
                return true
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                return true
            }
        }

        val mapEventsReceiver: MyMapEventsReceiver = MyMapEventsReceiver()
        val eventsOverlay: MapEventsOverlay = MapEventsOverlay(mapEventsReceiver)
        mapView.overlays.add(0, eventsOverlay)

        viewModel.reminders.observe(viewLifecycleOwner) { reminders ->
            updateMarkers(reminders)
        }

        viewModel.peerLocations.observe(viewLifecycleOwner) { peers ->
            updatePeerMarkers(peers)
        }

        centerButton.setOnClickListener {
            mapView.controller.setCenter(userLocationMarker.position)
        }
    }

    private fun addMarker(
        geoPoint: GeoPoint,
        name: String,
        information: String,
        type: String,
        shouldInvalidate: Boolean = true
    ): Marker {
        val marker = Marker(mapView)
        marker.position = geoPoint
        marker.title = name
        marker.snippet = information
        marker.infoWindow = MapInfoBox(mapView)
        marker.icon = if (type == "USER") peerMarkerDrawable else redMarkerDrawable
        marker.id = "${geoPoint.latitude}x${geoPoint.longitude}"


        Log.d("bgnw", "adding marker: $name, $information")

        mapView.overlays.add(marker) // add marker to map

        if (shouldInvalidate)
            mapView.invalidate() // force map update

        markers.add(marker) // keep record of what markers are on the map
        return marker
    }

    private fun removeMarker(marker: Marker, shouldInvalidate: Boolean = true) {
        mapView.overlays.remove(marker) // remove marker from map

        if (shouldInvalidate)
            mapView.invalidate() // force map update

        markers.remove(marker) // remove marker from the list of current markers
    }

    private fun updateMarkers(reminders: MutableList<ItemOpportunity>) {
        for (marker in markers.toList()) {
            removeMarker(marker = marker, shouldInvalidate = false)
        }

        for (reminder in reminders.toList()) {
            addMarker(
                geoPoint = GeoPoint(reminder.lati, reminder.longi),
                name = reminder.category,
                information = "Matches ${reminder.matchingItemCount} ${if (reminder.matchingItemCount == 1) "item" else "items"}",
                type = "PLACE",
                shouldInvalidate = false
            )
        }

        mapView.invalidate() // update map to show updated markers
    }

    private fun updatePeerMarkers(peers: MutableMap<String, GeoPoint>) {
        for (peer in peers.toList()) {
            addMarker(
                geoPoint = peer.second,
                name = peer.first,
                information = "your friend",
                type = "USER",
                shouldInvalidate = false
            )
        }

        mapView.invalidate() // update map to show updated markers
    }

    private fun getLocality(lat: Double, lon: Double): String {
        val nominatimResp = CoroutineScope(Dispatchers.IO).async {
            queryNominatimApi(lat, lon)
        }
        while (!nominatimResp.isCompleted) {
            Thread.sleep(500)
        }
        var addressObj = nominatimResp.getCompleted().address
        var primary = addressObj.neighbourhood ?: addressObj.city
        var secondary =
            if (primary == addressObj.neighbourhood && addressObj.city != null) {
                addressObj.city
            } else {
                addressObj.county ?: addressObj.state ?: addressObj.country
            }
        var locality = "${if (primary != null) "$primary, " else ""}${secondary}"
        return locality
    }
}