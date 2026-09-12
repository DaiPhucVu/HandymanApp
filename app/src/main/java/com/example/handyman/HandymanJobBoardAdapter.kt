package com.example.handyman

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.location.Geocoder
import java.util.Locale
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.handyman.utils.localizedPaymentOptionName
import com.example.handyman.utils.localizedServiceCategoryName


class HandymanJobBoardAdapter(
    private val onViewDetails: (Job) -> Unit
) : ListAdapter<Job, HandymanJobBoardAdapter.ViewHolder>(HandymanJobBoardDiff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.handyman_job_board_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val jobItem = getItem(position)
        holder.bind(jobItem)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvJobTitle: TextView = itemView.findViewById(R.id.tvJobTitle)
        private val tvJobDesc: TextView = itemView.findViewById(R.id.tvJobSubtitle)
        private val tvSalary: TextView = itemView.findViewById(R.id.tvPrice)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        private val tvLocation: TextView = itemView.findViewById(R.id.tvAddress)
        private val detailsBttn: Button = itemView.findViewById(R.id.btnViewDetails)

        fun bind(item: Job) {
            val context = itemView.context
            // Bind real data to views
            tvJobTitle.text = if (item.jobCat.isNotEmpty()) localizedServiceCategoryName(context, item.jobCat) else (item.title ?: context.getString(R.string.untitled_job_label))
            tvJobDesc.text = item.jobDesc.ifEmpty { item.description ?: "" }

            // Salary display logic
            tvSalary.text = if (item.jobSalaryFrom.isEmpty() && item.jobSalaryTo.isEmpty()) {
                context.getString(R.string.negotiable_label)
            } else {
                context.getString(R.string.salary_range_paymentoption_format, item.jobSalaryFrom, item.jobSalaryTo, localizedPaymentOptionName(context, item.jobPaymentOption))
            }

            tvDate.text = context.getString(R.string.range_dash_format, item.jobDateFrom, item.jobDateTo)
            tvTime.text = context.getString(R.string.range_dash_format, item.jobTimeFrom, item.jobTimeTo)

            // Location with fallback - Show approximate location for privacy
            if (!item.citySuburb.isNullOrBlank()) {
                tvLocation.text = context.getString(R.string.approximate_location_format, item.citySuburb)
            } else {
                tvLocation.text = context.getString(R.string.approximate_location_label)
                
                // Fallback geocoding for the list view
                if (item.latitude != null && item.longitude != null && item.latitude != 0.0 && item.longitude != 0.0) {
                    val expectedTitle = item.title ?: localizedServiceCategoryName(context, item.jobCat)
                    Thread {
                        try {
                            val geocoder = Geocoder(context, Locale.getDefault())
                            val addresses = geocoder.getFromLocation(item.latitude, item.longitude, 1)
                            if (!addresses.isNullOrEmpty()) {
                                val address = addresses[0]
                                val city = address.locality ?: address.subLocality ?: address.subAdminArea ?: address.adminArea
                                if (city != null) {
                                    itemView.post {
                                        // Re-check if this ViewHolder is still showing the same job
                                        if (tvJobTitle.text == expectedTitle) {
                                            tvLocation.text = context.getString(R.string.approximate_location_format, city)
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            // Ignore geocoding errors in the list
                        }
                    }.start()
                }
            }

            detailsBttn.setOnClickListener {
                onViewDetails(item)
            }
        }
    }
}
