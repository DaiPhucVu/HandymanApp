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

class CustomerJobListAdapter(
    private val onViewDetails: (Job) -> Unit,
    private val onEdit: (Job) -> Unit,
    private val onDelete: (Job) -> Unit,
    private val onUpdate: (Job) -> Unit,
    private val onLeaveReview: (Job) -> Unit,
    val onProceedToPayment: (Job) -> Unit,
    var hideStatus: Boolean = false
) : ListAdapter<Job, CustomerJobListAdapter.ViewHolder>(CustomerJobListDiff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.customer_job_list_item, parent, false)
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
        private val edit: ImageView = itemView.findViewById(R.id.ivEdit)
        private val delete: ImageView = itemView.findViewById(R.id.ivDelete)
        private val updateBttn: Button = itemView.findViewById(R.id.btnUpdate)
        private val status: TextView = itemView.findViewById(R.id.tvStatus)
        private val btnProceedToPayment: Button = itemView.findViewById(R.id.btnProceedToPayment)
        private val btnLeaveReview: Button = itemView.findViewById(R.id.btnLeaveReview)


        fun bind(item: Job) {
            tvJobTitle.text = if (!item.jobCat.isNullOrBlank()) item.jobCat else (item.title ?: "Untitled Job")
            tvJobDesc.text = if (!item.jobDesc.isNullOrBlank()) item.jobDesc else (item.description ?: "")
            if (item.paymentStatus == "done" && !item.custpay.isNullOrBlank()) {
                tvSalary.text = "Paid: BDT ${item.custpay}"
            } else if (item.jobSalaryFrom.isNotBlank() && item.jobSalaryTo.isNotBlank()) {
                tvSalary.text = if (item.jobPaymentOption == "Per Day")
                    "BDT ${item.jobSalaryFrom}-${item.jobSalaryTo}/day"
                else
                    "BDT ${item.jobSalaryFrom}-${item.jobSalaryTo}"
            } else {
                tvSalary.text = "To be negotiated"
            }

            tvDate.text = if (item.jobDateFrom == item.jobDateTo)
                item.jobDateFrom
            else
                "${item.jobDateFrom} — ${item.jobDateTo}"

            tvTime.text = "${item.jobTimeFrom} — ${item.jobTimeTo}"
            tvLocation.text = if (!item.jobLocation.isNullOrBlank()) item.jobLocation else (item.location ?: "")

            if (hideStatus) {
                status.visibility = View.GONE
                status.text = ""
            } else {
                status.visibility = View.VISIBLE
                val displayStatus = when {
                    item.assignedTo.isBlank() -> "Not assigned"
                    item.jobStatusCustomer.isNullOrBlank() || item.jobStatusHandyman.isNullOrBlank() ->
                        "Assigned"
                    else -> item.jobStatusCustomer!!
                }
                status.text = displayStatus

                when (displayStatus) {
                    "Not assigned" -> status.setBackgroundResource(R.drawable.status_not_assigned)
                    "Assigned"     -> status.setBackgroundResource(R.drawable.status_assigned)
                    "In-progress"  -> status.setBackgroundResource(R.drawable.status_in_progress)
                    "Done"         -> status.setBackgroundResource(R.drawable.status_done)
                }
            }

            if (item.paymentStatus == "done") {
                updateBttn.visibility = View.GONE
                btnProceedToPayment.visibility = View.GONE
                btnLeaveReview.visibility = if (item.isReviewedByCustomer) View.GONE else View.VISIBLE
                status.text = "Payment: Done"
                status.setBackgroundResource(R.drawable.status_done)
            } else {
                updateBttn.visibility = View.VISIBLE
                btnLeaveReview.visibility = View.GONE
                if (item.jobStatus == "Done") {
                    btnProceedToPayment.visibility = View.VISIBLE
                } else {
                    btnProceedToPayment.visibility = View.GONE
                }
            }

            detailsBttn.setOnClickListener { onViewDetails(item) }
            edit.setOnClickListener    { onEdit(item) }
            delete.setOnClickListener  { onDelete(item) }
            btnLeaveReview.setOnClickListener { onLeaveReview(item) }

            // Disable update until someone is assigned
            if (item.assignedTo.isBlank()) {
                updateBttn.isEnabled = false
                updateBttn.alpha     = 0.5f
                updateBttn.setOnClickListener {
                    Toast.makeText(
                        itemView.context,
                        "Please assign a handyman before updating status!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                if (item.jobStatus != "Done") {
                    updateBttn.isEnabled = true
                    updateBttn.alpha     = 1.0f
                    updateBttn.setOnClickListener { onUpdate(item) }
                } else {
                    updateBttn.isEnabled = false
                    updateBttn.alpha     = 0.5f
                    updateBttn.setOnClickListener {
                        Toast.makeText(
                            itemView.context,
                            "Job is already done!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            btnProceedToPayment.setOnClickListener {
                onProceedToPayment(item)
            }
        }
    }
}
