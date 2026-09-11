package com.example.handyman

import android.content.Context
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.database.FirebaseDatabase
import com.example.handyman.utils.SessionManager
import android.widget.Toast
import java.util.*
import coil.compose.AsyncImage
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.android.gms.tasks.Tasks
import java.time.LocalDateTime
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import java.io.File
import android.location.Geocoder
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clipToBounds
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch

@Composable
fun JobPostingProgressBar(currentStep: Int, onStepSelected: (Int) -> Unit = {}) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(vertical = 8.dp)
            .clipToBounds()
    ) {
        val dotSize = 32.dp
        val slotWidth = maxWidth / 5
        val activeOffset by animateDpAsState(
            targetValue = slotWidth * (currentStep - 1) + ((slotWidth - dotSize) / 2),
            animationSpec = tween(300),
            label = "jobPostingStepDot"
        )

        repeat(4) { index ->
            Box(
                modifier = Modifier
                    .offset(
                        x = slotWidth * index + (slotWidth / 2) + (dotSize / 2),
                        y = 23.dp
                    )
                    .width(slotWidth - dotSize)
                    .height(1.dp)
                    .background(Color(0xFFFFB703))
            )
        }

        Box(
            modifier = Modifier
                .offset(x = activeOffset)
                .size(dotSize)
                .background(Color(0xFFFFB703), CircleShape)
                .align(Alignment.CenterStart)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(dotSize)
                .align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically
        ) {
            (1..5).forEach { step ->
                val isStepEnabled = step <= currentStep
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(enabled = isStepEnabled) { onStepSelected(step) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = step.toString(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            step == currentStep -> Color.White
                            isStepEnabled -> Color(0xFFFFB703)
                            else -> Color(0xFFB8B8B8)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun JobPostingTopBar(
    navController: NavController,
    title: String,
    navigationIcon: Int = R.drawable.arrow_back,
    navigationContentDescription: String = stringResource(R.string.cd_back),
    onBack: () -> Unit = { navController.popBackStack() }
) {
    Box(modifier = Modifier.fillMaxWidth().height(32.dp)) {
        Icon(
            painter = painterResource(id = navigationIcon),
            contentDescription = navigationContentDescription,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(24.dp)
                .clickable { onBack() }
        )
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

private fun missingJobPostingFields(viewModel: JobPostingViewModel, context: Context): List<String> {
    val missing = mutableListOf<String>()
    if (viewModel.serviceCategory.isBlank()) missing += context.getString(R.string.field_service_category)
    if (viewModel.problemDesc.isBlank()) missing += context.getString(R.string.field_problem_description)
    if (viewModel.dateFrom.isBlank()) missing += context.getString(R.string.field_start_date)
    if (viewModel.dateTo.isBlank()) missing += context.getString(R.string.field_end_date)
    if (viewModel.timeFrom.isBlank()) missing += context.getString(R.string.field_start_time)
    if (viewModel.timeTo.isBlank()) missing += context.getString(R.string.field_end_time)
    if (viewModel.locationAddress.isBlank()) missing += context.getString(R.string.field_location)
    if (!viewModel.isHappyToNegotiate) {
        if (viewModel.salaryMin.isBlank()) missing += context.getString(R.string.field_minimum_salary)
        if (viewModel.salaryMax.isBlank()) missing += context.getString(R.string.field_maximum_salary)
    }
    if (viewModel.paymentOption.isBlank()) missing += context.getString(R.string.field_payment_frequency)
    return missing
}

@Composable
fun JobPostingDescriptionScreen(
    navController: NavController,
    viewModel: JobPostingViewModel
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(stringResource(R.string.describe_your_problem), fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        OutlinedTextField(
            value = viewModel.problemDesc,
            onValueChange = {
                viewModel.problemDesc = it
            },
            placeholder = { Text(stringResource(R.string.describe_problem_hint)) },
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
                    label = { Text(stringResource(R.string.start_date_label)) },
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
                    label = { Text(stringResource(R.string.end_date_label)) },
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
                    label = { Text(stringResource(R.string.start_time_label)) },
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
                    label = { Text(stringResource(R.string.end_time_label)) },
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
            Text(stringResource(R.string.continue_btn), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun JobPostingLocationScreen(navController: NavController, viewModel: JobPostingViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val geocoder = remember { Geocoder(context) }

    fun searchAddress(address: String, mapView: MapView) {
        if (address.isBlank()) return
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val results = geocoder.getFromLocationName(address, 1)
                if (results != null && results.isNotEmpty()) {
                    val location = results[0]
                    val city = location.locality ?: location.subLocality ?: location.subAdminArea ?: location.adminArea ?: ""
                    withContext(Dispatchers.Main) {
                        viewModel.latitude = location.latitude
                        viewModel.longitude = location.longitude
                        viewModel.citySuburb = city
                        val p = GeoPoint(location.latitude, location.longitude)
                        mapView.controller.animateTo(p)
                        mapView.controller.setZoom(16.0)
                        
                        val existingMarker = mapView.overlays.filterIsInstance<Marker>().firstOrNull()
                            ?: Marker(mapView).also { mapView.overlays.add(it) }
                        existingMarker.position = p
                        existingMarker.title = "Selected Location"
                        mapView.invalidate()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateAddressFromLocation(lat: Double, lng: Double) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val results = geocoder.getFromLocation(lat, lng, 1)
                if (results != null && results.isNotEmpty()) {
                    val address = results[0].getAddressLine(0)
                    val loc = results[0]
                    val city = loc.locality ?: loc.subLocality ?: loc.subAdminArea ?: loc.adminArea ?: ""
                    withContext(Dispatchers.Main) {
                        viewModel.locationAddress = address
                        viewModel.citySuburb = city
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(stringResource(R.string.where_do_you_need_help), fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        
        var mapViewRef by remember { mutableStateOf<MapView?>(null) }

        OutlinedTextField(
            value = viewModel.locationAddress,
            onValueChange = { viewModel.locationAddress = it },
            label = { Text(stringResource(R.string.enter_your_address_label)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            trailingIcon = {
                IconButton(onClick = { mapViewRef?.let { searchAddress(viewModel.locationAddress, it) } }) {
                    Icon(painter = painterResource(id = R.drawable.search_icon), contentDescription = stringResource(R.string.cd_search), tint = Color.Gray)
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                mapViewRef?.let { searchAddress(viewModel.locationAddress, it) }
            })
        )

        Spacer(modifier = Modifier.height(16.dp))
        
        // OpenStreetMap Implementation
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp)),
            factory = { ctx ->
                MapView(ctx).apply {
                    mapViewRef = this
                    setMultiTouchControls(true)
                    controller.setZoom(14.0)
                    
                    // Initial center
                    val startPoint = if (viewModel.latitude != 0.0) 
                        GeoPoint(viewModel.latitude, viewModel.longitude)
                        else GeoPoint(23.6850, 90.3563) // Dhaka
                    controller.setCenter(startPoint)

                    val marker = Marker(this)
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    if (viewModel.latitude != 0.0) {
                        marker.position = startPoint
                        overlays.add(marker)
                    }
                    
                    val mapEventsReceiver = object : MapEventsReceiver {
                        override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                            viewModel.latitude = p.latitude
                            viewModel.longitude = p.longitude
                            updateAddressFromLocation(p.latitude, p.longitude)
                            
                            marker.position = p
                            marker.title = "Selected Location"
                            if (!overlays.contains(marker)) {
                                overlays.add(marker)
                            }
                            invalidate()
                            return true
                        }

                        override fun longPressHelper(p: GeoPoint): Boolean = false
                    }

                    overlays.add(MapEventsOverlay(mapEventsReceiver))

                    addMapListener(object : MapListener {
                        override fun onScroll(event: ScrollEvent?): Boolean {
                            // Update marker/address when user finishes scrolling
                            // For simplicity, we can do it on zoom or scroll completion
                            return false
                        }

                        override fun onZoom(event: ZoomEvent?): Boolean = false
                    })
                }
            },
            update = { mapView ->
                // Ensure marker stays synced if viewModel changes from elsewhere
            }
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (viewModel.latitude != 0.0)
                stringResource(R.string.selected_location_format, String.format("%.4f", viewModel.latitude), String.format("%.4f", viewModel.longitude))
                else stringResource(R.string.tap_map_to_pinpoint),
            fontSize = 12.sp,
            color = if (viewModel.latitude != 0.0) Color(0xFF2F3367) else Color.Gray
        )

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
            Text(stringResource(R.string.continue_btn), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun JobPostingSalaryScreen(navController: NavController, viewModel: JobPostingViewModel) {
    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { viewModel.isHappyToNegotiate = !viewModel.isHappyToNegotiate }) {
            Checkbox(checked = viewModel.isHappyToNegotiate, onCheckedChange = { viewModel.isHappyToNegotiate = it })
            Text(stringResource(R.string.happy_to_negotiate), fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (!viewModel.isHappyToNegotiate) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = viewModel.salaryMin,
                    onValueChange = { viewModel.salaryMin = it },
                    label = { Text(stringResource(R.string.min_bdt_label)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = viewModel.salaryMax,
                    onValueChange = { viewModel.salaryMax = it },
                    label = { Text(stringResource(R.string.max_bdt_label)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(stringResource(R.string.payment_frequency_label), fontSize = 16.sp, fontWeight = FontWeight.Bold)

        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = viewModel.paymentOption == "Per Day", onClick = { viewModel.paymentOption = "Per Day" })
            Text(stringResource(R.string.per_day), modifier = Modifier.clickable { viewModel.paymentOption = "Per Day" })
            Spacer(modifier = Modifier.width(24.dp))
            RadioButton(selected = viewModel.paymentOption == "Job Completed", onClick = { viewModel.paymentOption = "Job Completed" })
            Text(stringResource(R.string.job_completed), modifier = Modifier.clickable { viewModel.paymentOption = "Job Completed" })
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
            Text(stringResource(R.string.continue_btn), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
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

    Column(modifier = Modifier.fillMaxSize()) {
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
                        contentDescription = stringResource(R.string.take_photo),
                        modifier = Modifier.size(40.dp),
                        tint = Color(0xFF2F3367)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(stringResource(R.string.take_photo), fontSize = 14.sp, fontWeight = FontWeight.Medium)
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
                        contentDescription = stringResource(R.string.cd_gallery),
                        modifier = Modifier.size(40.dp),
                        tint = Color(0xFF2F3367)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(stringResource(R.string.from_gallery), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (viewModel.imageUris.isNotEmpty()) {
            Text(stringResource(R.string.selected_photos), fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
                            Icon(painter = painterResource(id = R.drawable.cancel), contentDescription = stringResource(R.string.cd_remove), modifier = Modifier.size(16.dp), tint = Color.Red)
                        }
                    }
                }
            }
        } else {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.no_photos_added_yet), color = Color.Gray)
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
            Text(stringResource(R.string.continue_btn), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun JobPostingReviewScreen(navController: NavController, viewModel: JobPostingViewModel) {
    var showPopup by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var validationMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    // Reset editing state when entering review
    // LaunchedEffect(Unit) {
    //     viewModel.isEditing = false
    // }

    fun submitJobToFirebase() {
        isSubmitting = true
        val database = FirebaseDatabase.getInstance()
        val dbRef = database.reference
        
        val isEditing = viewModel.jobId.isNotBlank()
        val jobId = if (isEditing) viewModel.jobId else UUID.randomUUID().toString()
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
            citySuburb = viewModel.citySuburb,
            latitude = viewModel.latitude,
            longitude = viewModel.longitude,
            jobSalaryFrom = viewModel.salaryMin,
            jobSalaryTo = viewModel.salaryMax,
            jobPaymentOption = viewModel.paymentOption,
            paymentStatus = "",
            imageUris = viewModel.imageUris.map { it.toString() }
        )

        val userPath = "User/$customerId"
        
        val updates = hashMapOf<String, Any>()
        updates["/Job/$jobId"] = job
        
        if (!isEditing) {
            updates["$userPath/allJobs/${dbRef.child(userPath).child("allJobs").push().key!!}"] = jobId
            updates["$userPath/notAssignedJobs/${dbRef.child(userPath).child("notAssignedJobs").push().key!!}"] = jobId
        }

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
                Toast.makeText(context, context.getString(R.string.failed_to_post_job_format, it.message), Toast.LENGTH_LONG).show()
            }
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Spacer(modifier = Modifier.height(16.dp))

        val localizedPaymentOption = when (viewModel.paymentOption) {
            "Per Day" -> stringResource(R.string.per_day)
            "Job Completed" -> stringResource(R.string.job_completed)
            else -> viewModel.paymentOption
        }
        val pleaseCompleteFormat = stringResource(R.string.please_complete_format)

        ReviewSection(title = stringResource(R.string.review_service_title), content = viewModel.serviceCategory, onEdit = {
            viewModel.isEditing = true
            navController.navigate("customerHome")
        })
        ReviewSection(title = stringResource(R.string.review_description_timing_title), content = viewModel.problemDesc + "\n" + viewModel.dateFrom + " to " + viewModel.dateTo, onEdit = {
            viewModel.isEditing = true
            navController.navigate("jobPostingDescription")
        })
        ReviewSection(title = stringResource(R.string.location_label), content = viewModel.locationAddress, onEdit = {
            viewModel.isEditing = true
            navController.navigate("jobPostingLocation")
        })
        ReviewSection(title = stringResource(R.string.salary_label), content = if(viewModel.isHappyToNegotiate) stringResource(R.string.negotiable_label) else stringResource(R.string.salary_range_format, viewModel.salaryMin, viewModel.salaryMax, localizedPaymentOption), onEdit = {
            viewModel.isEditing = true
            navController.navigate("jobPostingSalary")
        })
        ReviewSection(title = stringResource(R.string.photos_label), content = stringResource(R.string.photos_attached_format, viewModel.imageUris.size), onEdit = {
            viewModel.isEditing = true
            navController.navigate("jobPostingPhotos")
        })

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (!isSubmitting) {
                    val missingFields = missingJobPostingFields(viewModel, context)
                    if (missingFields.isNotEmpty()) {
                        validationMessage = String.format(pleaseCompleteFormat, missingFields.joinToString(", "))
                    } else {
                        submitJobToFirebase()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F3367)),
            enabled = !isSubmitting
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(stringResource(R.string.confirm_request), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
                    // Navigate to all jobs page (CustomerJobListFragment)
                    navController.navigate("allJobsList")
                }) { Text(stringResource(R.string.ok_btn)) }
            },
            title = { Text(stringResource(R.string.confirmed_title)) },
            text = { Text(stringResource(R.string.job_request_submitted_message)) }
        )
    }

    validationMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { validationMessage = null },
            confirmButton = {
                Button(onClick = { validationMessage = null }) {
                    Text(stringResource(R.string.ok_btn))
                }
            },
            title = { Text(stringResource(R.string.missing_details_title)) },
            text = { Text(message) }
        )
    }
}

@Composable
fun ReviewSection(title: String, content: String, onEdit: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(8.dp)).padding(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(stringResource(R.string.cd_edit), color = Color(0xFF7D56F3), modifier = Modifier.clickable { onEdit() })
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(content, fontSize = 14.sp, color = Color.Gray)
    }
}
