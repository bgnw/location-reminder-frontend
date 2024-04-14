package com.bgnw.locationreminder.nominatim_api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class NominatimResp(
    val place_id: Int,
    val address: Address
)

@Serializable
data class Address(
    val road: String? = null,
    val neighbourhood: String? = null,
    val suburb: String? = null,
    val city: String? = null,
    val state: String? = null,
    val postcode: String? = null,
    val county: String? = null,
    val country: String? = null,
    val country_code: String? = null,
)


suspend fun queryNominatimApi(lat: Double, lon: Double): NominatimResp {
    val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    return client.get("https://nominatim.openstreetmap.org/reverse?lat=$lat&lon=$lon&format=json")
        .body()
}
