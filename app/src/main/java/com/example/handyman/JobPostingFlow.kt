package com.example.handyman

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.google.firebase.database.FirebaseDatabase
import com.example.handyman.utils.SessionManager
import android.widget.Toast
import com.example.handyman.components.DividerLine
import com.example.handyman.components.StepCircle
import java.util.*
import coil.compose.AsyncImage
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.android.gms.tasks.Tasks
import java.time.LocalDateTime

@Composable
fun JobPostingProgressBar(currentStep: Int) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
    ) {
        StepCircle(stepNumber = 1, isActive = currentStep >= 1)
        DividerLine()
        StepCircle(stepNumber = 2, isActive = currentStep >= 2)
        DividerLine()
        StepCircle(stepNumber = 3, isActive = currentStep >= 3)
        DividerLine()
        StepCircle(stepNumber = 4, isActive = currentStep >= 4)
        DividerLine()
        StepCircle(stepNumber = 5, isActive = currentStep >= 5)
    }
}

@Composable
fun JobPostingTopBar(navController: NavController, title: String, onBack: () -> Unit = { navController.popBackStack() }) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Icon(
            painter = painterResource(id = R.drawable.arrow_back),
            contentDescription = "Back",
            modifier = Modifier.size(24.dp).clickable { onBack() }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun JobPostingDescriptionScreen(navController: NavController, viewModel: JobPostingViewModel) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    Column(modifier = Modifier.fillMaxSize().background(Color.White).statusBarsPadding().padding(horizontal = 24.dp).verticalScroll(rememberScrollState())) {
        Spacer(modifier = Modifier.height(16.dp))
        JobPostingTopBar(navController, "Describe your problem", onBack = {
            if (!navController.popBackStack()) {
                navController.navigate("customerHome")
            }
        })
        JobPostingProgressBar(currentStep = 1)

        Spacer(modifier = Modifier.height(16.dp))
        Text("Describe your problem", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        OutlinedTextField(
            value = viewModel.problemDesc,
            onValueChange = { viewModel.problemDesc = it },
            placeholder = { Text("ex. I have water leaks in the bathroom") },
            modifier = Modifier.fillMaxWidth().height(150.dp),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))
        
        // Date Section
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = viewModel.dateFrom,
                    onValueChange = {},
                    label = { Text("Start Date") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().clickable {
                        DatePickerDialog(context, { _, y, m, d ->
                            viewModel.dateFrom = String.format("%02d/%02d/%04d", d, m + 1, y)
                        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                    },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Color.Black, disabledBorderColor = Color.Gray, disabledLabelColor = Color.Gray)
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = viewModel.dateTo,
                    onValueChange = {},
                    label = { Text("End Date") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().clickable {
                        DatePickerDialog(context, { _, y, m, d ->
                            viewModel.dateTo = String.format("%02d/%02d/%04d", d, m + 1, y)
                        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                    },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Color.Black, disabledBorderColor = Color.Gray, disabledLabelColor = Color.Gray)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Time Section
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = viewModel.timeFrom,
                    onValueChange = {},
                    label = { Text("Start Time") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().clickable {
                        TimePickerDialog(context, { _, h, min ->
                            viewModel.timeFrom = String.format("%02d:%02d", h, min)
                        }, 10, 0, true).show()
                    },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Color.Black, disabledBorderColor = Color.Gray, disabledLabelColor = Color.Gray)
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = viewModel.timeTo,
                    onValueChange = {},
                    label = { Text("End Time") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().clickable {
                        TimePickerDialog(context, { _, h, min ->
                            viewModel.timeTo = String.format("%02d:%02d", h, min)
                        }, 17, 0, true).show()
                    },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Color.Black, disabledBorderColor = Color.Gray, disabledLabelColor = Color.Gray)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { 
                if (viewModel.isEditing) {
                    navController.navigate("jobPostingReview") {
                        popUpTo("jobPostingDescription") { inclusive = true }
                    }
                } else {
                    navController.navigate("jobPostingLocation")
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F3367)),
            enabled = viewModel.problemDesc.isNotBlank() && viewModel.dateFrom.isNotBlank() && viewModel.dateTo.isNotBlank()
        ) {
            Text("Continue", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun JobPostingLocationScreen(navController: NavController, viewModel: JobPostingViewModel) {
    Column(modifier = Modifier.fillMaxSize().background(Color.White).statusBarsPadding().padding(horizontal = 24.dp)) {
        Spacer(modifier = Modifier.height(16.dp))
        JobPostingTopBar(navController, "Location")
        JobPostingProgressBar(currentStep = 2)

        Spacer(modifier = Modifier.height(16.dp))
        Text("Where do you need help?", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        
        OutlinedTextField(
            value = viewModel.locationAddress,
            onValueChange = { viewModel.locationAddress = it },
            label = { Text("Enter your address") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))
        
        // Map Placeholder
        Box(
            modifier = Modifier.fillMaxWidth().height(300.dp).background(Color(0xFFEEEEEE), RoundedCornerShape(12.dp)).border(1.dp, Color.Gray, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(painter = painterResource(id = R.drawable.location), contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                Text("Map of Bangladesh will be here", color = Color.Gray)
                Text("(Pinpoint feature coming soon)", fontSize = 12.sp, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { 
                if (viewModel.isEditing) {
                    navController.navigate("jobPostingReview") {
                        popUpTo("jobPostingLocation") { inclusive = true }
                    }
                } else {
                    navController.navigate("jobPostingSalary")
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F3367)),
            enabled = viewModel.locationAddress.isNotBlank()
        ) {
            Text("Continue", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun JobPostingSalaryScreen(navController: NavController, viewModel: JobPostingViewModel) {
    Column(modifier = Modifier.fillMaxSize().background(Color.White).statusBarsPadding().padding(horizontal = 24.dp)) {
        Spacer(modifier = Modifier.height(16.dp))
        JobPostingTopBar(navController, "Salary Requirements")
        JobPostingProgressBar(currentStep = 3)

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { viewModel.isHappyToNegotiate = !viewModel.isHappyToNegotiate }) {
            Checkbox(checked = viewModel.isHappyToNegotiate, onCheckedChange = { viewModel.isHappyToNegotiate = it })
            Text("I'm happy to negotiate", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (!viewModel.isHappyToNegotiate) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = viewModel.salaryMin,
                    onValueChange = { viewModel.salaryMin = it },
                    label = { Text("Min BDT") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = viewModel.salaryMax,
                    onValueChange = { viewModel.salaryMax = it },
                    label = { Text("Max BDT") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Payment Frequency", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = viewModel.paymentOption == "Per Day", onClick = { viewModel.paymentOption = "Per Day" })
            Text("Per Day", modifier = Modifier.clickable { viewModel.paymentOption = "Per Day" })
            Spacer(modifier = Modifier.width(24.dp))
            RadioButton(selected = viewModel.paymentOption == "Job Completed", onClick = { viewModel.paymentOption = "Job Completed" })
            Text("Job Completed", modifier = Modifier.clickable { viewModel.paymentOption = "Job Completed" })
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { 
                if (viewModel.isEditing) {
                    navController.navigate("jobPostingReview") {
                        popUpTo("jobPostingSalary") { inclusive = true }
                    }
                } else {
                    navController.navigate("jobPostingPhotos")
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F3367))
        ) {
            Text("Continue", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun JobPostingPhotoScreen(navController: NavController, viewModel: JobPostingViewModel) {
    val context = LocalContext.current
    var tempUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        viewModel.imageUris = (viewModel.imageUris + uris).distinct()
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            tempUri?.let { uri ->
                viewModel.imageUris = (viewModel.imageUris + uri).distinct()
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.White).statusBarsPadding().padding(horizontal = 24.dp)) {
        Spacer(modifier = Modifier.height(16.dp))
        JobPostingTopBar(navController, "Add Photos")
        JobPostingProgressBar(currentStep = 4)

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Take Photo
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        val fragment = (context as? androidx.fragment.app.FragmentActivity)
                            ?.supportFragmentManager
                            ?.findFragmentById(R.id.fragment_container)
                            ?.childFragmentManager
                            ?.primaryNavigationFragment as? JobPostingFragment
                        
                        val uri = fragment?.createImageFileUri()
                        tempUri = uri
                        uri?.let { cameraLauncher.launch(it) }
                    },
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE9ECEF))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.camera),
                        contentDescription = "Take Photo",
                        modifier = Modifier.size(40.dp),
                        tint = Color(0xFF2F3367)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Take Photo", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }

            // Gallery
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { galleryLauncher.launch("image/*") },
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE9ECEF))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.folder),
                        contentDescription = "Gallery",
                        modifier = Modifier.size(40.dp),
                        tint = Color(0xFF2F3367)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("From Gallery", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (viewModel.imageUris.isNotEmpty()) {
            Text("Selected Photos", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(3),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.imageUris.size) { index ->
                    Box {
                        AsyncImage(
                            model = viewModel.imageUris[index],
                            contentDescription = null,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                                .padding(2.dp),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                        IconButton(
                            onClick = {
                                viewModel.imageUris = viewModel.imageUris.toMutableList().apply { removeAt(index) }
                            },
                            modifier = Modifier.align(Alignment.TopEnd).size(24.dp).background(Color.White.copy(alpha = 0.7f), androidx.compose.foundation.shape.CircleShape)
                        ) {
                            Icon(painter = painterResource(id = R.drawable.cancel), contentDescription = "Remove", modifier = Modifier.size(16.dp), tint = Color.Red)
                        }
                    }
                }
            }
        } else {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text("No photos added yet", color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { 
                if (viewModel.isEditing) {
                    navController.navigate("jobPostingReview") {
                        popUpTo("jobPostingPhotos") { inclusive = true }
                    }
                } else {
                    navController.navigate("jobPostingReview")
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F3367))
        ) {
            Text("Continue", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun JobPostingReviewScreen(navController: NavController, viewModel: JobPostingViewModel) {
    var showPopup by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Reset editing state when entering review
    LaunchedEffect(Unit) {
        viewModel.isEditing = false
    }

    fun submitJobToFirebase() {
        isSubmitting = true
        val database = FirebaseDatabase.getInstance()
        val dbRef = database.reference
        
        val jobId = UUID.randomUUID().toString()
        val customerId = viewModel.customerId.ifBlank { SessionManager.currentUserID ?: SessionManager.getLoggedInUserId(context) }
        
        val job = Job(
            jobId = jobId,
            createdAt = LocalDateTime.now().toString(),
            customerId = customerId,
            jobCat = viewModel.serviceCategory,
            jobDesc = viewModel.problemDesc,
            jobDateFrom = viewModel.dateFrom,
            jobDateTo = viewModel.dateTo,
            jobTimeFrom = viewModel.timeFrom,
            jobTimeTo = viewModel.timeTo,
            jobLocation = viewModel.locationAddress,
            jobSalaryFrom = viewModel.salaryMin,
            jobSalaryTo = viewModel.salaryMax,
            jobPaymentOption = viewModel.paymentOption,
            paymentStatus = "",
            imageUris = viewModel.imageUris.map { it.toString() }
        )

        val userPath = "User/$customerId"
        
        val updates = hashMapOf<String, Any>(
            "/Job/$jobId" to job,
            "$userPath/allJobs/${dbRef.child(userPath).child("allJobs").push().key!!}" to jobId,
            "$userPath/notAssignedJobs/${dbRef.child(userPath).child("notAssignedJobs").push().key!!}" to jobId
        )

        dbRef.updateChildren(updates)
            .addOnSuccessListener {
                // Handle Image Uploads
                if (viewModel.imageUris.isNotEmpty()) {
                    val uploadTasks = viewModel.imageUris.mapIndexed { idx, uri ->
                        val imageRef: StorageReference = FirebaseStorage.getInstance()
                            .getReference("jobImages/$jobId/image_$idx.jpg")
                        imageRef.putFile(uri)
                    }

                    Tasks.whenAll(uploadTasks)
                        .addOnCompleteListener {
                            isSubmitting = false
                            showPopup = true
                            viewModel.clearData()
                        }
                } else {
                    isSubmitting = false
                    showPopup = true
                    viewModel.clearData()
                }
            }
            .addOnFailureListener {
                isSubmitting = false
                Toast.makeText(context, "Failed to post job: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.White).statusBarsPadding().padding(horizontal = 24.dp).verticalScroll(rememberScrollState())) {
        Spacer(modifier = Modifier.height(16.dp))
        JobPostingTopBar(navController, "Review & Confirm")
        JobPostingProgressBar(currentStep = 5)

        Spacer(modifier = Modifier.height(16.dp))

        ReviewSection(title = "Service", content = viewModel.serviceCategory, onEdit = { 
            viewModel.isEditing = true
            navController.navigate("customerHome") 
        })
        ReviewSection(title = "Description & Timing", content = viewModel.problemDesc + "\n" + viewModel.dateFrom + " to " + viewModel.dateTo, onEdit = { 
            viewModel.isEditing = true
            navController.navigate("jobPostingDescription") 
        })
        ReviewSection(title = "Location", content = viewModel.locationAddress, onEdit = { 
            viewModel.isEditing = true
            navController.navigate("jobPostingLocation") 
        })
        ReviewSection(title = "Salary", content = if(viewModel.isHappyToNegotiate) "Negotiable" else "${viewModel.salaryMin} - ${viewModel.salaryMax} BDT (${viewModel.paymentOption})", onEdit = { 
            viewModel.isEditing = true
            navController.navigate("jobPostingSalary") 
        })
        ReviewSection(title = "Photos", content = "${viewModel.imageUris.size} photos attached", onEdit = { 
            viewModel.isEditing = true
            navController.navigate("jobPostingPhotos") 
        })

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { if (!isSubmitting) submitJobToFirebase() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F3367)),
            enabled = !isSubmitting
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Confirm Request", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showPopup) {
        AlertDialog(
            onDismissRequest = { showPopup = false },
            confirmButton = {
                Button(onClick = { 
                    showPopup = false
                    navController.navigate("customerHome") // Or wherever after confirmation
                }) { Text("OK") }
            },
            title = { Text("Confirmed") },
            text = { Text("Your job request has been successfully submitted.") }
        )
    }
}

@Composable
fun ReviewSection(title: String, content: String, onEdit: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(8.dp)).padding(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("Edit", color = Color(0xFF7D56F3), modifier = Modifier.clickable { onEdit() })
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(content, fontSize = 14.sp, color = Color.Gray)
    }
}
