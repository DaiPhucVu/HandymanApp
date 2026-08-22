package com.example.handyman

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Job(
    val jobId: String = "",
    val createdAt: String = "",
    val customerId: String = "",
    val postedBy: String? = null,    // Added for compatibility
    val postedAt: Any? = null,      // Added for compatibility
    val title: String? = null,       // Added for compatibility
    val description: String? = null, // Added for compatibility
    val location: String? = null,    // Added for compatibility
    val category: String? = null,    // Added for compatibility
    val status: String? = null,      // Added for compatibility
    val lastUpdated: Any? = null,    // Added for compatibility
    val jobCat: String = "",
    val jobDesc: String = "",
    val jobDateFrom: String = "",
    val jobDateTo: String = "",
    val jobTimeFrom: String = "",
    val jobTimeTo: String = "",
    val jobLocation: String = "",
    val citySuburb: String = "",
    val jobSalaryFrom: String = "",
    val jobSalaryTo: String = "",
    val jobPaymentOption: String = "",
    val paymentStatus: String = "",
    val handypay: String = "",
    val custpay: String = "",
    val imageUris: List<String> = emptyList(),
    val assignedTo: String = "",
    val jobStatus: String = "",
    val jobStatusCustomer : String? = null,
    val jobStatusHandyman : String? = null,
    val lastUpdate: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val finishedBy: String? = null,
    val assignedAt: Any? = null,
    val assignedBy: Any? = null,
    val assignment: Any? = null,
    val assignmentHistory: Any? = null,
    val isReviewedByCustomer: Boolean = false,
    val isReviewedByHandyman: Boolean = false
)
