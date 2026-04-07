package com.example.handyman.customer_pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.handyman.R
import com.example.handyman.components.DividerLine
import com.example.handyman.components.StepCircle
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import com.example.handyman.utils.SessionManager
import com.google.firebase.database.FirebaseDatabase
import android.widget.Toast


@Composable
fun CustomerKYCAddressForm(modifier: Modifier = Modifier, navController: NavController) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

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

    // Validation rules (Relaxed to allow numbers, dots, commas, and hyphens)
    val isValidHouseNumber = houseNumber.trim().isNotBlank() && houseNumber.trim().length <= 20
    val isValidStreet = street.trim().isNotBlank() && street.trim().length <= 100
    val isValidArea = area.trim().isNotBlank() && area.trim().length <= 50
    val isValidPostCode = postCode.trim().matches(Regex("^\\d{4,10}$"))
    val isValidDivision = division.trim().isNotBlank() && division.trim().length <= 50
    val isValidDistrict = district.trim().isNotBlank() && district.trim().length <= 50
    val isValidThana = thana.trim().isNotBlank() && thana.trim().length <= 50
    val isValidCity = city.trim().isNotBlank() && city.trim().length <= 50
    val isValidCountry = country.trim().isNotBlank() && country.trim().length <= 50

    val allFieldsFilled = houseNumber.trim().isNotBlank() && street.trim().isNotBlank() && area.trim().isNotBlank() &&
            postCode.trim().isNotBlank() && division.trim().isNotBlank() && district.trim().isNotBlank() &&
            thana.trim().isNotBlank() && city.trim().isNotBlank() && country.trim().isNotBlank()

    val isFormComplete = allFieldsFilled && isValidHouseNumber && isValidStreet && isValidArea &&
            isValidPostCode && isValidDivision && isValidDistrict && isValidThana && isValidCity && isValidCountry

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .imePadding()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.arrow_back),
                contentDescription = "Back",
                modifier = Modifier
                    .size(24.dp)
                    .clickable { navController.popBackStack() }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Account verification", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                StepCircle(stepNumber = 1, isActive = true)
                DividerLine()
                StepCircle(stepNumber = 2, isActive = true)
                DividerLine()
                StepCircle(stepNumber = 3, isActive = false)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text("Confirm your address", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Tell us you live so we can bring our excellent service straight to your home.",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

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

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = area,
                    onValueChange = { area = it },
                    label = { Text("Neighborhood") },
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

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
        }

        Button(
            onClick = {
                val userId = SessionManager.currentUserID ?: SessionManager.getLoggedInUserId(context)
                if (userId.isBlank()) {
                    Log.e("KYC", "No logged-in user ID found")
                    Toast.makeText(context, "Session error. Please log in again.", Toast.LENGTH_LONG).show()
                    return@Button
                }

                val addressData = mapOf(
                    "houseNumber" to houseNumber.trim(),
                    "street" to street.trim(),
                    "area" to area.trim(),
                    "postcode" to postCode.trim(),
                    "division" to division.trim(),
                    "district" to district.trim(),
                    "thana" to thana.trim(),
                    "city" to city.trim(),
                    "country" to country.trim(),
                    "notes" to note.trim(),
                    "kycStatus" to "AddressSubmitted"
                )

                FirebaseDatabase.getInstance().getReference("User")
                    .child(userId)
                    .updateChildren(addressData)
                    .addOnSuccessListener {
                        SessionManager.saveLoggedInCity(context, city.trim())
                        navController.navigate("customerKycPhoneNumber")
                    }
                    .addOnFailureListener { error ->
                        Log.e("KYC", "Failed to update address: ${error.message}")
                        Toast.makeText(context, "Failed to save address. Please try again.", Toast.LENGTH_LONG).show()
                    }
            },
            enabled = isFormComplete,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isFormComplete) Color(0xFFFFB703) else Color(0xFFB0B0B0)
            )
        ) {
            Text("Submit address", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
        }
    }
}
