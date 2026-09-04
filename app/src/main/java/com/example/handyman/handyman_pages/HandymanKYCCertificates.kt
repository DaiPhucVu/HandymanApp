package com.example.handyman.handyman_pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.handyman.R
import com.example.handyman.components.DividerLine
import com.example.handyman.components.StepCircle
import com.example.handyman.utils.SessionManager
import com.google.firebase.database.FirebaseDatabase

/** Bangladesh NIDs are issued in these lengths (old 13/17-digit and current 10-digit). */
private val VALID_NID_LENGTHS = setOf(10, 13, 17)

private fun isValidNid(nid: String) = nid.length in VALID_NID_LENGTHS && nid.all { it.isDigit() }

/**
 * KYC step 3 — National ID number.
 *
 * This step used to ask handymen to photograph their certificates, which stored
 * a Firebase Storage URL. A URL is unusable in the admin dashboard's CSV/Excel
 * export — it cannot be sorted, searched or cross-checked — so the step now
 * collects the NID as a typed number instead.
 *
 * The value is written to `nid`. The approval status stays on the existing
 * `certificateApprovedStatus` key so the dashboard's approve/reject flow for
 * this step keeps working without a change on that side.
 */
@Composable
fun HandymanKYCCertificates(modifier: Modifier = Modifier, navController: NavController) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var isSaving by remember { mutableStateOf(false) }

    var nid by remember { mutableStateOf("") }
    // Only complain once they have started typing, not on an empty field.
    val showError = nid.isNotEmpty() && !isValidNid(nid)
    val isFormValid = isValidNid(nid)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .imePadding()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Header
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

            // Step indicator
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
                DividerLine()
                StepCircle(stepNumber = 4, isActive = false)
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text("National ID", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Enter your National ID (NID) number so we can verify who you are. " +
                    "No photo needed — just the number on your card.",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text("NID Number", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = nid,
                onValueChange = { input ->
                    // Strip anything that is not a digit as it is typed, so spaces
                    // and dashes copied off a card cannot reach the database.
                    nid = input.filter { it.isDigit() }.take(17)
                },
                placeholder = { Text("e.g. 1234567890") },
                singleLine = true,
                isError = showError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (showError) {
                    "An NID number is 10, 13 or 17 digits. You have entered ${nid.length}."
                } else {
                    "10, 13 or 17 digits, as printed on your NID card."
                },
                fontSize = 12.sp,
                color = if (showError) MaterialTheme.colorScheme.error else Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Action Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    isSaving = true
                    val email = SessionManager.getLoggedInEmail(context)
                    val dbRef = FirebaseDatabase.getInstance().getReference("Handyman")
                    val query = dbRef.orderByChild("email").equalTo(email)

                    query.get().addOnSuccessListener { snapshot ->
                        val updateMap = mapOf(
                            "nid" to nid,
                            "certificateApprovedStatus" to "pending"
                        )

                        for (child in snapshot.children) {
                            child.ref.updateChildren(updateMap)
                                .addOnSuccessListener {
                                    isSaving = false
                                    navController.navigate("handymanKYCAddressForm")
                                }
                                .addOnFailureListener { isSaving = false }
                        }
                    }.addOnFailureListener {
                        isSaving = false
                    }
                },
                enabled = isFormValid && !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFormValid) Color(0xFF30386D) else Color(0xFFB0B0B0)
                )
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text(
                        "Submit",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "Skip this step",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier
                    .clickable(enabled = !isSaving) {
                        val email = SessionManager.getLoggedInEmail(context)
                        val dbRef = FirebaseDatabase.getInstance().getReference("Handyman")
                        val query = dbRef.orderByChild("email").equalTo(email)
                        query.get().addOnSuccessListener { snapshot ->
                            for (child in snapshot.children) {
                                child.ref.child("professionalCertificate").setValue("skipped")
                                child.ref.child("certificateApprovedStatus").setValue("not_provided")
                            }
                            navController.navigate("handymanKYCAddressForm")
                        }.addOnFailureListener {
                            navController.navigate("handymanKYCAddressForm")
                        }
                    }
                    .padding(8.dp)
            )
        }
    }
}
