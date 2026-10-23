package com.example.handyman

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

/**
 * Holds customer signup data across the Signup -> Address -> Phone -> OTP steps.
 * Nothing is written to the database until OTP verification succeeds, so an
 * account only exists once the phone number has actually been verified.
 */
class CustomerSignupViewModel : ViewModel() {
    // Step: Signup
    var firstName by mutableStateOf("")
    var lastName by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("")

    // Step: Address
    var houseNumber by mutableStateOf("")
    var street by mutableStateOf("")
    var area by mutableStateOf("")
    var postcode by mutableStateOf("")
    var division by mutableStateOf("")
    var district by mutableStateOf("")
    var thana by mutableStateOf("")
    var city by mutableStateOf("")
    var country by mutableStateOf("")
    var notes by mutableStateOf("")
    var latitude by mutableStateOf(0.0)
    var longitude by mutableStateOf(0.0)

    // Step: Phone
    var phoneNumber by mutableStateOf("")

    fun clear() {
        firstName = ""
        lastName = ""
        email = ""
        password = ""
        houseNumber = ""
        street = ""
        area = ""
        postcode = ""
        division = ""
        district = ""
        thana = ""
        city = ""
        country = ""
        notes = ""
        latitude = 0.0
        longitude = 0.0
        phoneNumber = ""
    }
}
