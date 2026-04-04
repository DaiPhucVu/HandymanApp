package com.example.handyman

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import androidx.navigation.fragment.navArgs
import java.time.LocalDateTime
import com.example.handyman.utils.getCurrentYearMonth
import com.example.handyman.utils.incrementMetric

class CustomerJobListFragment : Fragment() {
    private var currentCategoryKey = "allJobs"
    private val args by navArgs<CustomerJobListFragmentArgs>()

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CustomerJobListAdapter
    private lateinit var customerId: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        customerId = args.customerId
        val view = inflater.inflate(R.layout.fragment_customer_job_list, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        view.findViewById<View>(R.id.btnBack).setOnClickListener {
            findNavController().popBackStack()
        }

        adapter = CustomerJobListAdapter(
            onViewDetails = { job ->
                // convert your stored String URIs back into Uri[]
                val urisArray: Array<Uri> = job.imageUris
                    .map { Uri.parse(it) }
                    .toTypedArray()

                val action =
                    CustomerJobListFragmentDirections
                        .actionCustomerJobListFragmentToCustomerJobDetailsFragment(
                            customerId   = customerId,
                            jobId        = job.jobId,
                            serviceCategory = job.jobCat,
                            problemDesc  = job.jobDesc,
                            dateFrom     = job.jobDateFrom,
                            dateTo       = job.jobDateTo,
                            timeFrom     = job.jobTimeFrom,
                            timeTo       = job.jobTimeTo,
                            location     = job.jobLocation,
                            salaryFrom   = job.jobSalaryFrom,
                            salaryTo     = job.jobSalaryTo,
                            paymentOption= job.jobPaymentOption,
                            imageUris    = urisArray,
                            assignedTo   = job.assignedTo,
                            jobStatus    = job.jobStatus
                        )
                findNavController().navigate(action)
            },
            onEdit = { job ->
                val customerRef = FirebaseDatabase.getInstance()
                    .getReference("dummyCustomers")
                    .child(customerId)
                    .child("notAssignedJobs")

                // Check if the jobId exists in notAssignedJobs (jobId is stored as a value)
                customerRef.orderByValue().equalTo(job.jobId)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (!snapshot.exists()) {
                                Toast.makeText(
                                    context,
                                    "Only jobs that are not yet assigned can be edited.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return
                            }

                            // Proceed with edit navigation
                            val urisArray: Array<Uri> = job.imageUris
                                .map { Uri.parse(it) }
                                .toTypedArray()

                            val action =
                                CustomerJobListFragmentDirections
                                    .actionCustomerJobListFragmentToJobEditFragment(
                                        customerId     = customerId,
                                        jobId          = job.jobId,
                                        serviceCategory= job.jobCat,
                                        problemDesc    = job.jobDesc,
                                        dateFrom       = job.jobDateFrom,
                                        dateTo         = job.jobDateTo,
                                        timeFrom       = job.jobTimeFrom,
                                        timeTo         = job.jobTimeTo,
                                        location       = job.jobLocation,
                                        salaryFrom     = job.jobSalaryFrom,
                                        salaryTo       = job.jobSalaryTo,
                                        paymentOption  = job.jobPaymentOption,
                                        imageUris      = urisArray,
                                        assignedTo     = job.assignedTo,
                                        jobStatus      = job.jobStatus
                                    )
                            findNavController().navigate(action)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(context, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
            },
            onDelete = fun(job: Job) {
                val customerRef = FirebaseDatabase.getInstance()
                    .getReference("dummyCustomers")
                    .child(customerId)
                    .child("notAssignedJobs")

                // Check if the job exists in the customer's notAssignedJobs list
                customerRef.orderByValue().equalTo(job.jobId)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(customerSnapshot: DataSnapshot) {
                            if (!customerSnapshot.exists()) {
                                Toast.makeText(
                                    context,
                                    "Cannot cancel.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return
                            }

                            AlertDialog.Builder(requireContext())
                                .setTitle("Delete job?")
                                .setMessage("Are you sure you want to delete this job?")
                                .setPositiveButton("Yes") { _, _ ->

                                    // Step 1: Set job status to "Inactive"
                                    val jobRef = FirebaseDatabase.getInstance()
                                        .getReference("DummyJob")
                                        .child(job.jobId)

                                    jobRef.child("jobStatus").setValue("Inactive")
                                        .addOnSuccessListener {
                                            Toast.makeText(
                                                context,
                                                "Job marked as inactive",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(
                                                context,
                                                "Failed to update job status: ${e.message}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }

                                    // Step 2: Remove the job ID from notAssignedJobs and allJobs in customer
                                    customerSnapshot.children.forEach { it.ref.removeValue() }
                                    FirebaseDatabase.getInstance()
                                        .getReference("dummyCustomers")
                                        .child(customerId)
                                        .child("allJobs")
                                        .orderByValue().equalTo(job.jobId)
                                        .addListenerForSingleValueEvent(object : ValueEventListener {
                                            override fun onDataChange(allJobsSnapshot: DataSnapshot) {
                                                allJobsSnapshot.children.forEach { it.ref.removeValue() }

                                                // Step 3: Add the job ID to the cancelledJobs list
                                                FirebaseDatabase.getInstance()
                                                    .getReference("dummyCustomers")
                                                    .child(customerId)
                                                    .child("cancelledJobs")
                                                    .push()
                                                    .setValue(job.jobId)
                                            }

                                            override fun onCancelled(error: DatabaseError) {}
                                        })

                                    // Step 4: Remove all quoted handymen from quotedHandymen list
                                    val quotesRef = jobRef.child("quotedHandymen")
                                    quotesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            val handymenList = mutableListOf<String>()
                                            snapshot.children.forEach { child ->
                                                val handymanId = child.value.toString()
                                                handymenList.add(handymanId)
                                                // Properly remove each quoted handyman
                                                child.ref.removeValue()
                                            }

                                            // Step 5: Remove the job ID from each handyman's quotedJobs and allJobs list
                                            handymenList.forEach { handymanId ->
                                                val handymanRef = FirebaseDatabase.getInstance()
                                                    .getReference("dummyHandymen")
                                                    .child(handymanId)

                                                // Iterate through both quotedJobs and allJobs lists
                                                listOf("quotedJobs", "allJobs").forEach { listType ->
                                                    handymanRef.child(listType)
                                                        .orderByValue().equalTo(job.jobId)
                                                        .addListenerForSingleValueEvent(object : ValueEventListener {
                                                            override fun onDataChange(handymanSnapshot: DataSnapshot) {
                                                                if (handymanSnapshot.exists()) {
                                                                    handymanSnapshot.children.forEach { it.ref.removeValue() }
                                                                }
                                                            }

                                                            override fun onCancelled(error: DatabaseError) {
                                                                Toast.makeText(
                                                                    context,
                                                                    "Error removing job from $listType: ${error.message}",
                                                                    Toast.LENGTH_LONG
                                                                ).show()
                                                            }
                                                        })
                                                }
                                            }

                                            // Step 6: Update the UI
                                            Toast.makeText(
                                                context,
                                                "Job has been cancelled.",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            // Update the list after deletion
//                                            val newList = adapter.currentList.filter { it.jobId != job.jobId }
//                                            adapter.submitList(newList)
                                            fetchJobsForCategory(currentCategoryKey)
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            Toast.makeText(
                                                context,
                                                "Failed to remove quoted handymen: ${error.message}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    })
                                }
                                .setNegativeButton("No", null)
                                .show()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(
                                context,
                                "Error checking job existence: ${error.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    })
            },
            onUpdate = fun(job: Job) {
                val currentStatus = job.jobStatus

                // build *only* the valid next‐step list
                val nextStatuses = when (currentStatus) {
                    "In-progress" -> arrayOf("Done")
                    "Done"        -> arrayOf()            // nothing left to do
                    else          -> arrayOf("In-progress")  // everything before In-progress
                }

                if (nextStatuses.isEmpty()) {
                    Toast.makeText(context, "No further updates available", Toast.LENGTH_SHORT).show()
                    return
                }

                // now show the dialog with only the valid choices
                var chosen = 0
                AlertDialog.Builder(requireContext())
                    .setTitle("Update status")
                    .setSingleChoiceItems(nextStatuses, 0) { _, which ->
                        chosen = which
                    }
                    .setPositiveButton("OK") { _, _ ->
                        val newStatus = nextStatuses[chosen]
                        val jobRef = FirebaseDatabase.getInstance()
                            .getReference("DummyJob")
                            .child(job.jobId)

                        jobRef.child("jobStatusCustomer")
                            .setValue(newStatus)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    context,
                                    "Status updated to $newStatus",
                                    Toast.LENGTH_SHORT
                                ).show()

                                jobRef.child("jobStatusHandyman")
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(snap: DataSnapshot) {
                                            val handymanStatus = snap.getValue(String::class.java)
                                            if (handymanStatus == newStatus) {
                                                jobRef.child("jobStatus")
                                                    .setValue(newStatus)
                                                    .addOnSuccessListener {
                                                        if (newStatus == "Done") {
                                                            //Increment "completedJobs" metric
                                                            val (yearStr, monthStr) = getCurrentYearMonth()
                                                            incrementMetric("serviceAnalytics/2025/$yearStr/$monthStr/jobsCompleted")

                                                            val finishedAt = LocalDateTime.now().toString()
                                                            jobRef.child("finishedBy").setValue(finishedAt)
                                                        }

                                                        if (currentCategoryKey != "allJobs") {
                                                            val updatedList = adapter.currentList.filter { it.jobId != job.jobId }
                                                            adapter.submitList(updatedList)
                                                        }
                                                    }

                                                val (custFrom, hmFrom, toList) = when (newStatus) {
                                                    "In-progress" -> Triple(
                                                        "assignedJobs",    // customer’s old list
                                                        "acceptedJobs",    // handyman’s old list
                                                        "inProgressJobs"   // new list
                                                    )
                                                    "Done" -> Triple(
                                                        "inProgressJobs",
                                                        "inProgressJobs",
                                                        "completedJobs"
                                                    )
                                                    else -> return
                                                }

                                                val custRef = FirebaseDatabase.getInstance()
                                                    .getReference("dummyCustomers")
                                                    .child(customerId)
                                                moveJobId(custRef, custFrom, toList, job.jobId)

                                                val hmRef = FirebaseDatabase.getInstance()
                                                    .getReference("dummyHandymen")
                                                    .child(job.assignedTo)
                                                moveJobId(hmRef, hmFrom, toList, job.jobId)

                                                fetchJobsForCategory(currentCategoryKey)
                                            }
                                        }
                                        override fun onCancelled(e: DatabaseError) {}
                                    })
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    context,
                                    "Failed to update status: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            },

            onProceedToPayment = { job ->
                if (job.jobStatus != "Done") {
                    Toast.makeText(context, "Job is not marked as Done yet.", Toast.LENGTH_SHORT).show()
                    return@CustomerJobListAdapter
                }

                val action = CustomerJobListFragmentDirections
                    .actionCustomerJobListFragmentToCustomerJobPaymentFragment(
                        customerId = customerId,
                        jobId = job.jobId
                    )
                findNavController().navigate(action)
            }
        )
        recyclerView.adapter = adapter

        val spinner = view.findViewById<Spinner>(R.id.spinnerStatus)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, v: View?, pos: Int, id: Long
            ) {
                val display = parent.getItemAtPosition(pos) as String
                currentCategoryKey = when (display) {
                    "Assigned"      -> "assignedJobs"
                    "Not assigned"  -> "notAssignedJobs"
                    "All"           -> "allJobs"
                    "In-progress"   -> "inProgressJobs"
                    "Done"          -> "completedJobs"
                    "Cancelled"     -> "cancelledJobs"
                    else            -> return
                }
                fetchJobsForCategory(currentCategoryKey)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        fetchJobsForCategory("allJobs")
        return view
    }

    private fun moveJobId(
        ref: DatabaseReference,
        fromList: String,
        toList: String,
        jobId: String
    ) {
        ref.child(fromList)
            .orderByValue()
            .equalTo(jobId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach { it.ref.removeValue() }
                    ref.child(toList)
                        .push()
                        .setValue(jobId)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun fetchJobsForCategory(category: String) {
        val listRef = FirebaseDatabase.getInstance()
            .getReference("dummyCustomers")
            .child(customerId)
            .child(category)

        listRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(listSnap: DataSnapshot) {
                val jobIds = listSnap.children
                    .mapNotNull { it.getValue(String::class.java) }
                fetchJobsByIds(jobIds)
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error loading $category", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchJobsByIds(jobIds: List<String>) {
        FirebaseDatabase.getInstance()
            .getReference("DummyJob")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val jobs = snapshot.children.mapNotNull { jobSnap ->
                        val key = jobSnap.key ?: return@mapNotNull null
                        if (key !in jobIds) return@mapNotNull null
                        val parsed = jobSnap.getValue(Job::class.java) ?: return@mapNotNull null
                        parsed.copy(jobId = key)
                    }
                    adapter.hideStatus = currentCategoryKey == "cancelledJobs"
                    adapter.submitList(jobs)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }
}