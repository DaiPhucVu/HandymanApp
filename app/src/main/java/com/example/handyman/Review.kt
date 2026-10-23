package com.example.handyman

data class Review(
    val reviewId: String = "",
    val jobId: String = "",
    val customerId: String = "",
    val handymanId: String = "",
    val rating: Float = 0f,
    val comment: String = "",
    val timestamp: String = "",
    val reviewerType: String = "customer" // "customer" or "handyman"
)
