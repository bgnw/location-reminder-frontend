package com.bgnw.locationreminder.frag

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.bgnw.locationreminder.ApplicationState
import com.bgnw.locationreminder.MainActivity
import com.bgnw.locationreminder.R
import com.bgnw.locationreminder.data.TaskItem
import com.bgnw.locationreminder.data.TaskList
import com.bgnw.locationreminder.map_aux.MapInfoBox
import org.osmdroid.config.Configuration
import org.osmdroid.config.IConfigurationProvider
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
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
//        Configuration.getInstance().load(context as MainActivity, PreferenceManager.getDefaultSharedPreferences(context as MainActivity));

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

        /*
        val mapLoadingMessage = getView()?.findViewById<TextView>(R.id.map_loading_message)

        val tileStates = mapView.overlayManager.tilesOverlay.tileStates
        // evaluate the tile states
        while (tileStates.total != tileStates.upToDate) {
            Thread.sleep(1000)
            Log.d("MAP", "not ready")
        }
        mapLoadingMessage?.visibility = View.GONE
        Log.d("MAP", "ready")
        */

        var i = -3.31961

        viewModel.lists.observe(viewLifecycleOwner, Observer { lists ->
            if (lists != null) {
                for (list: TaskList in lists) {
                    if (list.items != null) {
                        for (item: TaskItem in list.items!!) {
                            i -= 0.002
                            addMarker(GeoPoint(55.91201, i), item.body_text, "Sample")
                        }
                    }
                }
            }
        })

    }




    private fun addMarker(geoPoint: GeoPoint, name: String, information: String) {
        val marker = Marker(mapView)
        marker.position = geoPoint
        marker.title = name
        marker.snippet = information
        marker.infoWindow = MapInfoBox(mapView)
        mapView.overlays.add(marker)
    }
}