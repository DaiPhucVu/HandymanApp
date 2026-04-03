package com.example.handyman.handyman_pages

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.*

@Composable
fun HandymanKYCCertificates(modifier: Modifier = Modifier, navController: NavController) {
    val context = LocalContext.current
    val certificateTypes = listOf(
        "Professional Licenses",
        "Training Certifications",
        "Police Check",
        "Reference Letter",
        "Other"
    )

    var selectedType1 by remember { mutableStateOf("") }
    var selectedUri1 by remember { mutableStateOf<Uri?>(null) }

    var selectedType2 by remember { mutableStateOf("") }
    var selectedUri2 by remember { mutableStateOf<Uri?>(null) }

    val isFormValid = selectedUri1 != null

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (selectedUri1 == null) selectedUri1 = uri else selectedUri2 = uri
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.arrow_back),
                contentDescription = "Back",
                modifier = Modifier
                    .size(40.dp)
                    .clickable { navController.popBackStack() }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Account verification", fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

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
        Text("Professional Document", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Please upload your handyman certification or additional document to help verify your skills",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Document 1
        Text("Document 1")
        DropdownMenuBox(
            selectedOption = selectedType1,
            onOptionSelected = { selectedType1 = it },
            options = certificateTypes
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (selectedUri1 == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color(0xFFE8E8E8), RoundedCornerShape(8.dp))
                    .clickable { filePickerLauncher.launch("*/*") },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Upload document (.pdf, .jpeg, .jpg)",
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp)) // space between text and icon
                    Icon(
                        painter = painterResource(id = R.drawable.upload_icon),
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = Color.Black
                    )
                }
            }

        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(selectedUri1!!.lastPathSegment ?: "Document 1 selected")
                Icon(
                    painter = painterResource(id = R.drawable.cancel_icon),
                    contentDescription = "Remove",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { selectedUri1 = null; selectedType1 = "" }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Document 2
        if (selectedUri1 != null) {
            Text("Document 2")
            DropdownMenuBox(
                selectedOption = selectedType2,
                onOptionSelected = { selectedType2 = it },
                options = certificateTypes
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (selectedUri2 == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(Color(0xFFE8E8E8), RoundedCornerShape(8.dp))
                        .clickable { filePickerLauncher.launch("*/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Upload document (.pdf, .jpeg, .jpg)")
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(selectedUri2!!.lastPathSegment ?: "Document 2 selected")
                    Icon(
                        painter = painterResource(id = R.drawable.cancel_icon),
                        contentDescription = "Remove",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { selectedUri2 = null; selectedType2 = "" }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Upload button
        Button(
            onClick = {
                val email = SessionManager.getLoggedInEmail(context)
                val dbRef = FirebaseDatabase.getInstance().getReference("Handyman")
                val query = dbRef.orderByChild("email").equalTo(email)

                query.get().addOnSuccessListener { snapshot ->
                    val storage = FirebaseStorage.getInstance().reference
                    val urls = mutableListOf<String>()

                    val uploadTasks = listOfNotNull(
                        selectedUri1?.let {
                            val fileName = "handyman_certificates/${UUID.randomUUID()}_${it.lastPathSegment}"
                            val ref = storage.child(fileName)
                            ref.putFile(it).continueWithTask { task -> ref.downloadUrl }
                        },
                        selectedUri2?.let {
                            val fileName = "handyman_certificates/${UUID.randomUUID()}_${it.lastPathSegment}"
                            val ref = storage.child(fileName)
                            ref.putFile(it).continueWithTask { task -> ref.downloadUrl }
                        }
                    )

                    // Wait for all uploads
                    Tasks.whenAllSuccess<Uri>(uploadTasks).addOnSuccessListener { uriList ->
                        uriList.mapTo(urls) { it.toString() }
                        for (child in snapshot.children) {
                            child.ref.child("certificates").setValue(urls.joinToString(","))
                                .addOnSuccessListener {
                                    navController.navigate("handymanKYCAddressForm")
                                }
                        }
                    }
                }
            },
            enabled = isFormValid,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isFormValid) Color(0xFF30386D) else Color(0xFFB0B0B0)
            )
        ) {
            Text("Upload document", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Skip
        Text(
            "Skip this step",
            color = Color.Gray,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clickable {
                    navController.navigate("handymanKYCAddressForm")
                }
        )
    }
}

@Composable
fun DropdownMenuBox(
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    options: List<String>
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier
        .fillMaxWidth()
        .background(Color.White)
        .clickable { expanded = true }
        .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = selectedOption.ifEmpty { "Select document type" })
            Icon(
                painter = painterResource(id = R.drawable.dropdown_icon),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { label ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onOptionSelected(label)
                        expanded = false
                    }
                )
            }
        }
    }
}
