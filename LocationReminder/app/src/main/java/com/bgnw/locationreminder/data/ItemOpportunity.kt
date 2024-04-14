package com.bgnw.locationreminder.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ItemOpportunity(
    val opp_id: Int,
    val matchingItems: List<TaskItem>?,
    val suppressed: Boolean,
    val place_name: String?,
    val category: String,
    val metresFromUser: Int?,
    val matchingItemCount: Int?,
    val lati: Double,
    val longi: Double,
    var alti: Double,
) : Parcelable {
    override fun toString(): String {
        return """
            {
                "opp_id": $opp_id
                "matchingItems": $matchingItems
                "suppressed": $suppressed
                "place_name": $place_name
                "category": $category
                "lati": $lati
                "longi": $longi
                "alti": $alti
            }
        """.trimIndent()
    }
}