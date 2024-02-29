package com.bgnw.locationreminder

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.osmdroid.config.Configuration
import org.osmdroid.config.IConfigurationProvider
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.File

class MapFragment : Fragment() {

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
        mapView.invalidate()

    }
}