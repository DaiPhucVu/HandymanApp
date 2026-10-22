package com.example.handyman.handyman_pages

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.example.handyman.R
import com.example.handyman.components.DividerLine
import com.example.handyman.components.StepCircle
import android.location.Geocoder
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.handyman.HandymanSignupViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

@Composable
fun HandymanKYCAddressForm(modifier: Modifier = Modifier, navController: NavController, signupViewModel: HandymanSignupViewModel) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val textFieldModifier = Modifier
        .fillMaxWidth()
        .heightIn(min = 56.dp)

    var houseNumber by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var postCode by remember { mutableStateOf("") }
    var division by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var thana by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("Bangladesh") }
    var note by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf(0.0) }
    var longitude by remember { mutableStateOf(0.0) }
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var showMap by remember { mutableStateOf(false) }
    var isNavigating by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val geocoder = remember { Geocoder(context) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapViewRef?.onResume()
                Lifecycle.Event.ON_PAUSE -> mapViewRef?.onPause()
                Lifecycle.Event.ON_DESTROY -> mapViewRef?.onDetach()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapViewRef?.onDetach()
        }
    }

    fun reverseGeocode(lat: Double, lng: Double) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                if (!Geocoder.isPresent()) return@launch
                val results = geocoder.getFromLocation(lat, lng, 1)
                if (!results.isNullOrEmpty()) {
                    val a = results[0]
                    withContext(Dispatchers.Main) {
                        a.subThoroughfare?.let { houseNumber = it }
                        a.thoroughfare?.let { street = it }
                        a.subLocality?.let { area = it }
                        a.postalCode?.let { postCode = it }
                        a.adminArea?.let { division = it }
                        a.subAdminArea?.let { district = it }
                        a.locality?.let { city = it; thana = it }
                        a.countryName?.let { country = it }
                    }
                }
            } catch (e: Exception) {
                Log.e("KYC", "Reverse geocode failed: ${e.message}")
            }
        }
    }

    fun forwardGeocode(map: MapView) {
        val query = listOf(houseNumber, street, area, city, country)
            .filter { it.trim().isNotBlank() }.joinToString(", ")
        if (query.isBlank()) return
        coroutineScope.launch(Dispatchers.IO) {
            try {
                if (!Geocoder.isPresent()) return@launch
                val results = geocoder.getFromLocationName(query, 1)
                if (!results.isNullOrEmpty()) {
                    val r = results[0]
                    withContext(Dispatchers.Main) {
                        latitude = r.latitude
                        longitude = r.longitude
                        val p = GeoPoint(latitude, longitude)
                        map.controller.animateTo(p)
                        map.controller.setZoom(16.0)
                        val marker = map.overlays.filterIsInstance<Marker>().firstOrNull()
                            ?: Marker(map).also { map.overlays.add(it) }
                        marker.position = p
                        marker.title = "Selected Location"
                        map.invalidate()
                    }
                }
            } catch (e: Exception) {
                Log.e("KYC", "Forward geocode failed: ${e.message}")
            }
        }
    }

    // Validation rules
    val isValidHouseNumber = houseNumber.trim().isNotBlank()
    val isValidStreet = street.trim().isNotBlank()
    val isValidArea = area.trim().isNotBlank()
    val isValidPostCode = postCode.trim().matches(Regex("^\\d{4,10}$"))
    val isValidDivision = division.trim().isNotBlank()
    val isValidDistrict = district.trim().isNotBlank()
    val isValidThana = thana.trim().isNotBlank()
    val isValidCity = city.trim().isNotBlank()
    val isValidCountry = country.trim().isNotBlank()

    val allFieldsFilled = houseNumber.trim().isNotBlank() && street.trim().isNotBlank() && area.trim().isNotBlank() &&
            postCode.trim().isNotBlank() && division.trim().isNotBlank() && district.trim().isNotBlank() &&
            thana.trim().isNotBlank() && city.trim().isNotBlank() && country.trim().isNotBlank()

    val isFormComplete = allFieldsFilled && isValidHouseNumber && isValidStreet && isValidArea &&
            isValidPostCode && isValidDivision && isValidDistrict && isValidThana && isValidCity && isValidCountry

    if (showMap) {
        Dialog(
            onDismissRequest = {
                mapViewRef?.onPause()
                mapViewRef?.onDetach()
                mapViewRef = null
                showMap = false
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding()
                        .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 80.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {
                            mapViewRef?.onPause()
                            mapViewRef?.onDetach()
                            mapViewRef = null
                            showMap = false
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.arrow_back),
                                contentDescription = stringResource(R.string.cd_back)
                            )
                        }
                        Text(
                            stringResource(R.string.pin_location_on_map_label),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    AndroidView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .border(1.dp, Color.LightGray),
                        factory = { ctx ->
                            MapView(ctx).apply {
                                mapViewRef = this
                                setMultiTouchControls(true)
                                controller.setZoom(14.0)
                                val startPoint = if (latitude != 0.0)
                                    GeoPoint(latitude, longitude)
                                else GeoPoint(23.6850, 90.3563)
                                controller.setCenter(startPoint)

                                val marker = Marker(this)
                                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                if (latitude != 0.0) {
                                    marker.position = startPoint
                                    overlays.add(marker)
                                }

                                val mapEventsReceiver = object : MapEventsReceiver {
                                    override fun singleTapConfirmedHelper(point: GeoPoint): Boolean {
                                        latitude = point.latitude
                                        longitude = point.longitude
                                        reverseGeocode(point.latitude, point.longitude)
                                        marker.position = point
                                        marker.title = "Selected Location"
                                        if (!overlays.contains(marker)) overlays.add(marker)
                                        invalidate()
                                        return true
                                    }

                                    override fun longPressHelper(point: GeoPoint): Boolean = false
                                }
                                overlays.add(MapEventsOverlay(mapEventsReceiver))
                                forwardGeocode(this)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (latitude != 0.0)
                            stringResource(R.string.pinned_location_format, String.format("%.4f", latitude), String.format("%.4f", longitude))
                        else stringResource(R.string.tap_map_to_pin_location),
                        color = if (latitude != 0.0) Color(0xFF2F3367) else Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            mapViewRef?.onPause()
                            mapViewRef?.onDetach()
                            mapViewRef = null
                            showMap = false
                        },
                        enabled = latitude != 0.0,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F3367))
                    ) {
                        Text(stringResource(R.string.use_this_location_btn), color = Color.White)
                    }
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .imePadding()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Top Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.arrow_back),
                contentDescription = stringResource(R.string.cd_back),
                modifier = Modifier
                    .size(24.dp)
                    .clickable { navController.popBackStack() }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.account_verification_title), fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Step UI
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                StepCircle(stepNumber = 1, isActive = true)
                DividerLine()
                StepCircle(stepNumber = 2, isActive = true)
                DividerLine()
                StepCircle(stepNumber = 3, isActive = true)
                DividerLine()
                StepCircle(stepNumber = 4, isActive = false)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Header
            Text(stringResource(R.string.confirm_your_address_title), fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                stringResource(R.string.confirm_address_subtitle),
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Input Fields
            OutlinedTextField(
                value = houseNumber,
                onValueChange = { houseNumber = it },
                label = { Text(stringResource(R.string.house_number_label)) },
                modifier = textFieldModifier,
                isError = houseNumber.isNotBlank() && !isValidHouseNumber,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = street,
                onValueChange = { street = it },
                label = { Text(stringResource(R.string.street_label)) },
                modifier = textFieldModifier,
                isError = street.isNotBlank() && !isValidStreet,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = area,
                    onValueChange = { area = it },
                    label = { Text(stringResource(R.string.area_neighborhood_label)) },
                    modifier = Modifier.weight(1f),
                    isError = area.isNotBlank() && !isValidArea,
                    singleLine = true
                )
                OutlinedTextField(
                    value = postCode,
                    onValueChange = { postCode = it },
                    label = { Text(stringResource(R.string.post_code_label)) },
                    modifier = Modifier.weight(1f),
                    isError = postCode.isNotBlank() && !isValidPostCode,
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = division,
                    onValueChange = { division = it },
                    label = { Text(stringResource(R.string.division_label)) },
                    modifier = Modifier.weight(1f),
                    isError = division.isNotBlank() && !isValidDivision,
                    singleLine = true
                )
                OutlinedTextField(
                    value = district,
                    onValueChange = { district = it },
                    label = { Text(stringResource(R.string.district_label)) },
                    modifier = Modifier.weight(1f),
                    isError = district.isNotBlank() && !isValidDistrict,
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = thana,
                    onValueChange = { thana = it },
                    label = { Text(stringResource(R.string.thana_label)) },
                    modifier = Modifier.weight(1f),
                    isError = thana.isNotBlank() && !isValidThana,
                    singleLine = true
                )
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text(stringResource(R.string.city_label)) },
                    modifier = Modifier.weight(1f),
                    isError = city.isNotBlank() && !isValidCity,
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = country,
                onValueChange = { country = it },
                label = { Text(stringResource(R.string.country_label)) },
                modifier = textFieldModifier,
                isError = country.isNotBlank() && !isValidCountry,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text(stringResource(R.string.additional_note_optional_label)) },
                modifier = textFieldModifier
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.pin_location_on_map_label), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                TextButton(onClick = {
                    val map = mapViewRef
                    if (map == null) {
                        showMap = true
                    } else {
                        forwardGeocode(map)
                    }
                }) {
                    Text(stringResource(R.string.find_on_map_btn), color = Color(0xFFFFB703))
                }
            }

            if (latitude != 0.0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.pinned_location_format, String.format("%.4f", latitude), String.format("%.4f", longitude)),
                    fontSize = 12.sp,
                    color = Color(0xFF2F3367)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        Button(
            onClick = {
                if (isNavigating) return@Button
                isNavigating = true
                signupViewModel.houseNumber = houseNumber.trim()
                signupViewModel.street = street.trim()
                signupViewModel.area = area.trim()
                signupViewModel.postCode = postCode.trim()
                signupViewModel.division = division.trim()
                signupViewModel.district = district.trim()
                signupViewModel.thana = thana.trim()
                signupViewModel.city = city.trim()
                signupViewModel.country = country.trim()
                signupViewModel.notes = note.trim()
                signupViewModel.latitude = latitude
                signupViewModel.longitude = longitude
                mapViewRef?.onPause()
                mapViewRef?.onDetach()
                mapViewRef = null
                navController.navigate("handymanKycPhoneNumber")
            },
            enabled = isFormComplete && !isNavigating,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isFormComplete) Color(0xFF2F3367) else Color(0xFFCCCCCC)
            )
        ) {
            Text(stringResource(R.string.submit_address_btn), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}
