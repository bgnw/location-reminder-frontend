package com.bgnw.locationreminder.data

data class Account(
    var username: String,
    var display_name: String,
    var password: String,
    var biography: String,
    var profile_img_path: String,
    var lati: Double?,
    var longi: Double?
) {
    override fun toString(): String {
        return """
            {
                "username": "$username",
                "display_name": "$display_name",
                "password": "$password",
                "biography": "$biography",
                "profile_img_path": "$profile_img_path"
                "lati": "$lati"
                "longi": "$longi"
            }
        """.trimIndent()
    }
}
