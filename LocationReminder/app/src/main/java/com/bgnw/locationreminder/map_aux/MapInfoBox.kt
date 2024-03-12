package com.bgnw.locationreminder.map_aux

import android.view.View
import android.widget.TextView
import com.bgnw.locationreminder.R
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow


class MapInfoBox(mapView: MapView) :
    InfoWindow(R.layout.bonuspack_bubble, mapView) {


    override fun onClose() {
        mView.visibility = View.GONE
    }

    override fun onOpen(item: Any?) {
        // close any other info windows that may be open
        closeAllInfoWindowsOn(mapView)

        if (item is Marker) {
            // make this info window visible
            mView.visibility = View.VISIBLE

            val markerName = mView.findViewById<TextView>(R.id.bubble_title)
            val markerDescription = mView.findViewById<TextView>(R.id.bubble_description)

            markerName.text = item.title
            markerDescription.text = item.snippet
        }
    }
}