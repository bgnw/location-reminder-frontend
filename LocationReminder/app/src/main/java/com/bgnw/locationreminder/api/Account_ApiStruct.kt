package com.bgnw.locationreminder.api

data class Account_ApiStruct (
    var username: String,
    var display_name: String,
    var password: String,
    var biography: String,
    var profile_img_path: String
) {
    override fun toString(): String {
        return """
            {
                "username": "$username",
                "display_name": "$display_name",
                "password": "$password",
                "biography": "$biography",
                "profile_img_path": "$profile_img_path"
            }
        """.trimIndent()
    }
}
