package com.example.handyman

import androidx.lifecycle.ViewModel

class JobViewModel : ViewModel() {
    val jobs = listOf(
        Job(java.util.UUID.randomUUID().toString()),
    )
}
