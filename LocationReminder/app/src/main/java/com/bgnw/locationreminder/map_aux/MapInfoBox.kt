package com.bgnw.locationreminder.map_aux

import android.widget.TextView
import com.bgnw.locationreminder.R
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow

class MapInfoBox(mapView: MapView):
    InfoWindow(R.layout.bonuspack_bubble, mapView) {

    override fun onClose() {
        // TODO("Not yet implemented")
    }

    override fun onOpen(item: Any?) {
        // any window opening code

        if (item is Marker){
            val markerName = mView.findViewById<TextView>(R.id.bubble_title)
            val markerDescription = mView.findViewById<TextView>(R.id.bubble_description)

            markerName.text = item.title
            markerDescription.text = item.snippet
        }
    }
}