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
import androidx.compose.ui.res.stringResource
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

@Composable
private fun tradeDisplayName(trade: String): String = when (trade) {
    "Electric and Plumbing" -> stringResource(R.string.electric_and_plumbing)
    "A/C Repair Services" -> stringResource(R.string.ac_repair_services)
    "Appliance Repair" -> stringResource(R.string.appliance_repair)
    "Cleaning Solution" -> stringResource(R.string.cleaning_solution)
    "Painting and Renovation" -> stringResource(R.string.painting_and_renovation)
    "Pest Control" -> stringResource(R.string.pest_control)
    "Electronics and Gadget Repair" -> stringResource(R.string.electronics_and_gadget_repair_trade)
    "Shifting" -> stringResource(R.string.shifting)
    "Other" -> stringResource(R.string.other_label)
    else -> trade
}

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
                contentDescription = stringResource(R.string.cd_back),
                modifier = Modifier
                    .size(24.dp)
                    .clickable { navController.popBackStack() }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(stringResource(R.string.professional_skills_title), fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(stringResource(R.string.tell_us_about_expertise_title), fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(
                stringResource(R.string.profile_visibility_hint),
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Trade Selection
            Text(stringResource(R.string.primary_trade_label), fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = if (selectedTrade.isBlank()) "" else tradeDisplayName(selectedTrade),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.select_main_trade_label)) },
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
                            text = { Text(tradeDisplayName(trade)) },
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
                    label = { Text(stringResource(R.string.specify_your_trade_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = experienceYears,
                onValueChange = { if (it.all { char -> char.isDigit() }) experienceYears = it },
                label = { Text(stringResource(R.string.years_of_experience_label)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = hourlyRate,
                onValueChange = { if (it.all { char -> char.isDigit() }) hourlyRate = it },
                label = { Text(stringResource(R.string.expected_hourly_rate_label)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = skillDescription,
                onValueChange = { skillDescription = it },
                label = { Text(stringResource(R.string.short_bio_label)) },
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
                                navController.navigate("handymanKYCLanding")
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, context.getString(R.string.failed_to_save_skills_message), Toast.LENGTH_SHORT).show()
                            }
                    }
                },
                enabled = isFormValid,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D2E5E)),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(stringResource(R.string.continue_to_verification_btn), color = Color.White, fontSize = 16.sp)
            }
        }
    }
}
