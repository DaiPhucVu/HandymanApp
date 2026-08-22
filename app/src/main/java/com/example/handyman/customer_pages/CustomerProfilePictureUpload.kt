package com.example.handyman.customer_pages

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.handyman.R
import com.example.handyman.utils.SessionManager
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CustomerProfilePictureUpload(navController: NavController) {
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var tempUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) imageUri = uri
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) imageUri = tempUri
    }

    fun createImageUri(context: Context): Uri {
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = context.cacheDir
        val file = File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
        return FileProvider.getUriForFile(
            context,
            context.packageName + ".fileprovider",
            file
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Select Profile Picture") },
            text = { Text("Choose a photo from your gallery or take a new one.") },
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
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Final Step!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFFB703)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Add a profile picture so handymen can recognize you. A friendly face builds trust!",
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(60.dp))

        // Profile Picture Preview
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(Color(0xFFF1F3F5))
                .border(2.dp, Color(0xFFFFB703), CircleShape)
                .clickable { showDialog = true },
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(imageUri),
                    contentDescription = "Profile Picture",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(id = R.drawable.camera),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color(0xFFFFB703)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Upload Photo",
                        color = Color(0xFFFFB703),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            // Camera Icon Overlay
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 12.dp, end = 12.dp)
                    .size(40.dp)
                    .background(Color(0xFFFFB703), CircleShape)
                    .border(3.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.camera),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        TextButton(
            onClick = {
                navController.navigate("customerKycSuccess") {
                    popUpTo("landingPage") { inclusive = true }
                }
            },
            enabled = !isUploading
        ) {
            Text(
                text = "Skip for now",
                color = Color.Gray,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (imageUri == null) {
                    navController.navigate("customerKycSuccess") {
                        popUpTo("landingPage") { inclusive = true }
                    }
                    return@Button
                }

                isUploading = true
                val storageRef = FirebaseStorage.getInstance().reference
                    .child("profile_pictures/${UUID.randomUUID()}.jpg")

                storageRef.putFile(imageUri!!)
                    .addOnSuccessListener {
                        storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                            val userId = SessionManager.getLoggedInUserId(context)

                            if (userId.isNotEmpty()) {
                                val dbRef = FirebaseDatabase.getInstance()
                                    .getReference("User")
                                    .child(userId)

                                dbRef.child("profileImageUrl").setValue(downloadUri.toString())
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            context,
                                            "Profile updated!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        navController.navigate("customerKycSuccess") {
                                            popUpTo("landingPage") { inclusive = true }
                                        }
                                    }
                                    .addOnFailureListener {
                                        isUploading = false
                                        Toast.makeText(
                                            context,
                                            "Database update failed",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            } else {
                                val email = SessionManager.getLoggedInEmail(context)
                                val dbRef = FirebaseDatabase.getInstance().getReference("User")

                                dbRef.orderByChild("email").equalTo(email).get()
                                    .addOnSuccessListener { snapshot ->
                                        for (child in snapshot.children) {
                                            child.ref.child("profileImageUrl")
                                                .setValue(downloadUri.toString())
                                        }

                                        Toast.makeText(
                                            context,
                                            "Profile updated!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        navController.navigate("customerKycSuccess") {
                                            popUpTo("landingPage") { inclusive = true }
                                        }
                                    }
                                    .addOnFailureListener {
                                        isUploading = false
                                        Toast.makeText(
                                            context,
                                            "User search failed",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                        }
                    }
                    .addOnFailureListener {
                        isUploading = false
                        Toast.makeText(context, "Upload failed: ${it.message}", Toast.LENGTH_SHORT)
                            .show()
                    }
            },
            enabled = !isUploading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFB703),
                disabledContainerColor = Color.Gray
            )
        ) {
            if (isUploading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    "Finish and Start",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.DarkGray
                )
            }
        }
    }
}
