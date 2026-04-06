package com.example.handyman

import androidx.lifecycle.ViewModel

class ServiceCategoryViewModel : ViewModel() {

    val categories = listOf(
        ServiceCategory("A/C Repair Services", R.drawable.air_conditioner),
        ServiceCategory("Appliance Repair", R.drawable.appliance),
        ServiceCategory("Cleaning Solution", R.drawable.cleaning),
        ServiceCategory("Beauty and Wellness", R.drawable.beauty_service),
        ServiceCategory("Shifting", R.drawable.house_moving),
        ServiceCategory("Men's Care and Salon", R.drawable.men_care),
        ServiceCategory("Health and Care", R.drawable.healthcare),
        ServiceCategory("Electronics and Gadget Repair", R.drawable.electronic_gadget_repair),
        ServiceCategory("Electric and Plumbing", R.drawable.electric_and_plumbing),
        ServiceCategory("Pest Control", R.drawable.pest_control),
        ServiceCategory("Driver Service", R.drawable.driver_service),
        ServiceCategory("Car Care Services", R.drawable.car_service),
        ServiceCategory("Trips and Travel", R.drawable.trips_and_travel),
        ServiceCategory("Car Rental", R.drawable.car_rental),
        ServiceCategory("Painting and Renovation", R.drawable.painting_and_renovation),
        ServiceCategory("Emergency Service", R.drawable.emergency_service),
    )
}
