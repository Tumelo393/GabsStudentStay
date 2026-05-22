package com.example.gabsstudentstay.models

data class Message(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val text: String = "",
    val timestamp: String = "",
    val isFromMe: Boolean = false
)
