package com.bgnw.locationreminder.taginfo_api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class TagInfoElement(val key: String, val value: String, val count_all: Int)

@Serializable
data class TagInfoResponse(var data: List<TagInfoElement>)

suspend fun queryTagInfoApi(query: String): TagInfoResponse? {

    val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    return try {
        client.get("https://taginfo.openstreetmap.org/api/4/search/by_value?$query").body()
    } catch (e: Exception) {
        null
    }
}

suspend fun procGetSuggestionsFromKeyword(keyword: String): TagInfoResponse? {
    val query = "query=$keyword&page=1&rp=10&sortname=count_all&sortorder=desc"
    return queryTagInfoApi(query)
}
