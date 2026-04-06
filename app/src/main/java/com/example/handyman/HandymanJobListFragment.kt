package com.example.handyman

import androidx.appcompat.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import java.util.UUID
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import androidx.navigation.fragment.navArgs
import android.widget.EditText



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
                // Handle "View Details" button click
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
            onDelete = fun(job: Job) {
                val handymanRef = FirebaseDatabase.getInstance()
                    .getReference("Handyman")
                    .child(handymanID)
                    .child("quotedJobs")

                // Check if the job is in the quotedJobs list before proceeding
                handymanRef.orderByValue().equalTo(job.jobId)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(handymanSnapshot: DataSnapshot) {
                            if (!handymanSnapshot.exists()) {
                                Toast.makeText(
                                    context,
                                    "Cannot cancel.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return
                            }

                            // Proceed with the deletion if the job exists
                            AlertDialog.Builder(requireContext())
                                .setTitle("Withdraw quote?")
                                .setMessage("Remove your quote for this job?")
                                .setPositiveButton("Yes") { _, _ ->

                                    // Reference to the quotes in Jobs
                                    val quotesRef = FirebaseDatabase.getInstance()
                                        .getReference("Job")
                                        .child(job.jobId)
                                        .child("quotedHandymen")

                                    // Find the push-key(s) whose VALUE == this handyman name
                                    quotesRef.orderByValue().equalTo(handymanID)
                                        .addListenerForSingleValueEvent(object : ValueEventListener {
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                if (!snapshot.exists()) {
                                                    Toast.makeText(
                                                        context,
                                                        "You haven’t quoted this job.",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    return
                                                }

                                                // Remove the job from the quotedHandymen list under Jobs
                                                snapshot.children.forEach { it.ref.removeValue() }

                                                // Remove the job ID from the quotedJobs list under dummyHandymen
                                                handymanSnapshot.children.forEach { it.ref.removeValue() }

                                                // Add the job ID to the cancelledJobs list of the handyman
                                                val cancelledJobsRef = FirebaseDatabase.getInstance()
                                                    .getReference("Handyman")
                                                    .child(handymanID)
                                                    .child("cancelledJobs")

                                                cancelledJobsRef.push()
                                                    .setValue(job.jobId)
                                                    .addOnSuccessListener {
                                                        // Remove from allJobs as well
                                                        val allJobsRef = FirebaseDatabase.getInstance()
                                                            .getReference("Handyman")
                                                            .child(handymanID)
                                                            .child("allJobs")

                                                        allJobsRef.orderByValue().equalTo(job.jobId)
                                                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                                    snapshot.children.forEach { it.ref.removeValue() }

                                                                    Toast.makeText(
                                                                        context,
                                                                        "Your quote was removed successfully and removed from allJobs.",
                                                                        Toast.LENGTH_SHORT
                                                                    ).show()

                                                                    // Refresh the list after full cleanup
                                                                    fetchJobsForCategory(currentCategoryKey)
                                                                }

                                                                override fun onCancelled(error: DatabaseError) {
                                                                    Toast.makeText(
                                                                        context,
                                                                        "Cancelled but failed to clean up allJobs: ${error.message}",
                                                                        Toast.LENGTH_LONG
                                                                    ).show()
                                                                }
                                                            })
                                                    }
                                                    .addOnFailureListener { e ->
                                                        Toast.makeText(
                                                            context,
                                                            "Failed to add to cancelled jobs: ${e.message}",
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                    }
                                            }

                                            override fun onCancelled(error: DatabaseError) {
                                                Toast.makeText(
                                                    context,
                                                    "Failed: ${error.message}",
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
                                "Failed to check quoted jobs: ${error.message}",
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
                    "Done" -> arrayOf()            // nothing left to do
                    else -> arrayOf("In-progress")  // everything before In-progress
                }

                if (nextStatuses.isEmpty()) {
                    Toast.makeText(context, "No further updates available", Toast.LENGTH_SHORT)
                        .show()
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
                        val jobRef = FirebaseDatabase
                            .getInstance()
                            .getReference("Job")
                            .child(job.jobId)

                        // 1) update the handyman’s status field
                        jobRef.child("jobStatusHandyman")
                            .setValue(newStatus)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    context,
                                    "Status updated to $newStatus",
                                    Toast.LENGTH_SHORT
                                ).show()

                                // 2) only once customer’s status matches do we move lists
                                jobRef.child("jobStatusCustomer")
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(snap: DataSnapshot) {
                                            val customerStatus = snap.getValue(String::class.java)
                                            if (customerStatus == newStatus) {

                                                // 3) unify the main status
                                                jobRef.child("jobStatus")
                                                    .setValue(newStatus)
                                                    .addOnSuccessListener {
                                                        if (newStatus == "Done") {
                                                            val finishedAt = java.time.LocalDateTime.now().toString()
                                                            jobRef.child("finishedBy").setValue(finishedAt)
                                                        }

                                                        if (currentCategoryKey != "allJobs") {
                                                            val updatedList =
                                                                adapter.currentList.filter { it.jobId != job.jobId }
                                                            adapter.submitList(updatedList)
                                                        }
                                                    }

                                                // 4) decide which lists to move between
                                                val (custFrom, hmFrom, toList) = when (newStatus) {
                                                    "In-progress" -> Triple(
                                                        "assignedJobs",   // customer’s old
                                                        "acceptedJobs",   // handyman’s old
                                                        "inProgressJobs"  // new
                                                    )

                                                    "Done" -> Triple(
                                                        "inProgressJobs",
                                                        "inProgressJobs",
                                                        "completedJobs"
                                                    )

                                                    else -> return
                                                }

                                                // 5) move the jobId under both nodes
                                                val custRef = FirebaseDatabase.getInstance()
                                                    .getReference("User")
                                                    .child(job.customerId)
                                                val hmRef = FirebaseDatabase.getInstance()
                                                    .getReference("Handyman")
                                                    .child(handymanID)

                                                moveJobId(custRef, custFrom, toList, job.jobId)
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
            onPaymentProceed = { job ->
                if (job.jobStatus != "Done") {
                    Toast.makeText(context, "Job is not marked as Done yet.", Toast.LENGTH_SHORT).show()
                } else {
                    val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_payment_input, null)
                    val etAmount = dialogView.findViewById<EditText>(R.id.etAmount)

                    AlertDialog.Builder(requireContext())
                        .setTitle("Proceed with Payment")
                        .setView(dialogView)
                        .setPositiveButton("Confirm") { dialogInterface, _ ->
                            val enteredAmountStr = etAmount.text.toString()

                            if (enteredAmountStr.isBlank()) {
                                Toast.makeText(context, "Amount cannot be empty", Toast.LENGTH_SHORT).show()
                                return@setPositiveButton
                            }

                            val enteredAmount = enteredAmountStr.toDoubleOrNull()
                            if (enteredAmount == null) {
                                Toast.makeText(context, "Invalid number entered", Toast.LENGTH_SHORT).show()
                                return@setPositiveButton
                            }

                            val from = job.jobSalaryFrom.toDoubleOrNull()
                            val to = job.jobSalaryTo.toDoubleOrNull()

                            val isOutOfRange = from != null && to != null && (enteredAmount < from || enteredAmount > to)

                            val proceedWithUpdate = {
                                val jobRef = FirebaseDatabase.getInstance()
                                    .getReference("Job")
                                    .child(job.jobId)

                                jobRef.child("handypay").setValue(enteredAmountStr)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Payment recorded: BDT $enteredAmountStr", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                            }

                            if (isOutOfRange) {
                                AlertDialog.Builder(requireContext())
                                    .setTitle("Amount Outside Range")
                                    .setMessage("This amount is outside the job's salary range of BDT ${from?.toInt()} - ${to?.toInt()}. Proceed anyway?")
                                    .setPositiveButton("Yes") { _, _ ->
                                        proceedWithUpdate()
                                    }
                                    .setNegativeButton("No", null)
                                    .show()
                            } else {
                                proceedWithUpdate()
                            }
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
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
                    "Quoted"      -> "quotedJobs"
                    "Accepted"  -> "acceptedJobs"
                    "All"    -> "allJobs"
                    "In-progress" -> "inProgressJobs"
                    "Done"        -> "completedJobs"
                    "Cancelled" -> "cancelledJobs"
                    else          -> return
                }
                fetchJobsForCategory(currentCategoryKey)
            }
            override fun onNothingSelected(parent: AdapterView<*>) { }
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
        val rootRef = FirebaseDatabase.getInstance().getReference("Handyman").child(handymanID)

        if (category == "allJobs") {
            // Aggregate all categories for "All" view
            val categoriesToFetch = listOf("quotedJobs", "acceptedJobs", "inProgressJobs", "completedJobs")
            val allJobIds = mutableSetOf<String>()
            var completedFetches = 0

            categoriesToFetch.forEach { cat ->
                rootRef.child(cat).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.children.mapNotNull { it.getValue(String::class.java) }.let {
                            allJobIds.addAll(it)
                        }
                        completedFetches++
                        if (completedFetches == categoriesToFetch.size) {
                            fetchJobsByIds(allJobIds.toList())
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        completedFetches++
                        if (completedFetches == categoriesToFetch.size) {
                            fetchJobsByIds(allJobIds.toList())
                        }
                    }
                })
            }
            return
        }

        // read the job‐ID list under /Handyman/{handymanId}/{category}
        val listRef = rootRef.child(category)

        Log.d("HandymanJobList", "Fetching jobs for handyman: $handymanID, category: $category")

        listRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(listSnap: DataSnapshot) {
                val jobIds = listSnap.children
                    .mapNotNull { it.getValue(String::class.java) }

                Log.d("HandymanJobList", "Found ${jobIds.size} job IDs for $category: $jobIds")
                fetchJobsByIds(jobIds)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("HandymanJobList", "Error loading $category: ${error.message}")
                Toast.makeText(context, "Error loading $category", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchJobsByIds(jobIds: List<String>) {
        if (jobIds.isEmpty()) {
            Log.d("HandymanJobList", "No job IDs to fetch.")
            adapter.submitList(emptyList())
            return
        }

        FirebaseDatabase.getInstance()
            .getReference("Job")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val jobs = snapshot.children.mapNotNull { jobSnap ->
                        val key = jobSnap.key ?: return@mapNotNull null
                        if (key !in jobIds) return@mapNotNull null

                        val parsed = jobSnap.getValue(Job::class.java) ?: return@mapNotNull null
                        Log.d("HandymanJobList", "Fetched job: ${parsed.jobId}, assignedTo: ${parsed.assignedTo}")

                        parsed.copy(jobId = key)
                    }

                    Log.d("HandymanJobList", "Submitting ${jobs.size} jobs to adapter")
                    adapter.submitList(jobs)
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("HandymanJobList", "Error loading Jobs data: ${error.message}")
                }
            })
    }
}
