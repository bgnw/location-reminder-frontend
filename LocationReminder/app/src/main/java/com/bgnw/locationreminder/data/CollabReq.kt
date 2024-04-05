package com.bgnw.locationreminder.data

import com.google.gson.annotations.Expose

data class CollabReq(
    @Expose(serialize = false, deserialize = true)
    var request_id: Int?,
    @Expose(serialize = false, deserialize = true)
    var datetime_sent: String?,
    var user_sender: String,
    var user_recipient: String,
)