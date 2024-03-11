package com.bgnw.locationreminder.overpass_api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class OverpassElement(val type: String, val id: Long, val lat: Double,
                           val lon: Double, val tags: Tags? = null)

@Serializable
data class Center(val lat: Double, val lon: Double)

@Serializable
data class Tags(
    val name: String? = "",
    val amenity: String? = "",
    val shop: String? = "",
    val place: String? = "",
    val leisure: String? = "",
    val office: String? = "",
    val education: String? = "",
    val tourism: String? = "",
    val public_transport: String? = "",
    val craft: String? = "",
    val building: String? = "",
    val man_made: String? = "",
    val emergency: String? = "",
    val healthcare: String? = "",
    val sport: String? = ""
)

@Serializable
data class OverpassResp(val elements: List<OverpassElement>)


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