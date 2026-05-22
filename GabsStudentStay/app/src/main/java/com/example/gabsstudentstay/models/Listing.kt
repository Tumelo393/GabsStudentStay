package com.example.gabsstudentstay.models

data class Listing(
    val id: String = "",
    val title: String = "",
    val price: Double = 0.0,
    val depositAmount: Double = 0.0,
    val rooms: Int = 1,
    val location: String = "",
    val houseType: String = "", // e.g., Studio, Apartment, Room
    val amenities: String = "", // e.g., WiFi, AC, Parking
    val availabilityDate: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val landlordId: String = "",
    val status: String = "Available" // Available, Reserved
)
