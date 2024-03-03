package com.bgnw.locationreminder.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter

@Parcelize
data class ItemOpportunity(
    val opp_id: Int,
    val item: TaskItem,
    val suppressed: Boolean,
    val place_name: String,
    val category: String,
    val lati: Double,
    val longi: Double,
    var alti: Double,
): Parcelable {
    override fun toString(): String {
        return """
            {
                "opp_id": $opp_id
                "item": $item
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