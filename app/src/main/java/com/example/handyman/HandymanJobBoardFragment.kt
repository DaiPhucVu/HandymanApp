package com.example.handyman

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.handyman.utils.SessionManager
import com.google.firebase.database.FirebaseDatabase
import java.util.UUID
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.example.handyman.MainJobBoard
import com.example.handyman.ChooseAccountType
import android.content.Intent
import android.widget.Button
import android.widget.TextView

class HandymanJobBoardFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HandymanJobBoardAdapter
    private lateinit var handymanID: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        handymanID = SessionManager.getLoggedInUserId(requireContext()) ?: ""
        val view = inflater.inflate(R.layout.fragment_handyman_job_board, container, false)

        val tvGreeting = view.findViewById<TextView>(R.id.tvGreeting)
        val userName = SessionManager.getLoggedInUserName(requireContext())
        tvGreeting.text = "Hello, $userName"

        val tvLocation = view.findViewById<TextView>(R.id.tvLocation)
        val currentCity = SessionManager.getLoggedInCity(requireContext())
        if (currentCity.isNotEmpty()) {
            tvLocation.text = currentCity
        }

        // Listen for city updates from Firebase
        val handymanRef = FirebaseDatabase.getInstance().getReference("Handyman").child(handymanID)
        handymanRef.child("city").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val city = snapshot.getValue(String::class.java)
                if (!city.isNullOrEmpty()) {
                    tvLocation.text = city
                    SessionManager.saveLoggedInCity(requireContext(), city)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        val logoutIcon = view.findViewById<View>(R.id.ivLogout)
        logoutIcon.setOnClickListener {
            // Clear session
            SessionManager.clearSession(requireContext())

            // Redirect to ChooseAccountTypeActivity
            val intent = Intent(requireContext(), ChooseAccountTypeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        adapter = HandymanJobBoardAdapter(
            onViewDetails = { job ->
                // Handle "View Details" button click
                val action = HandymanJobBoardFragmentDirections.actionHandymanJobBoardFragmentToHandymanJobBoardDetailsFragment(
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
                        jobStatus = job.jobStatus,
                        latitude = job.latitude?.toFloat() ?: 0f,
                        longitude = job.longitude?.toFloat() ?: 0f
                    )
                findNavController().navigate(action)
            },
            onQuoteJob = { job, quoteJobBttn ->
                val jobQuotesRef = FirebaseDatabase.getInstance()
                    .getReference("Job")
                    .child(job.jobId)
                    .child("quotedHandymen")

                jobQuotesRef.push()
                    .setValue(handymanID)
                    .addOnSuccessListener {
                        val handymanRef = FirebaseDatabase.getInstance()
                            .getReference("Handyman")
                            .child(handymanID)

                        handymanRef.child("quotedJobs")
                            .push()
                            .setValue(job.jobId)
                            .addOnSuccessListener {
                                handymanRef.child("allJobs")
                                    .push()
                                    .setValue(job.jobId)
                                    .addOnSuccessListener {
                                        quoteJobBttn.isEnabled = false
                                        quoteJobBttn.text = "Quoted"
                                        ViewCompat.setBackgroundTintList(
                                            quoteJobBttn,
                                            ColorStateList.valueOf(Color.parseColor("#FFCCCCCC"))
                                        )
                                        Toast.makeText(
                                            context,
                                            "Job quoted successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    .addOnFailureListener { e3 ->
                                        Toast.makeText(
                                            context,
                                            "Quoted, but failed to add to allJobs: ${e3.message}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                            }
                            .addOnFailureListener { e2 ->
                                Toast.makeText(
                                    context,
                                    "Quoted handyman, but failed to record in quotedJobs: ${e2.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }
                    .addOnFailureListener { e1 ->
                        Toast.makeText(
                            context,
                            "Failed to quote job: ${e1.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
        )
        recyclerView.adapter = adapter

        val avatar = view.findViewById<View>(R.id.ivAvatar)
        avatar.setOnClickListener {
            val intent = Intent(requireContext(), HandymanProfileActivity::class.java)
            startActivity(intent)
        }

        val btnYourJobs = view.findViewById<Button>(R.id.btnYourJobs)
        btnYourJobs.setOnClickListener {
            val action = HandymanJobBoardFragmentDirections.actionHandymanJobBoardFragmentToHandymanJobListFragment(handymanID)
            findNavController().navigate(action)
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        fetchJobsFromDatabase()
    }

    private fun fetchJobsFromDatabase() {
        val db = FirebaseDatabase.getInstance()
        val rootRef = db.reference

        // Step 1: Load cancelledJobs list for the current handyman
        rootRef.child("Handyman")
            .child(handymanID)
            .child("cancelledJobs")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(cancelSnap: DataSnapshot) {
                    val cancelledJobs = cancelSnap.children
                        .mapNotNull { it.getValue(String::class.java) }
                        .toSet()

                    // Step 2: Now load all jobs
                    rootRef.child("Job")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val availableJobs = snapshot.children.mapNotNull { child ->
                                    // 1) Deserialize the Job object
                                    val job = child.getValue(Job::class.java) ?: return@mapNotNull null

                                    // 2) Filter out inactive jobs
                                    if (job.jobStatus == "Inactive") return@mapNotNull null

                                    // 3) Get job ID
                                    val jobId = child.key ?: return@mapNotNull null

                                    // 3.5) Skip if job is assigned
                                    val assignedTo = child.child("assignedTo").getValue(String::class.java)
                                    if (!assignedTo.isNullOrEmpty()) return@mapNotNull null

                                    // 4) Skip if cancelled
                                    if (jobId in cancelledJobs) return@mapNotNull null

                                    // 5) Check quotedHandymen
                                    val quotedSnapshot = child.child("quotedHandymen")
                                    val quoted = if (quotedSnapshot.exists()) {
                                        if (quotedSnapshot.value is Map<*, *>) {
                                            (quotedSnapshot.value as Map<*, *>).values.mapNotNull { it?.toString() }
                                        } else if (quotedSnapshot.value is List<*>) {
                                            (quotedSnapshot.value as List<*>).mapNotNull { it?.toString() }
                                        } else {
                                            emptyList<String>()
                                        }
                                    } else {
                                        emptyList<String>()
                                    }

                                    // 6) Skip if already quoted
                                    if (handymanID in quoted) return@mapNotNull null

                                    // 7) Return the job
                                    job.copy(jobId = jobId)
                                }

                                adapter.submitList(availableJobs)
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e("HandymanJobBoard", "Failed to load jobs", error.toException())
                                context?.let {
                                    Toast.makeText(
                                        it,
                                        "Error loading jobs: ${error.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        })
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("HandymanJobBoard", "Failed to load cancelled jobs", error.toException())
                    context?.let {
                        Toast.makeText(
                            it,
                            "Error loading cancelled jobs: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            })
    }
}
