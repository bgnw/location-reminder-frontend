package com.bgnw.locationreminder.api

data class AuthResponse(
    var authentication_success: Boolean,
) {
    override fun toString(): String {
        return """
            {
                "authentication_success": "$authentication_success",
            }
        """.trimIndent()
    }
}
