package com.example.handyman.handyman_pages

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.handyman.R
import com.example.handyman.components.DividerLine
import com.example.handyman.components.StepCircle
import com.example.handyman.utils.SessionManager
import com.google.firebase.database.FirebaseDatabase

@Composable
fun HandymanKYCAddressForm(modifier: Modifier = Modifier, navController: NavController) {
    val context = LocalContext.current

    val textFieldModifier = Modifier
        .fillMaxWidth()
        .height(56.dp)

    var houseNumber by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var postCode by remember { mutableStateOf("") }
    var division by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var thana by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("Bangladesh") }
    var note by remember { mutableStateOf("") }

    // Validations
    val isValidHouseNumber = houseNumber.matches(Regex("^[a-zA-Z0-9\\- ]{1,10}$"))
    val isValidStreet = street.length in 1..50
    val isValidArea = area.matches(Regex("^[a-zA-Z ]{1,30}$"))
    val isValidPostCode = postCode.matches(Regex("^\\d{4,6}$"))
    val isValidDivision = division.matches(Regex("^[a-zA-Z ]{1,30}$"))
    val isValidDistrict = district.matches(Regex("^[a-zA-Z ]{1,30}$"))
    val isValidThana = thana.matches(Regex("^[a-zA-Z ]{1,30}$"))
    val isValidCity = city.matches(Regex("^[a-zA-Z ]{1,30}$"))
    val isValidCountry = country.matches(Regex("^[a-zA-Z ]{1,30}$"))

    val allFieldsFilled = listOf(
        houseNumber, street, area, postCode, division,
        district, thana, city, country
    ).all { it.isNotBlank() }

    val isFormComplete = allFieldsFilled &&
            isValidHouseNumber && isValidStreet && isValidArea &&
            isValidPostCode && isValidDivision && isValidDistrict &&
            isValidThana && isValidCity && isValidCountry

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
    ) {
        // Top Bar
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.arrow_back),
                contentDescription = "Back",
                modifier = Modifier
                    .size(24.dp)
                    .clickable { navController.navigate("handymanKycCaptureID") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Account verification", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Step UI
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            StepCircle(stepNumber = 1, isActive = true)
            DividerLine()
            StepCircle(stepNumber = 2, isActive = true)
            DividerLine()
            StepCircle(stepNumber = 3, isActive = true)
            DividerLine()
            StepCircle(stepNumber = 4, isActive = false)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Header
        Text("Confirm your address", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Tell us you live so we can bring our excellent service straight to your home.",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Input Fields
        OutlinedTextField(
            value = houseNumber,
            onValueChange = { houseNumber = it },
            label = { Text("House number") },
            modifier = textFieldModifier,
            isError = houseNumber.isNotBlank() && !isValidHouseNumber
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = street,
            onValueChange = { street = it },
            label = { Text("Street") },
            modifier = textFieldModifier,
            isError = street.isNotBlank() && !isValidStreet
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = area,
                onValueChange = { area = it },
                label = { Text("Area/Neighborhood") },
                modifier = Modifier.weight(1f),
                isError = area.isNotBlank() && !isValidArea
            )
            OutlinedTextField(
                value = postCode,
                onValueChange = { postCode = it },
                label = { Text("Post code") },
                modifier = Modifier.weight(1f),
                isError = postCode.isNotBlank() && !isValidPostCode
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = division,
                onValueChange = { division = it },
                label = { Text("Division") },
                modifier = Modifier.weight(1f),
                isError = division.isNotBlank() && !isValidDivision
            )
            OutlinedTextField(
                value = district,
                onValueChange = { district = it },
                label = { Text("District") },
                modifier = Modifier.weight(1f),
                isError = district.isNotBlank() && !isValidDistrict
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = thana,
                onValueChange = { thana = it },
                label = { Text("Thana") },
                modifier = Modifier.weight(1f),
                isError = thana.isNotBlank() && !isValidThana
            )
            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("City") },
                modifier = Modifier.weight(1f),
                isError = city.isNotBlank() && !isValidCity
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = country,
            onValueChange = { country = it },
            label = { Text("Country") },
            modifier = textFieldModifier,
            isError = country.isNotBlank() && !isValidCountry
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Additional note (optional)") },
            modifier = textFieldModifier
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val currentEmail = SessionManager.getLoggedInEmail(context)
                val handymanRef = FirebaseDatabase.getInstance().getReference("Handyman")
                val query = handymanRef.orderByChild("email").equalTo(currentEmail)

                val addressData = mapOf(
                    "houseNumber" to houseNumber,
                    "street" to street,
                    "area" to area,
                    "postCode" to postCode,
                    "division" to division,
                    "district" to district,
                    "thana" to thana,
                    "city" to city,
                    "country" to country,
                    "notes" to note
                )

                query.get().addOnSuccessListener { snapshot ->
                    for (child in snapshot.children) {
                        child.ref.updateChildren(addressData)
                            .addOnSuccessListener {
                                navController.navigate("handymanKYCPhoneNumber")
                            }
                            .addOnFailureListener { e ->
                                Log.e("KYC", "Failed to save address: ${e.message}")
                            }
                    }
                    if (!snapshot.exists()) {
                        Log.e("KYC", "No handyman found for email: $currentEmail")
                    }
                }.addOnFailureListener { e ->
                    Log.e("KYC", "Query error: ${e.message}")
                }
            },
            enabled = isFormComplete,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isFormComplete) Color(0xFF2F3367) else Color(0xFFCCCCCC)
            )
        ) {
            Text("Submit address", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}
