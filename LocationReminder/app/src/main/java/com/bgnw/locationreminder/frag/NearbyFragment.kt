package com.bgnw.locationreminder.frag

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.bgnw.locationreminder.ApplicationState
import com.bgnw.locationreminder.MainActivity
import com.bgnw.locationreminder.R
import com.bgnw.locationreminder.adapter.ItemOpportunityListAdapter
import com.bgnw.locationreminder.api.Utils
import com.bgnw.locationreminder.data.ItemOpportunity
import com.bgnw.locationreminder.data.TaskList
import com.bgnw.locationreminder.databinding.FragmentNearbyBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.time.format.DateTimeFormatter

class NearbyFragment : Fragment() {

    private val viewModel: ApplicationState by activityViewModels()

    private lateinit var binding: FragmentNearbyBinding
    private var dtFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm") // TODO remove if not used here
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_nearby, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentNearbyBinding.inflate(layoutInflater)
        var distanceConditionMetres = 500
        var userLat: Double? = 0.0
        var userLong: Double? = 0.0
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        val userToPointDist = FloatArray(1)

        @SuppressLint("MissingPermission")
        fun updateUserLocation(){
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    userLat =  location?.latitude
                    userLong = location?.longitude
                }
        }
        updateUserLocation()

        var allOpps: MutableList<ItemOpportunity>?
        var nearbyOpps: MutableList<ItemOpportunity> = mutableListOf()
        val context = context as MainActivity
        val lv = context.findViewById(R.id.lv_nearby_tasks) as ListView
        val adapter = ItemOpportunityListAdapter(context, nearbyOpps)
        lv.adapter = adapter

        fun updateAdapterData(lists: List<TaskList>?) {
            nearbyOpps.clear() // clear current nearby items
            allOpps = Utils.getOppsFromLists(lists) // get all opps

            if (allOpps != null) {
                for (opp: ItemOpportunity in allOpps!!) {
                    if (userLat != null && userLong != null) {
                        userToPointDist[0] = -1.0F
                        Location.distanceBetween(opp.lati, opp.longi, userLat!!, userLong!!, userToPointDist)
                        if (userToPointDist[0] <= distanceConditionMetres) { // if opp is "nearby", add it to nearby list
                            nearbyOpps.add(opp)
                        }
                    }
                }
            }

            adapter.notifyDataSetChanged()
            adapter.notifyDataSetInvalidated()
        }

        viewModel.lists.observe(viewLifecycleOwner, Observer { lists ->
            updateUserLocation()
            updateAdapterData(lists)
        })
    }
}
