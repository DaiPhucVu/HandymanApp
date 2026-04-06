package com.example.handyman.handyman_pages

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HandymanSkillsScreen(navController: NavController) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val database = FirebaseDatabase.getInstance().getReference("Handyman")
    val userId = SessionManager.getLoggedInUserId(context)

    var selectedTrade by remember { mutableStateOf("") }
    var otherTrade by remember { mutableStateOf("") }
    val trades = listOf(
        "Electric and Plumbing",
        "A/C Repair Services",
        "Appliance Repair",
        "Cleaning Solution",
        "Painting and Renovation",
        "Pest Control",
        "Electronics and Gadget Repair",
        "Shifting",
        "Other"
    )

    var skillDescription by remember { mutableStateOf("") }
    var experienceYears by remember { mutableStateOf("") }
    var hourlyRate by remember { mutableStateOf("") }

    val finalTrade = if (selectedTrade == "Other") otherTrade else selectedTrade
    val isFormValid = finalTrade.isNotBlank() && experienceYears.isNotEmpty() && hourlyRate.isNotEmpty()

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
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.arrow_back),
                contentDescription = "Back",
                modifier = Modifier
                    .size(24.dp)
                    .clickable { navController.popBackStack() }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text("Professional Skills", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text("Tell us about your expertise", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(
                "This information will be shown on your profile to help customers find you.",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Trade Selection
            Text("Primary Trade", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedTrade,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select your main trade") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    trades.forEach { trade ->
                        DropdownMenuItem(
                            text = { Text(trade) },
                            onClick = {
                                selectedTrade = trade
                                expanded = false
                            }
                        )
                    }
                }
            }

            if (selectedTrade == "Other") {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = otherTrade,
                    onValueChange = { otherTrade = it },
                    label = { Text("Specify your trade") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

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
                value = hourlyRate,
                onValueChange = { if (it.all { char -> char.isDigit() }) hourlyRate = it },
                label = { Text("Expected Hourly Rate (৳)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = skillDescription,
                onValueChange = { skillDescription = it },
                label = { Text("Short Bio / Description of Skills") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Bottom Button Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Button(
                onClick = {
                    if (userId != null) {
                        val updates = mapOf(
                            "primaryTrade" to finalTrade,
                            "experienceYears" to experienceYears,
                            "hourlyRate" to hourlyRate,
                            "bio" to skillDescription,
                            "skills" to listOf(finalTrade)
                        )
                        database.child(userId).updateChildren(updates)
                            .addOnSuccessListener {
                                navController.navigate("handymanProfile")
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Failed to save skills", Toast.LENGTH_SHORT).show()
                            }
                    }
                },
                enabled = isFormValid,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D2E5E)),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text("Continue to Verification", color = Color.White, fontSize = 16.sp)
            }
        }
    }
}
