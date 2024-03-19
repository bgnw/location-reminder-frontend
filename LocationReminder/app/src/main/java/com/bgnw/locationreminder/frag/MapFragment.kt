package com.bgnw.locationreminder.frag

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import com.bgnw.locationreminder.ApplicationState
import com.bgnw.locationreminder.MainActivity
import com.bgnw.locationreminder.R
import com.bgnw.locationreminder.data.ItemOpportunity
import com.bgnw.locationreminder.map_aux.MapInfoBox
import com.bgnw.locationreminder.overpass_api.OverpassElement
import com.bgnw.locationreminder.overpass_api.OverpassResp
import com.bgnw.locationreminder.overpass_api.getCoordinatesForElement
import com.bgnw.locationreminder.overpass_api.queryOverpassApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
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
import java.lang.Thread.sleep

class MapFragment : Fragment() {

    private val viewModel: ApplicationState by activityViewModels()

    private lateinit var mapView: MapView
    private lateinit var redMarkerDrawable: BitmapDrawable
    private lateinit var userLocationMarkerDrawable: BitmapDrawable

    private var markers = mutableSetOf<Marker>()
    private var markerSetLocked = MutableLiveData<Boolean>(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        redMarkerDrawable =
            BitmapDrawable(resources, BitmapFactory.decodeResource(resources, R.drawable.marker_red).let {
                Bitmap.createScaledBitmap(it, 74, 120, false)
            })

        userLocationMarkerDrawable =
            BitmapDrawable(resources, BitmapFactory.decodeResource(resources, R.drawable.user_location_marker).let {
                Bitmap.createScaledBitmap(it, 85, 85, false)
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
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


//        val osmdroidBasePath = File(Environment.getExternalStorageDirectory(), "osmdroid")
//        val osmdroidTileCache = File(osmdroidBasePath, "tile")

        // Set the storage parameters
//        Configuration.getInstance().osmdroidBasePath = osmdroidBasePath
//        Configuration.getInstance().osmdroidTileCache = osmdroidTileCache


        mapView = requireView().findViewById(R.id.osm_map)
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.mapCenter
        mapView.getLocalVisibleRect(Rect())

        mapView.minZoomLevel = 16.0
        mapView.maxZoomLevel = 19.0
        mapView.controller.setZoom(19.0)

        mapView.zoomController.setVisibility(CustomZoomButtonsController.Visibility.ALWAYS)

        val updateMapButton: Button = requireView().findViewById(R.id.btn_update_map)
        val overrideButton: Button = requireView().findViewById(R.id.btn_override)
        val centerButton: Button = requireView().findViewById(R.id.btn_center_map)

        val addMkrBtn: Button = requireView().findViewById(R.id.btn_mkr_add)
        val delMkrBtn: Button = requireView().findViewById(R.id.btn_mkr_del)


//        val locationOverlay: MyLocationNewOverlay
        val sharedUserLocation = viewModel.userLocation.value
//        if (sharedUserLocation != null) {
//            locationOverlay = MyLocationNewOverlay(, mapView)
//        }
//        else {
//            locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), mapView)
//        }

//        locationOverlay.enableMyLocation()
//        locationOverlay.enableFollowLocation()
////        locationOverlay.isDrawAccuracyEnabled = true
//        locationOverlay.runOnFirstFix {
//            requireActivity().runOnUiThread {
//                mapView.controller.setCenter(locationOverlay.myLocation)
//                mapView.controller.animateTo(locationOverlay.myLocation)
//            }
//        }
//        mapView.overlays.add(locationOverlay)

        val userLocationMarker = Marker(mapView)
        userLocationMarker.icon = userLocationMarkerDrawable
        userLocationMarker.id = "USER_CURRENT_LOC_MARKER"

        if (sharedUserLocation != null) {
            val point = GeoPoint(
                sharedUserLocation.first.latitude, sharedUserLocation.first.longitude
            )
            userLocationMarker.position = point
            mapView.overlays.add(userLocationMarker)
            mapView.controller.setCenter(point)
            mapView.controller.animateTo(point)

        } else {
            Toast.makeText(context, "Location services are not available - try again later", Toast.LENGTH_LONG).show()
        }

        addMarker(GeoPoint(55.91201, -3.31961), "GRID", "Research building")
        mapView.invalidate()

        viewModel.userLocation.observe(viewLifecycleOwner) {pair ->
            if (pair != null) {
                val loc = pair.first
                val diff = pair.second

                if (diff > 0.5) {
                    userLocationMarker.position = GeoPoint(loc.latitude, loc.longitude)
                    mapView.invalidate()
                }
            }
        }

        viewModel.reminders.value?.let { updateMarkers(it) }

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

//
//        viewModel.lists.observe(viewLifecycleOwner, Observer { lists ->
//            val opps = Utils.getOppsFromLists(lists?.toMutableList())
//            if (opps != null) {
//                for (opp: ItemOpportunity in opps) {
//                    addMarker(GeoPoint(opp.lati, opp.longi), opp.place_name, opp.category)
//                }
//            }
//        })

//        fun getAsianFood(lat: Double, lon: Double, areaRadius: Double) {
//            GlobalScope.launch {
//                val asianFood = getNearbyNodes(lat, lon, areaRadius, "\"cuisine\"=\"asian\"")
//                if (asianFood != null) {
//                    for (element: OverpassElement in asianFood.elements) {
//                        addMarker(
//                            GeoPoint(element.lat, element.lon),
//                            element.tags?.name ?: "<name>",
//                            element.tags?.amenity ?: "<amenity/shop>"
//                        )
//                    }
//                }
//            }
//        }

        fun getPOI(lat: Double, lon: Double, areaRadius: Double) {
            CoroutineScope(Dispatchers.IO).launch {
                val nodes = getAllNearbyPOI(lat, lon, areaRadius)
                if (nodes != null) {
                    for (element: OverpassElement in nodes.elements) {

                        addMarker(
                            getCoordinatesForElement(element),
                            element.tags?.name ?: element.tags?.official_name ?: "(no name)",
                            element.tags?.amenity
                                ?: element.tags?.shop
                                ?: element.tags?.leisure
                                ?: element.tags?.education
                                ?: element.tags?.tourism
                                ?: element.tags?.public_transport
                                ?: element.tags?.building
                                ?: element.tags?.sport
                                ?: element.tags?.product
                                ?: element.tags?.vending
                                ?: element.tags?.cuisine
                                ?: element.tags?.landuse
                                ?: element.tags?.healthcare
                                ?: element.tags?.place_of_worship
                                ?: element.tags?.restaurant
                                ?: element.tags?.beauty
                                ?: "(no info)"
                        )
                    }
                }
            }
        }

        fun updateOnDrag(lat: Double, lon: Double, zoom: Double): Boolean {
            val areaRadius = 300 + (20 - zoom) * 80
            Log.d("bgnw_update-map", "updating map due to drag (zoom: $zoom, factor:$areaRadius)")
            // getAsianFood(lat, lon, areaRadius)
            getPOI(lat, lon, areaRadius)
            return true
        }

        updateMapButton.setOnClickListener {
            GlobalScope.launch {
                val resp = getNearbyNodes(mapView.boundingBox.centerLatitude, mapView.boundingBox.centerLongitude, 400.0, "'amenity'='bicycle_parking'")
                Log.d("bgnw", resp.toString())
            }
            updateOnDrag(
                mapView.boundingBox.centerLatitude,
                mapView.boundingBox.centerLongitude,
                mapView.zoomLevelDouble
            )
        }

        overrideButton.setOnClickListener {
            mapView.maxZoomLevel = 25.0
            mapView.minZoomLevel = 0.0
        }

        centerButton.setOnClickListener {
            mapView.controller.setCenter(userLocationMarker.position)
        }




//
//        var themarker: Marker? = null
//        addMkrBtn.setOnClickListener {
//           themarker = addMarker(GeoPoint(55.90001, -3.30061), "test", "test2")
//        }
//
//        delMkrBtn.setOnClickListener {
//            removeMarker(themarker!!)
//
//            for(marker in markers.toList()) {
//                removeMarker(marker)
//            }
//        }

    }

    private fun addMarker(geoPoint: GeoPoint, name: String, information: String, shouldInvalidate: Boolean = true): Marker {
        val marker = Marker(mapView)
        marker.position = geoPoint
        marker.title = name
        marker.snippet = information
        marker.infoWindow = MapInfoBox(mapView)
        marker.icon = redMarkerDrawable
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
        for(marker in markers.toList()) {
            removeMarker(marker = marker, shouldInvalidate = false)
        }

        for (reminder in reminders.toList()) {
            addMarker(
                geoPoint = GeoPoint(reminder.lati, reminder.longi),
                name = reminder.category,
                information = "Matches ${reminder.matchingItemCount} ${if (reminder.matchingItemCount == 1) "item" else "items"}",
                shouldInvalidate = false
            )
        }

        mapView.invalidate() // update map to show updated markers
    }

    private suspend fun getNearbyNodes(
        lat: Double,
        lon: Double,
        areaRadius: Double,
        conditions: String
    ): OverpassResp? {
        val overpassQuery = """
        [out:json][timeout:60];
        (
          node(around: $areaRadius, $lat, $lon)[$conditions];
        );
        out geom;

    """.trimIndent()

        try {
            val response = queryOverpassApi(overpassQuery)
            return response
        } catch (e: Exception) {
            Log.d("bgnw_overpass", "Error: ${e.message}")
            return null
        }
    }

    private suspend fun getAllNearbyPOI(
        lat: Double,
        lon: Double,
        areaRadius: Double
    ): OverpassResp? {
        val overpassQuery = """
        [out:json][timeout:60];
        (
            node(around: $areaRadius, $lat, $lon)["amenity"]
                ["amenity" != "bench"]
                ["amenity" != "waste_basket"];
            node(around: $areaRadius, $lat, $lon)["tourism"];
            node(around: $areaRadius, $lat, $lon)["shop"];
            node(around: $areaRadius, $lat, $lon)["leisure"];
            node(around: $areaRadius, $lat, $lon)["public_transport"];
            node(around: $areaRadius, $lat, $lon)["craft"];
            node(around: $areaRadius, $lat, $lon)["office"];
            node(around: $areaRadius, $lat, $lon)["place"];
            node(around: $areaRadius, $lat, $lon)["building"];
            node(around: $areaRadius, $lat, $lon)["man_made"];
            node(around: $areaRadius, $lat, $lon)["emergency"];
            node(around: $areaRadius, $lat, $lon)["healthcare"];
            node(around: $areaRadius, $lat, $lon)["education"];
            node(around: $areaRadius, $lat, $lon)["sport"];
            
            
        );
        out geom;

    """.trimIndent()

        /* removed from query for now

            way(around: $areaRadius, $lat, $lon)["amenity"];
            way(around: $areaRadius, $lat, $lon)["tourism"];
            way(around: $areaRadius, $lat, $lon)["shop"];
            way(around: $areaRadius, $lat, $lon)["leisure"];
            way(around: $areaRadius, $lat, $lon)["public_transport"];
            way(around: $areaRadius, $lat, $lon)["craft"];
            way(around: $areaRadius, $lat, $lon)["office"];
            way(around: $areaRadius, $lat, $lon)["place"]
                ["place" != "neighbourhood"];
            way(around: $areaRadius, $lat, $lon)["man_made"];
            way(around: $areaRadius, $lat, $lon)["emergency"];
            way(around: $areaRadius, $lat, $lon)["healthcare"];
            way(around: $areaRadius, $lat, $lon)["education"];
            way(around: $areaRadius, $lat, $lon)["sport"];
         */

        Log.d("bgnw_overpass", "QUERY IS: \n$overpassQuery")


        try {
            val response = queryOverpassApi(overpassQuery)
            return response
        } catch (e: Exception) {
            Log.d("bgnw_overpass", "Error: ${e.message}")
            return null
        }
    }
}