package com.example.myapp.data



class UserDTO {
    data class UserProfileResponse(
        val id: Long,
        val email: String,
        val name: String,
        val phone: String,
        val createdAt: String
    )

    data class UserUpdateRequest(
        val name: String,
        val phone: String
    )
}
