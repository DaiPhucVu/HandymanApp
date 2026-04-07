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
                val handymanRef = FirebaseDatabase.getInstance()
                    .getReference("Handyman")
                    .child(handymanID)
                    .child("quotedJobs")

                handymanRef.orderByValue().equalTo(job.jobId)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(handymanSnapshot: DataSnapshot) {
                            if (!handymanSnapshot.exists()) {
                                context?.let {
                                    Toast.makeText(it, "Cannot cancel.", Toast.LENGTH_SHORT).show()
                                }
                                return
                            }

                            AlertDialog.Builder(requireContext())
                                .setTitle("Withdraw quote?")
                                .setMessage("Remove your quote for this job?")
                                .setPositiveButton("Yes") { _, _ ->
                                    val quotesRef = FirebaseDatabase.getInstance()
                                        .getReference("Job")
                                        .child(job.jobId)
                                        .child("quotedHandymen")

                                    quotesRef.orderByValue().equalTo(handymanID)
                                        .addListenerForSingleValueEvent(object : ValueEventListener {
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                if (!snapshot.exists()) {
                                                    context?.let {
                                                        Toast.makeText(it, "You haven’t quoted this job.", Toast.LENGTH_SHORT).show()
                                                    }
                                                    return
                                                }

                                                snapshot.children.forEach { it.ref.removeValue() }
                                                handymanSnapshot.children.forEach { it.ref.removeValue() }

                                                val cancelledJobsRef = FirebaseDatabase.getInstance()
                                                    .getReference("Handyman")
                                                    .child(handymanID)
                                                    .child("cancelledJobs")

                                                cancelledJobsRef.push().setValue(job.jobId)
                                                    .addOnSuccessListener {
                                                        val allJobsRef = FirebaseDatabase.getInstance()
                                                            .getReference("Handyman")
                                                            .child(handymanID)
                                                            .child("allJobs")

                                                        allJobsRef.orderByValue().equalTo(job.jobId)
                                                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                                    snapshot.children.forEach { it.ref.removeValue() }
                                                                    context?.let {
                                                                        Toast.makeText(it, "Your quote was removed.", Toast.LENGTH_SHORT).show()
                                                                    }
                                                                    fetchJobsForCategory(currentCategoryKey)
                                                                }
                                                                override fun onCancelled(error: DatabaseError) {}
                                                            })
                                                    }
                                            }
                                            override fun onCancelled(error: DatabaseError) {}
                                        })
                                }
                                .setNegativeButton("No", null)
                                .show()
                        }
                        override fun onCancelled(error: DatabaseError) {}
                    })
            },
            onUpdate = { job ->
                val currentStatus = job.jobStatus
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
                if (job.jobStatus != "Done") {
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
                    "Quoted"      -> "quotedJobs"
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

    private fun fetchJobsForCategory(category: String) {
        val rootRef = FirebaseDatabase.getInstance().getReference("Handyman").child(handymanID)

        if (category == "allJobs") {
            val categoriesToFetch = listOf("quotedJobs", "acceptedJobs", "inProgressJobs", "completedJobs", "allJobs")
            val allJobIds = mutableSetOf<String>()
            var completedFetches = 0

            categoriesToFetch.forEach { cat ->
                rootRef.child(cat).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.children.mapNotNull { it.getValue(String::class.java) }.let { allJobIds.addAll(it) }
                        completedFetches++
                        if (completedFetches == categoriesToFetch.size) fetchJobsByIds(allJobIds.toList())
                    }
                    override fun onCancelled(error: DatabaseError) {
                        completedFetches++
                        if (completedFetches == categoriesToFetch.size) fetchJobsByIds(allJobIds.toList())
                    }
                })
            }
            return
        }

        rootRef.child(category).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(listSnap: DataSnapshot) {
                val jobIds = listSnap.children.mapNotNull { it.getValue(String::class.java) }
                fetchJobsByIds(jobIds)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun fetchJobsByIds(jobIds: List<String>) {
        if (jobIds.isEmpty()) {
            adapter.submitList(emptyList())
            return
        }

        FirebaseDatabase.getInstance().getReference("Job")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val jobs = snapshot.children.mapNotNull { jobSnap ->
                        val key = jobSnap.key ?: return@mapNotNull null
                        if (key !in jobIds) return@mapNotNull null
                        jobSnap.getValue(Job::class.java)?.copy(jobId = key)
                    }
                    adapter.submitList(jobs)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }
}
