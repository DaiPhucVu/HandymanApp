package com.example.handyman.handyman_pages

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
    val scrollState = rememberScrollState()
    var isUploading by remember { mutableStateOf(false) }

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

    val isFormValid = selectedUri1 != null && selectedType1.isNotEmpty() && (selectedUri2 == null || selectedType2.isNotEmpty())

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (selectedUri1 == null) selectedUri1 = uri else selectedUri2 = uri
    }

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
            Text("Professional Document", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Please upload your handyman certification or additional document to help verify your skills",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Document 1
            DocumentUploadSection(
                title = "Main Document",
                selectedType = selectedType1,
                selectedUri = selectedUri1,
                onTypeSelected = { selectedType1 = it },
                onFileClick = { filePickerLauncher.launch("*/*") },
                onRemove = {
                    selectedUri1 = null
                    selectedType1 = ""
                },
                options = certificateTypes
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Document 2 (Only if first is uploaded)
            if (selectedUri1 != null) {
                DocumentUploadSection(
                    title = "Additional Document (Optional)",
                    selectedType = selectedType2,
                    selectedUri = selectedUri2,
                    onTypeSelected = { selectedType2 = it },
                    onFileClick = { filePickerLauncher.launch("*/*") },
                    onRemove = {
                        selectedUri2 = null
                        selectedType2 = ""
                    },
                    options = certificateTypes
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Action Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    isUploading = true
                    val email = SessionManager.getLoggedInEmail(context)
                    val dbRef = FirebaseDatabase.getInstance().getReference("Handyman")
                    val query = dbRef.orderByChild("email").equalTo(email)

                    query.get().addOnSuccessListener { snapshot ->
                        val storage = FirebaseStorage.getInstance().reference
                        val urls = mutableListOf<String>()

                        val uploadTasks = listOfNotNull(
                            selectedUri1?.let {
                                val fileName =
                                    "handyman_certificates/${UUID.randomUUID()}_${it.lastPathSegment}"
                                val ref = storage.child(fileName)
                                ref.putFile(it).continueWithTask { task -> ref.downloadUrl }
                            },
                            selectedUri2?.let {
                                val fileName =
                                    "handyman_certificates/${UUID.randomUUID()}_${it.lastPathSegment}"
                                val ref = storage.child(fileName)
                                ref.putFile(it).continueWithTask { task -> ref.downloadUrl }
                            }
                        )

                        // Wait for all uploads
                        Tasks.whenAllSuccess<Uri>(uploadTasks).addOnSuccessListener { uriList ->
                            val urlStrings = uriList.map { it.toString() }
                            val combinedUrls = urlStrings.joinToString(",")
                            
                            val updateMap = mapOf(
                                "certificates" to combinedUrls,
                                "certificateApprovedStatus" to "pending",
                                "certificateType1" to selectedType1,
                                "certificateType2" to selectedType2
                            )

                            for (child in snapshot.children) {
                                child.ref.updateChildren(updateMap)
                                    .addOnSuccessListener {
                                        isUploading = false
                                        navController.navigate("handymanKYCAddressForm")
                                    }
                            }
                        }.addOnFailureListener {
                            isUploading = false
                        }
                    }.addOnFailureListener {
                        isUploading = false
                    }
                },
                enabled = isFormValid && !isUploading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFormValid) Color(0xFF30386D) else Color(0xFFB0B0B0)
                )
            ) {
                if (isUploading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text(
                        "Submit Documents",
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
                    .clickable(enabled = !isUploading) {
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

@Composable
fun DocumentUploadSection(
    title: String,
    selectedType: String,
    selectedUri: Uri?,
    onTypeSelected: (String) -> Unit,
    onFileClick: () -> Unit,
    onRemove: () -> Unit,
    options: List<String>
) {
    Column {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))

        DropdownMenuBox(
            selectedOption = selectedType,
            onOptionSelected = onTypeSelected,
            options = options
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (selectedUri == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color(0xFFF8F9FA), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFE9ECEF), RoundedCornerShape(12.dp))
                    .clickable { onFileClick() },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(id = R.drawable.upload_icon),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = Color(0xFF30386D)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Tap to upload document",
                        color = Color(0xFF30386D),
                        fontWeight = FontWeight.Medium
                    )
                    Text("PDF, JPEG, JPG (Max 5MB)", color = Color.Gray, fontSize = 12.sp)
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F3F5)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.document_icon_yellow),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = Color.Unspecified
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = selectedUri.lastPathSegment?.take(25) ?: "Selected Document",
                            maxLines = 1,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    IconButton(onClick = onRemove) {
                        Icon(
                            painter = painterResource(id = R.drawable.cancel_icon),
                            contentDescription = "Remove",
                            modifier = Modifier.size(20.dp),
                            tint = Color.Unspecified
                        )
                    }
                }
            }
        }
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
        .border(1.dp, Color(0xFFE9ECEF), RoundedCornerShape(12.dp))
        .background(Color.White, RoundedCornerShape(12.dp))
        .clickable { expanded = true }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedOption.ifEmpty { "Select document type" },
                color = if (selectedOption.isEmpty()) Color.Gray else Color.Black
            )
            Icon(
                painter = painterResource(id = R.drawable.dropdown_icon),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = Color.Gray
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
