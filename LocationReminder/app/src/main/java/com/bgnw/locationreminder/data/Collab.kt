package com.bgnw.locationreminder.data

import com.google.gson.annotations.Expose

data class Collab(
    @Expose(serialize = false, deserialize = true)
    var collab_id: Int?,
    var user_master: String,
    var user_peer: String,
)