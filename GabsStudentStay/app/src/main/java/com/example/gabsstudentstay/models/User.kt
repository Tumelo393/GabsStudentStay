package com.example.gabsstudentstay.models

data class User(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "student", // student, provider
    val preferences: Map<String, Any>? = null // for notifications: location, maxPrice
)
