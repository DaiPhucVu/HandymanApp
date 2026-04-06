package com.example.handyman.customer_pages

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.handyman.R
import com.example.handyman.components.DividerLine
import com.example.handyman.components.StepCircle
import com.example.handyman.utils.SessionManager
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CustomerKYCCaptureID(navController: NavController, modifier: Modifier = Modifier) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var tempUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val showActionIcons = selectedImageUri == null

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) selectedImageUri = uri
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) selectedImageUri = tempUri
    }

    fun createImageUri(context: Context): Uri {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = context.cacheDir
        val file = File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
        return FileProvider.getUriForFile(
            context,
            "com.example.handyman.fileprovider",
            file
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Select ID Photo") },
            text = { Text("Choose a photo of your ID from your gallery or take a new one.") },
            confirmButton = {
                TextButton(onClick = {
                    val uri = createImageUri(context)
                    tempUri = uri
                    cameraLauncher.launch(uri)
                    showDialog = false
                }) {
                    Text("Camera")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    galleryLauncher.launch("image/*")
                    showDialog = false
                }) {
                    Text("Gallery")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Top bar
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.arrow_back),
                contentDescription = "Back",
                modifier = Modifier
                    .size(24.dp)
                    .clickable { navController.popBackStack() }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("ID Card Photo", fontSize = 20.sp)
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
            StepCircle(stepNumber = 2, isActive = false)
            DividerLine()
            StepCircle(stepNumber = 3, isActive = false)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Header
        Text("Photo ID Card", fontSize = 28.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Please review your ID Card Photo can submit.",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        // ID Frame Preview or Camera Preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(Color(0xFFE8E8E8), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (selectedImageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(selectedImageUri),
                    contentDescription = "Selected ID",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.id_card_icon),
                    contentDescription = "Default ID",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Action buttons
        if (showActionIcons){
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gallery button
                Icon(
                    painter = painterResource(id = R.drawable.image_icon),
                    contentDescription = "Gallery",
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(48.dp)
                        .clickable {
                            showDialog = true
                        }
                )

                // Capture button
                Icon(
                    painter = painterResource(id = R.drawable.camera_shutter_button),
                    contentDescription = "Capture",
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(72.dp)
                        .clickable {
                            showDialog = true
                        }
                )


                // Conditional: Remove (Bin) icon
                Icon(
                    painter = painterResource(id = R.drawable.bin_icon),
                    contentDescription = "Remove",
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(42.dp)
                        .clickable(enabled = showActionIcons) {
                            selectedImageUri = null
                        }
                        .alpha(0f) // always hide visually
                        //.alpha(if (showActionIcons) 1f else 0f) // hide visually but reserve space
                )
            }
        }
        else{

            Spacer(modifier = Modifier.height(48.dp))

            Column{ // Upload Button
                Button(
                    onClick = {
                        if (selectedImageUri != null) {
                            isUploading = true
                            val currentEmail = SessionManager.getLoggedInEmail(context)
                            val userRef = FirebaseDatabase.getInstance().getReference("User")
                            val query = userRef.orderByChild("email").equalTo(currentEmail)

                            // Step 1: Upload to Firebase Storage
                            val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance().reference
                            val fileName = "photo_id_cards/${System.currentTimeMillis()}_${selectedImageUri!!.lastPathSegment}"
                            val photoRef = storageRef.child(fileName)

                            photoRef.putFile(selectedImageUri!!)
                                .addOnSuccessListener {
                                    photoRef.downloadUrl.addOnSuccessListener { uri ->
                                        val downloadUrl = uri.toString()

                                        // Step 2: Save URL to Realtime Database
                                        query.get().addOnSuccessListener { snapshot ->
                                            for (child in snapshot.children) {
                                                child.ref.child("photoIdCard").setValue(downloadUrl)
                                                    .addOnSuccessListener {
                                                        isUploading = false
                                                        navController.navigate("customerKycAddressForm")
                                                    }
                                                    .addOnFailureListener { e ->
                                                        isUploading = false
                                                        Log.e("KYC", "Failed to save photo URL: ${e.message}")
                                                    }
                                            }

                                            if (!snapshot.exists()) {
                                                isUploading = false
                                                Log.e("KYC", "No user found with email: $currentEmail")
                                            }
                                        }.addOnFailureListener { e ->
                                            isUploading = false
                                            Log.e("KYC", "Failed to query user: ${e.message}")
                                        }
                                    }
                                }
                                .addOnFailureListener { e ->
                                    isUploading = false
                                    Log.e("KYC", "Failed to upload image to Firebase Storage: ${e.message}")
                                }
                        } else {
                            Log.e("KYC", "No image selected.")
                        }
                    }
,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isUploading,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB703))
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.DarkGray,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Submit ID Card", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                    }
                }

                Button(
                    onClick = {
                        selectedImageUri = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isUploading,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text("Try again", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                }
            }
        }
    }
}


