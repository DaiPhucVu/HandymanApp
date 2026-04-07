package com.example.handyman.handyman_pages

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.handyman.R
import com.example.handyman.utils.SessionManager
import com.google.firebase.database.FirebaseDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HandymanEditProfile(navController: NavController) {
    val context = LocalContext.current
    val database = FirebaseDatabase.getInstance().getReference("Handyman")
    val userId = SessionManager.getLoggedInUserId(context)
    val scrollState = rememberScrollState()

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var hourlyRate by remember { mutableStateOf("") }
    var experienceYears by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(userId) {
        if (userId != null) {
            database.child(userId).get().addOnSuccessListener { snapshot ->
                val data = snapshot.value as? Map<*, *>
                firstName = data?.get("firstName") as? String ?: ""
                lastName = data?.get("lastName") as? String ?: ""
                bio = data?.get("bio") as? String ?: ""
                hourlyRate = data?.get("hourlyRate")?.toString() ?: ""
                experienceYears = data?.get("experienceYears")?.toString() ?: ""
                isLoading = false
            }
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .systemBarsPadding()
                .imePadding()
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                IconButton(onClick = { 
                    if (!navController.popBackStack()) {
                        (context as? android.app.Activity)?.finish()
                    }
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.arrow_back),
                        contentDescription = "Back",
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text("Edit Profile", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp)
            ) {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = hourlyRate,
                    onValueChange = { if (it.all { char -> char.isDigit() }) hourlyRate = it },
                    label = { Text("Hourly Rate (৳)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = experienceYears,
                    onValueChange = { if (it.all { char -> char.isDigit() }) experienceYears = it },
                    label = { Text("Years of Experience") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio") },
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    maxLines = 5
                )
                
                Spacer(modifier = Modifier.height(32.dp))
            }

            // Save Button
            Button(
                onClick = {
                    if (userId != null) {
                        val updates = mapOf(
                            "firstName" to firstName,
                            "lastName" to lastName,
                            "bio" to bio,
                            "hourlyRate" to hourlyRate,
                            "experienceYears" to experienceYears
                        )
                        database.child(userId).updateChildren(updates)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Failed to update profile", Toast.LENGTH_SHORT).show()
                            }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D2E5E)),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text("Save Changes", color = Color.White, fontSize = 16.sp)
            }
        }
    }
}
