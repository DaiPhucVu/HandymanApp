package com.example.handyman

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class JobPostingViewModel : ViewModel() {
    // Session Info
    var customerId by mutableStateOf("")

    // Selected Service
    var serviceCategory by mutableStateOf("")

    // Step 1: Description & Timing
    var problemDesc by mutableStateOf("")
    var dateFrom by mutableStateOf("")
    var dateTo by mutableStateOf("")
    var timeFrom by mutableStateOf("")
    var timeTo by mutableStateOf("")

    // Step 2: Location
    var locationAddress by mutableStateOf("")
    var latitude by mutableStateOf(0.0)
    var longitude by mutableStateOf(0.0)

    // Step 3: Salary
    var isHappyToNegotiate by mutableStateOf(false)
    var salaryMin by mutableStateOf("")
    var salaryMax by mutableStateOf("")
    var paymentOption by mutableStateOf("") // "Per Day" or "Job Completed"

    // Step 4: Photos
    var imageUris by mutableStateOf<List<Uri>>(emptyList())

    // Navigation State
    var isEditing by mutableStateOf(false)

    fun clearData() {
        problemDesc = ""
        dateFrom = ""
        dateTo = ""
        timeFrom = ""
        timeTo = ""
        locationAddress = ""
        isHappyToNegotiate = false
        salaryMin = ""
        salaryMax = ""
        paymentOption = ""
        imageUris = emptyList()
    }
}
