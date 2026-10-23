package com.example.handyman.utils

import android.content.Context
import com.example.handyman.R

/**
 * Job status and payment-option values are stored and compared on as literal
 * English strings (Firebase job records, status-transition logic keyed on
 * "Done"/"In-progress"/etc.), so they can't be swapped for localized text at
 * the source without breaking that logic. These map each literal to its
 * display string instead — callers must keep using the raw value for storage
 * and comparisons, and only pass it through here for what's shown on screen.
 */
private val STATUS_NAME_TO_RES: Map<String, Int> = mapOf(
    "Pending" to R.string.status_pending,
    "Accepted" to R.string.status_accepted,
    "Assigned" to R.string.status_assigned,
    "Not assigned" to R.string.status_not_assigned,
    "In-progress" to R.string.status_in_progress,
    "Done" to R.string.status_done,
    "Cancelled" to R.string.status_cancelled,
    "All" to R.string.status_all,
)

fun localizedJobStatusLabel(context: Context, status: String): String =
    STATUS_NAME_TO_RES[status]?.let { context.getString(it) } ?: status

fun localizedPaymentOptionName(context: Context, paymentOption: String): String = when (paymentOption) {
    "Per Day" -> context.getString(R.string.per_day)
    "Job Completed" -> context.getString(R.string.job_completed)
    else -> paymentOption
}
