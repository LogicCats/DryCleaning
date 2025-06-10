package com.example.myapp.data


class AuthDTO {
    data class RegisterRequest(
        val email: String,
        val password: String,
        val name: String,
        val phone: String
    )

    data class LoginRequest(
        val email: String,
        val password: String
    )

    data class AuthResponse(
        val token: String,
        val tokenType: String = "Bearer"
    )
}
