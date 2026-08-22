package com.example.handyman

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.EditText
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.UUID

class HandymanJobListFragment : Fragment() {
    private var currentCategoryKey = "allJobs"
    private val args: HandymanJobListFragmentArgs by navArgs()
    private lateinit var handymanID: String

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HandymanJobListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        handymanID = args.handymanId
        val view = inflater.inflate(R.layout.fragment_handyman_job_list, container, false)

        view.findViewById<ImageView>(R.id.ivBack).setOnClickListener {
            findNavController().navigateUp()
        }

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        adapter = HandymanJobListAdapter(
            handymanId = handymanID,
            onViewDetails = { job ->
                val action = HandymanJobListFragmentDirections.actionHandymanJobListFragmentToHandymanJobListDetailsFragment(
                    customerId = job.customerId,
                    jobId = job.jobId,
                    serviceCategory = job.jobCat,
                    problemDesc = job.jobDesc,
                    dateFrom = job.jobDateFrom,
                    dateTo = job.jobDateTo,
                    timeFrom = job.jobTimeFrom,
                    timeTo = job.jobTimeTo,
                    location = job.jobLocation,
                    salaryFrom = job.jobSalaryFrom,
                    salaryTo = job.jobSalaryTo,
                    paymentOption = job.jobPaymentOption,
                    imageUris = null,
                    assignedTo = job.assignedTo,
                    jobStatus = job.jobStatus
                )
                findNavController().navigate(action)
            },
            onDelete = { job ->
                // No-op or remove if we don't want any job removal from list by handyman
                Toast.makeText(requireContext(), "Removal of assigned jobs not permitted.", Toast.LENGTH_SHORT).show()
            },
            onAccept = { job ->
                acceptAssignedJob(job)
            },
            onDecline = { job ->
                confirmDeclineAssignedJob(job)
            },
            onUpdate = { job ->
                val currentStatus = normalizeJobStatus(job.jobStatus)
                val nextStatuses = when (currentStatus) {
                    "In-progress" -> arrayOf("Done")
                    "Done" -> arrayOf()
                    else -> arrayOf("In-progress")
                }

                if (nextStatuses.isEmpty()) {
                    context?.let {
                        Toast.makeText(it, "No further updates available", Toast.LENGTH_SHORT).show()
                    }
                    return@HandymanJobListAdapter
                }

                var chosen = 0
                AlertDialog.Builder(requireContext())
                    .setTitle("Update status")
                    .setSingleChoiceItems(nextStatuses, 0) { _, which -> chosen = which }
                    .setPositiveButton("OK") { _, _ ->
                        val newStatus = nextStatuses[chosen]
                        val jobRef = FirebaseDatabase.getInstance().getReference("Job").child(job.jobId)

                        jobRef.child("jobStatusHandyman").setValue(newStatus)
                            .addOnSuccessListener {
                                jobRef.child("jobStatusCustomer").addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snap: DataSnapshot) {
                                        val customerStatus = snap.getValue(String::class.java)
                                        if (customerStatus == newStatus) {
                                            jobRef.child("jobStatus").setValue(newStatus)
                                                .addOnSuccessListener {
                                                    if (newStatus == "Done") {
                                                        jobRef.child("finishedBy").setValue(java.time.LocalDateTime.now().toString())
                                                    }
                                                    
                                                    val (custFrom, hmFrom, toList) = when (newStatus) {
                                                        "In-progress" -> Triple("assignedJobs", "acceptedJobs", "inProgressJobs")
                                                        "Done" -> Triple("inProgressJobs", "inProgressJobs", "completedJobs")
                                                        else -> return@addOnSuccessListener
                                                    }

                                                    val custRef = FirebaseDatabase.getInstance().getReference("User").child(job.customerId)
                                                    val hmRef = FirebaseDatabase.getInstance().getReference("Handyman").child(handymanID)

                                                    moveJobId(custRef, custFrom, toList, job.jobId)
                                                    moveJobId(hmRef, hmFrom, toList, job.jobId)

                                                    fetchJobsForCategory(currentCategoryKey)
                                                }
                                        } else {
                                            context?.let {
                                                Toast.makeText(it, "Waiting for customer to also update to $newStatus", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                    override fun onCancelled(e: DatabaseError) {}
                                })
                            }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            },
            onLeaveReview = { job ->
                showCustomerReviewDialog(job)
            },
            onPaymentProceed = { job ->
                if (normalizeJobStatus(job.jobStatus) != "Done") {
                    context?.let {
                        Toast.makeText(it, "Job is not marked as Done yet.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    showPaymentDialog(job)
                }
            }
        )
        recyclerView.adapter = adapter

        val spinner = view.findViewById<Spinner>(R.id.spinnerStatus)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, v: View?, pos: Int, id: Long) {
                val display = parent.getItemAtPosition(pos) as String
                currentCategoryKey = when (display) {
                    "Pending" -> "pendingJobs"
                    "Accepted"  -> "acceptedJobs"
                    "In-progress" -> "inProgressJobs"
                    "Done"        -> "completedJobs"
                    "Cancelled" -> "cancelledJobs"
                    else          -> "allJobs"
                }
                fetchJobsForCategory(currentCategoryKey)
            }
            override fun onNothingSelected(parent: AdapterView<*>) { }
        }

        fetchJobsForCategory("allJobs")
        return view
    }

    private fun showCustomerReviewDialog(job: Job) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_customer_review, null)
        val ratingBar = dialogView.findViewById<RatingBar>(R.id.ratingBar)
        val etComment = dialogView.findViewById<EditText>(R.id.etComment)
        val tvTitle = dialogView.findViewById<android.widget.TextView>(R.id.tvReviewTitle)

        tvTitle.text = "How was your experience with this customer?"

        AlertDialog.Builder(requireContext())
            .setTitle("Review Customer")
            .setView(dialogView)
            .setPositiveButton("Submit") { _, _ ->
                val rating = ratingBar.rating
                val comment = etComment.text.toString()

                if (rating == 0f) {
                    context?.let {
                        Toast.makeText(it, "Please provide a rating", Toast.LENGTH_SHORT).show()
                    }
                    return@setPositiveButton
                }

                val reviewId = UUID.randomUUID().toString()
                val review = Review(
                    reviewId = reviewId,
                    jobId = job.jobId,
                    customerId = job.customerId,
                    handymanId = handymanID,
                    rating = rating,
                    comment = comment,
                    timestamp = java.time.LocalDateTime.now().toString(),
                    reviewerType = "handyman"
                )

                FirebaseDatabase.getInstance().getReference("Reviews").child(reviewId)
                    .setValue(review)
                    .addOnSuccessListener {
                        updateAverageRating(job.customerId, "User")
                        // Mark job as reviewed by handyman
                        FirebaseDatabase.getInstance().getReference("Job")
                            .child(job.jobId)
                            .child("isReviewedByHandyman")
                            .setValue(true)
                            .addOnSuccessListener {
                                context?.let {
                                    Toast.makeText(it, "Review submitted!", Toast.LENGTH_SHORT).show()
                                }
                                fetchJobsForCategory(currentCategoryKey)
                            }
                    }
                    .addOnFailureListener { e ->
                        context?.let {
                            Toast.makeText(it, "Failed to submit review: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateAverageRating(userId: String, userType: String) {
        val reviewsRef = FirebaseDatabase.getInstance().getReference("Reviews")
        val idField = if (userType == "Handyman") "handymanId" else "customerId"
        val reviewerType = if (userType == "Handyman") "customer" else "handyman"

        reviewsRef.orderByChild(idField).equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var totalRating = 0f
                    var count = 0
                    for (child in snapshot.children) {
                        val review = child.getValue(Review::class.java)
                        if (review != null && review.reviewerType == reviewerType) {
                            totalRating += review.rating
                            count++
                        }
                    }

                    if (count > 0) {
                        val average = totalRating / count
                        val userRef = FirebaseDatabase.getInstance().getReference(userType).child(userId)
                        val updates = mapOf(
                            "averageRating" to average,
                            "reviewCount" to count
                        )
                        userRef.updateChildren(updates)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun showPaymentDialog(job: Job) {
        val input = EditText(requireContext())
        input.hint = "Enter amount in BDT"
        
        AlertDialog.Builder(requireContext())
            .setTitle("Enter Final Payment")
            .setView(input)
            .setPositiveButton("Submit") { _, _ ->
                val amount = input.text.toString()
                if (amount.isNotEmpty()) {
                    FirebaseDatabase.getInstance().getReference("Job")
                        .child(job.jobId).child("handypay").setValue(amount)
                        .addOnSuccessListener {
                            context?.let {
                                Toast.makeText(it, "Payment recorded: BDT $amount", Toast.LENGTH_SHORT).show()
                            }
                            fetchJobsForCategory(currentCategoryKey)
                        }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun moveJobId(ref: DatabaseReference, fromList: String, toList: String, jobId: String) {
        ref.child(fromList).orderByValue().equalTo(jobId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach { it.ref.removeValue() }
                    ref.child(toList).push().setValue(jobId)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun acceptAssignedJob(job: Job) {
        val jobRef = FirebaseDatabase.getInstance().getReference("Job").child(job.jobId)
        val updates = mapOf<String, Any>(
            "jobStatus" to "Accepted",
            "jobStatusHandyman" to "Accepted",
            "lastUpdated" to java.time.LocalDateTime.now().toString()
        )

        jobRef.updateChildren(updates)
            .addOnSuccessListener {
                val customerRef = FirebaseDatabase.getInstance().getReference("User").child(job.customerId)
                val handymanRef = FirebaseDatabase.getInstance().getReference("Handyman").child(handymanID)

                removeJobId(customerRef, "notAssignedJobs", job.jobId)
                addJobIdIfMissing(customerRef, "assignedJobs", job.jobId)
                addJobIdIfMissing(customerRef, "allJobs", job.jobId)

                removeJobId(handymanRef, "cancelledJobs", job.jobId)
                addJobIdIfMissing(handymanRef, "acceptedJobs", job.jobId)
                addJobIdIfMissing(handymanRef, "allJobs", job.jobId)

                Toast.makeText(requireContext(), "Job accepted.", Toast.LENGTH_SHORT).show()
                fetchJobsForCategory(currentCategoryKey)
            }
            .addOnFailureListener { error ->
                Toast.makeText(requireContext(), "Failed to accept job: ${error.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun confirmDeclineAssignedJob(job: Job) {
        AlertDialog.Builder(requireContext())
            .setTitle("Decline job?")
            .setMessage("This will unassign the job and return it to the open job list.")
            .setPositiveButton("Decline") { _, _ -> declineAssignedJob(job) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun declineAssignedJob(job: Job) {
        val jobRef = FirebaseDatabase.getInstance().getReference("Job").child(job.jobId)
        val updates = mapOf<String, Any>(
            "assignedTo" to "",
            "jobStatus" to "",
            "lastUpdated" to java.time.LocalDateTime.now().toString()
        )

        jobRef.updateChildren(updates)
            .addOnSuccessListener {
                jobRef.child("jobStatusHandyman").removeValue()

                val customerRef = FirebaseDatabase.getInstance().getReference("User").child(job.customerId)
                val handymanRef = FirebaseDatabase.getInstance().getReference("Handyman").child(handymanID)

                removeJobId(customerRef, "assignedJobs", job.jobId)
                addJobIdIfMissing(customerRef, "notAssignedJobs", job.jobId)
                addJobIdIfMissing(customerRef, "allJobs", job.jobId)

                listOf("acceptedJobs", "inProgressJobs", "completedJobs", "allJobs").forEach { listName ->
                    removeJobId(handymanRef, listName, job.jobId)
                }
                addJobIdIfMissing(handymanRef, "cancelledJobs", job.jobId)

                Toast.makeText(requireContext(), "Job declined.", Toast.LENGTH_SHORT).show()
                fetchJobsForCategory(currentCategoryKey)
            }
            .addOnFailureListener { error ->
                Toast.makeText(requireContext(), "Failed to decline job: ${error.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun addJobIdIfMissing(ref: DatabaseReference, listName: String, jobId: String) {
        ref.child(listName).orderByValue().equalTo(jobId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        ref.child(listName).push().setValue(jobId)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun removeJobId(ref: DatabaseReference, listName: String, jobId: String) {
        ref.child(listName).orderByValue().equalTo(jobId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach { it.ref.removeValue() }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun fetchJobsForCategory(category: String) {
        val rootRef = FirebaseDatabase.getInstance().getReference("Handyman").child(handymanID)
        val categoriesToFetch = if (category == "allJobs") {
            listOf("acceptedJobs", "inProgressJobs", "completedJobs", "allJobs")
        } else {
            listOf(category)
        }

        val listedJobIds = mutableSetOf<String>()
        var completedFetches = 0

        categoriesToFetch.forEach { cat ->
            rootRef.child(cat).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.mapNotNull { it.getValue(String::class.java) }.let { listedJobIds.addAll(it) }
                    completedFetches++
                    if (completedFetches == categoriesToFetch.size) fetchJobsForCategory(category, listedJobIds)
                }
                override fun onCancelled(error: DatabaseError) {
                    completedFetches++
                    if (completedFetches == categoriesToFetch.size) fetchJobsForCategory(category, listedJobIds)
                }
            })
        }
    }

    private fun fetchJobsForCategory(category: String, listedJobIds: Set<String>) {
        FirebaseDatabase.getInstance().getReference("Job")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val jobs = snapshot.children.mapNotNull { jobSnap ->
                        val key = jobSnap.key ?: return@mapNotNull null
                        val job = jobSnap.getValue(Job::class.java)?.copy(jobId = key)
                            ?: return@mapNotNull null

                        if (key in listedJobIds || isAssignedJobInCategory(job, category)) job else null
                    }
                    adapter.submitList(jobs)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun isAssignedJobInCategory(job: Job, category: String): Boolean {
        if (job.assignedTo != handymanID) return false

        return when (category) {
            "allJobs" -> normalizeJobStatus(job.jobStatus) != "Inactive"
            "acceptedJobs" -> !isPendingAssignment(job) &&
                    normalizeJobStatus(job.jobStatus) == "Accepted"
            "pendingJobs" -> isPendingAssignment(job)
            "inProgressJobs" -> !isPendingAssignment(job) &&
                    normalizeJobStatus(job.jobStatus) == "In-progress"
            "completedJobs" -> normalizeJobStatus(job.jobStatus) == "Done"
            "cancelledJobs" -> normalizeJobStatus(job.jobStatus) == "Cancelled"
            else -> false
        }
    }

    private fun isPendingAssignment(job: Job): Boolean {
        val status = normalizeJobStatus(job.jobStatus)
        val handymanStatus = normalizeJobStatus(job.jobStatusHandyman.orEmpty())
        return job.assignedTo == handymanID &&
                status != "Done" &&
                status != "Cancelled" &&
                (handymanStatus.isBlank() || handymanStatus == "Pending" || handymanStatus == "Assigned")
    }

    private fun normalizeJobStatus(status: String): String = when (status.trim().lowercase()) {
        "in progress", "in-progress", "in_progress" -> "In-progress"
        "done", "completed", "complete" -> "Done"
        "cancelled", "canceled" -> "Cancelled"
        "inactive" -> "Inactive"
        "accepted" -> "Accepted"
        "assigned" -> "Assigned"
        else -> status.trim()
    }
}
