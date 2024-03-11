package com.bgnw.locationreminder.frag

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.bgnw.locationreminder.ApplicationState
import com.bgnw.locationreminder.MainActivity
import com.bgnw.locationreminder.R
import com.bgnw.locationreminder.api.Utils
import com.bgnw.locationreminder.data.ItemOpportunity
import com.bgnw.locationreminder.map_aux.MapInfoBox
import com.bgnw.locationreminder.overpass_api.OverpassElement
import com.bgnw.locationreminder.overpass_api.OverpassResp
import com.bgnw.locationreminder.overpass_api.queryOverpassApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.config.IConfigurationProvider
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.File

class MapFragment : Fragment() {

    private val viewModel: ApplicationState by activityViewModels()

    private lateinit var mapView: MapView

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
        mapView.controller.setZoom(20.0)

        val updateMapButton: Button = requireView().findViewById(R.id.btn_update_map)

        val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), mapView)
        locationOverlay.enableMyLocation()
        locationOverlay.enableFollowLocation()
//        locationOverlay.isDrawAccuracyEnabled = true
        locationOverlay.runOnFirstFix {
            requireActivity().runOnUiThread {
                mapView.controller.setCenter(locationOverlay.myLocation)
                mapView.controller.animateTo(locationOverlay.myLocation)
            }
        }
        mapView.overlays.add(locationOverlay)

        addMarker(GeoPoint(55.91201, -3.31961), "GRID", "Research building")
        mapView.invalidate()

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

        class MyMapEventsReceiver: MapEventsReceiver {
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


        viewModel.lists.observe(viewLifecycleOwner, Observer { lists ->
            val opps = Utils.getOppsFromLists(lists?.toMutableList())
            if (opps != null) {
                for (opp: ItemOpportunity in opps){
                    addMarker(GeoPoint(opp.lati, opp.longi), opp.place_name, opp.category)
                }
            }
        })

        fun getAsianFood(lat: Double, lon: Double, areaRadius: Double) {
            GlobalScope.launch {
                val asianFood = getNearbyNodes(lat, lon, areaRadius, "\"cuisine\"=\"asian\"")
                if (asianFood != null) {
                    for (element: OverpassElement in asianFood.elements) {
                        addMarker(
                            GeoPoint(element.lat, element.lon),
                            element.tags?.name ?: "<name>",
                            element.tags?.amenity ?: "<amenity/shop>"
                        )
                    }
                }
            }
        }

        fun getPOI(lat: Double, lon: Double, areaRadius: Double) {
            GlobalScope.launch {
                val nodes = getAllNearbyPOI(lat, lon, areaRadius)
                if (nodes != null) {
                    for (element: OverpassElement in nodes.elements) {
                        addMarker(
                            GeoPoint(element.lat, element.lon),
                            element.tags?.name ?: "<name>",
                            element.tags?.amenity
                                ?: element.tags?.shop
                                ?: element.tags?.place
                                ?: element.tags?.leisure
                                ?: element.tags?.office
                                ?: element.tags?.education
                                ?: element.tags?.tourism
                                ?: element.tags?.public_transport
                                ?: element.tags?.craft
                                ?: element.tags?.building
                                ?: element.tags?.man_made
                                ?: element.tags?.emergency
                                ?: element.tags?.healthcare
                                ?: element.tags?.sport
                                ?: "<amenity/shop>"
                        )
                    }
                }
            }
        }

        fun updateOnDrag(lat: Double, lon: Double, zoom: Double): Boolean {
            val areaRadius = 300+(20-zoom)*80
            Log.d("bgnw_update-map", "updating map due to drag (zoom: $zoom, factor:$areaRadius)")
            // getAsianFood(lat, lon, areaRadius)
            getPOI(lat, lon, areaRadius)
            return true
        }

        updateMapButton.setOnClickListener {
            GlobalScope.launch {
                val resp = getNearbyNodes(55.9086, -3.3187, 400.0, "'amenity'='bicycle_parking'")
                Log.d("bgnw", resp.toString())
            }
//            updateOnDrag(
//                mapView.boundingBox.centerLatitude,
//                mapView.boundingBox.centerLongitude,
//                mapView.zoomLevelDouble
//            )
        }
    }

    private fun addMarker(geoPoint: GeoPoint, name: String, information: String) {
        val marker = Marker(mapView)
        marker.position = geoPoint
        marker.title = name
        marker.snippet = information
        marker.infoWindow = MapInfoBox(mapView)

        mapView.overlays.add(marker)
    }

    private suspend fun getNearbyNodes(lat: Double, lon: Double, areaRadius: Double, conditions: String): OverpassResp? {
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

    private suspend fun getAllNearbyPOI(lat: Double, lon: Double, areaRadius: Double): OverpassResp? {
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