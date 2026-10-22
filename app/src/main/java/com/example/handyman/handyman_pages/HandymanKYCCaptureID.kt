package com.example.handyman.handyman_pages

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.camera.core.ImageCapture
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.handyman.HandymanSignupViewModel
import com.example.handyman.R
import com.example.handyman.components.CameraPreviewView
import com.example.handyman.components.DividerLine
import com.example.handyman.components.StepCircle
import com.example.handyman.components.takePhoto
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HandymanKYCCaptureID(modifier: Modifier = Modifier, navController: NavController, signupViewModel: HandymanSignupViewModel) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val imageCapture = remember { ImageCapture.Builder().build() }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    val galleryLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) selectedImageUri = uri
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    val showActionIcons = selectedImageUri == null

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Top Bar
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.arrow_back),
                contentDescription = stringResource(R.string.cd_back),
                modifier = Modifier
                    .size(24.dp)
                    .clickable { navController.popBackStack() }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.id_card_photo_title), fontSize = 20.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Step Indicator
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
            DividerLine()
            StepCircle(stepNumber = 4, isActive = false)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Header
        Text(stringResource(R.string.photo_id_card_title), fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            stringResource(R.string.place_id_card_hint),
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Preview Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFE8E8E8)),
            contentAlignment = Alignment.Center
        ) {
            if (selectedImageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(selectedImageUri),
                    contentDescription = stringResource(R.string.cd_selected_id),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else if (hasCameraPermission) {
                CameraPreviewView(
                    imageCapture = imageCapture,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.id_card_icon_handyman),
                    contentDescription = stringResource(R.string.cd_default_id),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        if (showActionIcons) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.image_icon),
                    contentDescription = stringResource(R.string.cd_gallery),
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(48.dp)
                        .clickable {
                            galleryLauncher.launch("image/*")
                        }
                )

                Icon(
                    painter = painterResource(id = R.drawable.camera_shutter_button),
                    contentDescription = stringResource(R.string.cd_capture),
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(72.dp)
                        .clickable {
                            if (hasCameraPermission) {
                                takePhoto(imageCapture, context) { uri ->
                                    selectedImageUri = uri
                                }
                            } else {
                                permissionLauncher.launch(android.Manifest.permission.CAMERA)
                            }
                        }
                )

                Icon(
                    painter = painterResource(id = R.drawable.bin_icon),
                    contentDescription = stringResource(R.string.cd_upload_placeholder),
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(42.dp)
                        .alpha(0f)
                )
            }
        } else {
            Spacer(modifier = Modifier.height(48.dp))
            Column {
                Button(
                    onClick = {
                        if (selectedImageUri != null) {
                            isUploading = true

                            // Storage upload happens immediately (it isn't tied to
                            // an account record); the resulting URL is held in the
                            // ViewModel and only attached to a user record once
                            // phone verification succeeds at the end of the flow.
                            val storageRef = FirebaseStorage.getInstance().reference
                            val fileName = "photo_id_cards/${System.currentTimeMillis()}_${selectedImageUri!!.lastPathSegment}"
                            val photoRef = storageRef.child(fileName)

                            photoRef.putFile(selectedImageUri!!)
                                .addOnSuccessListener {
                                    photoRef.downloadUrl.addOnSuccessListener { uri ->
                                        signupViewModel.photoIdCard = uri.toString()
                                        isUploading = false
                                        navController.navigate("handymanKYCCertificates")
                                    }.addOnFailureListener { e ->
                                        isUploading = false
                                        Log.e("KYC", "Failed to get download URL: ${e.message}")
                                    }
                                }
                                .addOnFailureListener { e ->
                                    isUploading = false
                                    Log.e("KYC", "Upload failed: ${e.message}")
                                }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isUploading,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F3367))
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(stringResource(R.string.submit_id_card_btn), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { selectedImageUri = null },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isUploading,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text(stringResource(R.string.try_again_btn), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                }
            }
        }
    }
}
