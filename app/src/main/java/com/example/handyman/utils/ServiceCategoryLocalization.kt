package com.example.handyman.utils

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.handyman.R

/**
 * Service category names are stored and matched on as literal English strings
 * (Firebase job records, ServiceCategoryViewModel, navigation args), so they
 * can't be swapped for localized text at the source without breaking existing
 * data. This maps each literal to its display string instead, so the stored
 * value stays stable while what the user sees is localized.
 */
private val SERVICE_CATEGORY_NAME_TO_RES: Map<String, Int> = mapOf(
    "A/C Repair Services" to R.string.ac_repair_services,
    "Appliance Repair" to R.string.appliance_repair,
    "Cleaning Solution" to R.string.cleaning_solution,
    "Beauty and Wellness" to R.string.beauty_and_wellness,
    "Shifting" to R.string.shifting,
    "Men's Care and Salon" to R.string.men_care_and_salon,
    "Health and Care" to R.string.health_and_care,
    "Electronics and Gadget Repair" to R.string.electronics_and_gadget_repair_trade,
    "Electric and Plumbing" to R.string.electric_and_plumbing,
    "Pest Control" to R.string.pest_control,
    "Driver Service" to R.string.driver_service,
    "Car Care Services" to R.string.car_care_services,
    "Trips and Travel" to R.string.trips_and_travel_category,
    "Car Rental" to R.string.car_rental,
    "Painting and Renovation" to R.string.painting_and_renovation,
    "Emergency Service" to R.string.emergency_service_category,
)

@StringRes
private fun resFor(categoryName: String): Int? = SERVICE_CATEGORY_NAME_TO_RES[categoryName]

fun localizedServiceCategoryName(context: Context, categoryName: String): String =
    resFor(categoryName)?.let { context.getString(it) } ?: categoryName

@Composable
fun localizedServiceCategoryName(categoryName: String): String {
    val resId = resFor(categoryName) ?: return categoryName
    return stringResource(resId)
}
