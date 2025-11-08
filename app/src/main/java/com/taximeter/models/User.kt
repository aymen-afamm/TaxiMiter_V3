package com.taximeter.app.models

data class User(
    val id: String,
    val username: String,
    val password: String,
    val userType: UserType,
    val profile: UserProfile
)

enum class UserType {
    DRIVER, CLIENT, ADMIN
}

data class UserProfile(
    val name: String,
    val firstName: String,
    val age: Int,
    val licenseType: String? = null,
    val phone: String,
    val photoUrl: String? = null
)