package com.example.handyman

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

/**
 * Holds handyman signup data across the Signup -> Skills -> ID capture ->
 * Certificates -> Address -> Phone -> OTP steps. Nothing is written to the
 * database until OTP verification succeeds, so an account only exists once
 * the phone number has actually been verified.
 */
class HandymanSignupViewModel : ViewModel() {
    // Step: Signup
    var firstName by mutableStateOf("")
    var lastName by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("")

    // Step: Skills
    var primaryTrade by mutableStateOf("")
    var experienceYears by mutableStateOf("")
    var hourlyRate by mutableStateOf("")
    var bio by mutableStateOf("")

    // Step: ID capture (uploaded to Storage immediately; only the resulting URL is held here)
    var photoIdCard by mutableStateOf("")

    // Step: Certificates / NID
    var nid by mutableStateOf("")
    var certificateApprovedStatus by mutableStateOf("")
    var professionalCertificate by mutableStateOf("")

    // Step: Address
    var houseNumber by mutableStateOf("")
    var street by mutableStateOf("")
    var area by mutableStateOf("")
    var postCode by mutableStateOf("")
    var division by mutableStateOf("")
    var district by mutableStateOf("")
    var thana by mutableStateOf("")
    var city by mutableStateOf("")
    var country by mutableStateOf("")
    var notes by mutableStateOf("")

    // Step: Phone
    var phoneNumber by mutableStateOf("")

    fun clear() {
        firstName = ""
        lastName = ""
        email = ""
        password = ""
        primaryTrade = ""
        experienceYears = ""
        hourlyRate = ""
        bio = ""
        photoIdCard = ""
        nid = ""
        certificateApprovedStatus = ""
        professionalCertificate = ""
        houseNumber = ""
        street = ""
        area = ""
        postCode = ""
        division = ""
        district = ""
        thana = ""
        city = ""
        country = ""
        notes = ""
        phoneNumber = ""
    }
}
