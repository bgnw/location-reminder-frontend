package com.bgnw.locationreminder.api

import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink

class CustomBody(private val json: String) : RequestBody() {

    override fun contentType(): MediaType? {
        val x = "application/json".toMediaTypeOrNull()
        return x
    }

    override fun writeTo(sink: BufferedSink) {
        sink.writeUtf8(json)
    }
}