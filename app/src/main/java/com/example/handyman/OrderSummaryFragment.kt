package com.example.handyman

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.time.LocalDateTime
import java.util.UUID

class OrderSummaryFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_order_summary, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val loadingOverlay = view.findViewById<FrameLayout>(R.id.loadingOverlay)

        val args = OrderSummaryFragmentArgs.fromBundle(requireArguments())
        val customerId    = args.customerId
        val serviceName   = args.serviceCategory
        val jobDescription= args.problemDesc
        val dateFrom      = args.dateFrom
        val dateTo        = args.dateTo
        val timeFrom      = args.timeFrom
        val timeTo        = args.timeTo
        val location      = args.location
        val salaryFrom    = args.salaryFrom
        val salaryTo      = args.salaryTo
        val paymentOption = args.paymentOption

        view.findViewById<TextView>(R.id.tvJobTitle).text = serviceName
        val salaryDisplay = view.findViewById<TextView>(R.id.tvPrice)
        if (salaryFrom.isNotBlank() && salaryTo.isNotBlank()) {
            salaryDisplay.text = if (paymentOption == "Per Day")
                "BDT $salaryFrom-$salaryTo/day"
            else
                "BDT $salaryFrom-$salaryTo"
        } else {
            salaryDisplay.text = "To be negotiated"
        }
        view.findViewById<TextView>(R.id.tvJobSubtitle).text = jobDescription
        view.findViewById<TextView>(R.id.tvDate).text = if (dateFrom == dateTo)
            dateFrom else "$dateFrom — $dateTo"
        view.findViewById<TextView>(R.id.tvTime).text = "$timeFrom — $timeTo"
        view.findViewById<TextView>(R.id.tvAddress).text = location

        view.findViewById<Button>(R.id.btnSubmitRequest)
            .setOnClickListener {
                loadingOverlay.visibility = View.VISIBLE

                val jobId = UUID.randomUUID().toString()
                val job = Job(
                    jobId            = jobId,
                    createdAt        = LocalDateTime.now().toString(),
                    customerId       = customerId,
                    jobCat           = serviceName,
                    jobDesc          = jobDescription,
                    jobDateFrom      = dateFrom,
                    jobDateTo        = dateTo,
                    jobTimeFrom      = timeFrom,
                    jobTimeTo        = timeTo,
                    jobLocation      = location,
                    jobSalaryFrom    = salaryFrom,
                    jobSalaryTo      = salaryTo,
                    jobPaymentOption = paymentOption,
                    paymentStatus = "",
                    imageUris        = args.imageUris?.map { it.toString() } ?: emptyList()
                )

                val dbRef    = FirebaseDatabase.getInstance().reference
                val custPath = "User/$customerId"
                val allKey   = dbRef.child(custPath).child("allJobs").push().key!!
                val naKey    = dbRef.child(custPath).child("notAssignedJobs").push().key!!

                val updates = mapOf(
                    "/Job/$jobId"               to job,
                    "$custPath/allJobs/$allKey"        to jobId,
                    "$custPath/notAssignedJobs/$naKey" to jobId
                )

                dbRef.updateChildren(updates)
                    .addOnSuccessListener {
                        // 1) Kick off all the uploads and collect their Tasks
                        val uploadTasks: List<com.google.android.gms.tasks.Task<*>> =
                            args.imageUris.orEmpty().mapIndexed { idx, uri ->
                                val imageRef: StorageReference = FirebaseStorage
                                    .getInstance()
                                    .getReference("jobImages/$jobId/image_$idx.jpg")

                                imageRef.putFile(uri)
                                    .addOnSuccessListener {

                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(
                                            context,
                                            "Image #$idx failed: ${e.message}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                            }

                        // 2) Wait for all of them to at least _start_/complete
                        Tasks.whenAll(uploadTasks)
                            .addOnSuccessListener {
                                loadingOverlay.visibility = View.GONE
                                findNavController().navigate(
                                    OrderSummaryFragmentDirections
                                        .actionOrderSummaryFragmentToJobRequestDoneFragment()
                                )
                            }
                            .addOnFailureListener {
                                loadingOverlay.visibility = View.GONE
                                Toast.makeText(
                                    context,
                                    "One or more image uploads failed",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }
                    .addOnFailureListener { e ->
                        loadingOverlay.visibility = View.GONE
                        Toast.makeText(
                            context,
                            "Failed to post job: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }

        view.findViewById<Button>(R.id.btnCancel)
            .setOnClickListener {
                findNavController().navigate(
                    OrderSummaryFragmentDirections
                        .actionOrderSummaryFragmentToServiceCategoryFragment()
                )
            }
    }
}
