package com.example.handyman

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView


class HandymanJobListAdapter(
    private val handymanId: String,
    private val onViewDetails: (Job) -> Unit,
    private val onDelete: (Job) -> Unit,
    private val onUpdate: (Job) -> Unit,
    private val onLeaveReview: (Job) -> Unit,
    val onPaymentProceed: (Job) -> Unit
) : ListAdapter<Job, HandymanJobListAdapter.ViewHolder>(HandymanJobListDiff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.handyman_job_list_item, parent, false)
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
        private val delete: ImageView = itemView.findViewById(R.id.ivDelete)
        private val updateBttn   = itemView.findViewById<Button>(R.id.btnUpdate)
        private val status: TextView = itemView.findViewById(R.id.tvStatus)
        val btnLeaveReview: Button = itemView.findViewById(R.id.btnLeaveReview)
        val btnProceedPayment: Button = itemView.findViewById(R.id.btnProceedPayment)


        fun bind(item: Job) {
            // Bind your Job data to the views with fallback logic
            tvJobTitle.text = item.title ?: item.jobCat
            tvJobDesc.text = item.description ?: item.jobDesc

            if (item.paymentStatus == "done" && item.custpay != null && item.custpay.isNotEmpty()) {
                tvSalary.text = "Paid: BDT ${item.custpay}"
            } else if (item.jobSalaryFrom != null && item.jobSalaryFrom.isNotEmpty() && item.jobSalaryTo != null && item.jobSalaryTo.isNotEmpty()) {
                if (item.jobPaymentOption == "Per Day") {
                    tvSalary.text = "BDT ${item.jobSalaryFrom}-${item.jobSalaryTo}/day"
                } else {
                    tvSalary.text = "BDT ${item.jobSalaryFrom}-${item.jobSalaryTo}"
                }
            } else {
                tvSalary.text = "To be negotiated"
            }
            if (item.jobDateFrom == item.jobDateTo) {
                tvDate.text = item.jobDateFrom
            } else {
                tvDate.text = "${item.jobDateFrom} — ${item.jobDateTo}"
            }
            tvTime.text = "${item.jobTimeFrom} — ${item.jobTimeTo}"
            tvLocation.text = item.location ?: item.jobLocation

            val hasQuoted = (item.quotedHandymen as? Map<*, *>)
                ?.containsValue(handymanId) == true
            val isAssigned = item.assignedTo == handymanId
            val hasOverall = item.jobStatus == "In-progress" || item.jobStatus == "Done"

            if (!hasQuoted && !isAssigned && !hasOverall) {
                // truly nothing to show
                status.visibility = View.GONE
            } else {
                status.visibility = View.VISIBLE

                // pick one of four status labels
                val displayStatus = when {
                    hasQuoted && !isAssigned -> "Quoted"
                    isAssigned && !hasOverall -> "Accepted"
                    item.jobStatus == "In-progress" -> "In-progress"
                    else -> "Done"
                }
                status.text = displayStatus

                // apply matching background
                when (displayStatus) {
                    "Quoted" -> status.setBackgroundResource(R.drawable.status_not_assigned)
                    "Accepted" -> status.setBackgroundResource(R.drawable.status_assigned)
                    "In-progress" -> status.setBackgroundResource(R.drawable.status_in_progress)
                    "Done" -> status.setBackgroundResource(R.drawable.status_done)
                }
            }
            if (item.paymentStatus == "done") {
                updateBttn.visibility = View.GONE
                btnProceedPayment.visibility = View.GONE
                btnLeaveReview.visibility = if (item.isReviewedByHandyman) View.GONE else View.VISIBLE
                status.text = "Payment: Done"
                status.setBackgroundResource(R.drawable.status_done)
            } else {
                updateBttn.visibility = View.VISIBLE
                btnLeaveReview.visibility = View.GONE
                if (item.jobStatus == "Done") {
                    btnProceedPayment.visibility = if (item.handypay.isNotBlank()) View.GONE else View.VISIBLE
                } else {
                    btnProceedPayment.visibility = View.GONE
                }
            }

            detailsBttn.setOnClickListener {
                onViewDetails(item)
            }

            delete.setOnClickListener {
                onDelete(item)
            }

            if (item.assignedTo != null && item.assignedTo.isEmpty()) {
                // No handyman assigned at all
                updateBttn.isEnabled = false
                updateBttn.alpha = 0.5f
                updateBttn.setOnClickListener {
                    Toast.makeText(
                        itemView.context,
                        "This job has not been assigned to you yet!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else if (item.assignedTo != handymanId) {
                // Assigned to someone else
                updateBttn.isEnabled = false
                updateBttn.alpha = 0.5f
                updateBttn.setOnClickListener {
                    Toast.makeText(
                        itemView.context,
                        "This job is assigned to another handyman.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                // Assigned to this handyman
                if (item.jobStatus != "Done") {
                    updateBttn.isEnabled = true
                    updateBttn.alpha = 1.0f
                    updateBttn.setOnClickListener {
                        onUpdate(item)
                    }
                } else {
                    updateBttn.isEnabled = false
                    updateBttn.alpha = 0.5f
                    updateBttn.setOnClickListener {
                        Toast.makeText(
                            itemView.context,
                            "Job is already done!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            btnProceedPayment.setOnClickListener {
                onPaymentProceed(item)
            }
            btnLeaveReview.setOnClickListener {
                onLeaveReview(item)
            }
        }
    }
}
