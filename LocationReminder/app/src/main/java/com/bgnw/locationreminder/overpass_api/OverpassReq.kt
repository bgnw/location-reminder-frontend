package com.bgnw.locationreminder.overpass_api

import com.bgnw.locationreminder.api.TagValuePair
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.osmdroid.util.GeoPoint

@Serializable
data class OverpassElement(
    val type: String,
    val id: Long,
    val lat: Double? = null, // if node
    val lon: Double? = null, // if node
    val center: Center? = null, // if center of way
    val tags: Tags? = null
) {
    override fun toString(): String {
        return "OverpassElement(type='$type', id=$id, lat=$lat, lon=$lon, tags=${tags.toString()})"
    }
}

@Serializable
data class Center(val lat: Double, val lon: Double)

@Serializable
data class Tags(
    val name: String? = "",
    val official_name: String? = "",
    val amenity: String? = "",
    val shop: String? = "",
    val leisure: String? = "",
    val education: String? = "",
    val tourism: String? = "",
    val public_transport: String? = "",
    val building: String? = "",
    val sport: String? = "",
    val product: String? = "",
    val vending: String? = "",
    val cuisine: String? = "",
    val landuse: String? = "",
    val healthcare: String? = "",
    val place_of_worship: String? = "",
    val restaurant: String? = "",
    val beauty: String? = ""
) {
    override fun toString(): String {
        // show only the tags that are non null
        val sb = StringBuilder("Tags(")

        if (name != null) sb.append("name=$name, ")
        if (official_name != null) sb.append("official_name=$official_name, ")
        if (amenity != null) sb.append("amenity=$amenity, ")
        if (shop != null) sb.append("shop=$shop, ")
        if (leisure != null) sb.append("leisure=$leisure, ")
        if (education != null) sb.append("education=$education, ")
        if (tourism != null) sb.append("tourism=$tourism, ")
        if (public_transport != null) sb.append("public_transport=$public_transport, ")
        if (building != null) sb.append("building=$building, ")
        if (sport != null) sb.append("sport=$sport, ")
        if (product != null) sb.append("product=$product, ")
        if (vending != null) sb.append("vending=$vending, ")
        if (cuisine != null) sb.append("cuisine=$cuisine, ")
        if (landuse != null) sb.append("landuse=$landuse, ")
        if (healthcare != null) sb.append("healthcare=$healthcare, ")
        if (place_of_worship != null) sb.append("place_of_worship=$place_of_worship, ")
        if (restaurant != null) sb.append("restaurant=$restaurant, ")
        if (beauty != null) sb.append("beauty=$beauty, ")

        if (sb.endsWith(", ")) {
            sb.delete(sb.length - 2, sb.length)
        }

        sb.append(")")

        return sb.toString()
    }
}

@Serializable
data class OverpassResp(val elements: List<OverpassElement>) {
    override fun toString(): String {
        return "OverpassResp(elements=$elements)"
    }
}

suspend fun queryOverpassApi(query: String): OverpassResp {
    val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    return client.get("https://overpass-api.de/api/interpreter?data=$query").body()
}

fun getCoordinatesForElement(element: OverpassElement): GeoPoint {
    return when (element.type) {
        "node" -> {
            GeoPoint(element.lat!!, element.lon!!)
        }

        "way" -> {
            GeoPoint(element.center!!.lat, element.center.lon)
        }

        else -> {
            throw Exception("Overpass element isn't a node nor a way -- don't know how to get coords")
        }
    }
}

fun tagsClassToPairs(tags: Tags?): MutableList<TagValuePair>? {
    if (tags == null) {
        return null
    }
    val tagPairs = mutableListOf<TagValuePair>()
    if (tags.name != null) tagPairs.add(TagValuePair("name", tags.name))
    if (tags.official_name != null) tagPairs.add(TagValuePair("official_name", tags.official_name))
    if (tags.amenity != null) tagPairs.add(TagValuePair("amenity", tags.amenity))
    if (tags.shop != null) tagPairs.add(TagValuePair("shop", tags.shop))
    if (tags.leisure != null) tagPairs.add(TagValuePair("leisure", tags.leisure))
    if (tags.education != null) tagPairs.add(TagValuePair("education", tags.education))
    if (tags.tourism != null) tagPairs.add(TagValuePair("tourism", tags.tourism))
    if (tags.public_transport != null) tagPairs.add(
        TagValuePair(
            "public_transport",
            tags.public_transport
        )
    )
    if (tags.building != null) tagPairs.add(TagValuePair("building", tags.building))
    if (tags.sport != null) tagPairs.add(TagValuePair("sport", tags.sport))
    if (tags.product != null) tagPairs.add(TagValuePair("product", tags.product))
    if (tags.vending != null) tagPairs.add(TagValuePair("vending", tags.vending))
    if (tags.cuisine != null) tagPairs.add(TagValuePair("cuisine", tags.cuisine))
    if (tags.landuse != null) tagPairs.add(TagValuePair("landuse", tags.landuse))
    if (tags.healthcare != null) tagPairs.add(TagValuePair("healthcare", tags.healthcare))
    if (tags.place_of_worship != null) tagPairs.add(
        TagValuePair(
            "place_of_worship",
            tags.place_of_worship
        )
    )
    if (tags.restaurant != null) tagPairs.add(TagValuePair("restaurant", tags.restaurant))
    if (tags.beauty != null) tagPairs.add(TagValuePair("beauty", tags.beauty))
    return tagPairs
}

fun tagsStringMapToPairs(tagList: List<Map<String, String>>): MutableList<TagValuePair>? {
    val pattern = """([a-zA-Z_]+)=\"([a-zA-Z_]*)\"""".toRegex()
    val tagPairs = mutableListOf<TagValuePair>()

    for (tagMap in tagList) {
        var attr = tagMap["filters"]
        val matchResult = pattern.find(attr!!)
        if (matchResult != null) {
            var (osmTag, osmValue: String?) = matchResult!!.destructured
            tagPairs.add(
                TagValuePair(
                    osmTag,
                    osmValue.ifBlank { null }
                )
            )
        }
    }
    return tagPairs
}