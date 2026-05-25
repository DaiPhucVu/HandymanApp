package com.example.handyman

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.handyman.payment.InitPaymentRequest
import com.example.handyman.payment.PaymentApi
import com.google.firebase.database.*
import kotlinx.coroutines.launch

class CustomerJobPaymentFragment : Fragment() {

    private val args by navArgs<CustomerJobPaymentFragmentArgs>()
    private lateinit var database: DatabaseReference
    private lateinit var requestedAmount: String

    private lateinit var jobTitleView: android.widget.TextView
    private lateinit var jobDescView: android.widget.TextView
    private lateinit var requestedAmountView: android.widget.TextView
    private lateinit var btnPayCash: android.widget.Button
    private lateinit var btnPayBkash: android.widget.Button

    // Listener for the IPN-driven paymentStatus change
    private var jobStatusListener: ValueEventListener? = null
    private var awaitingPayment: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.fragment_customer_job_payment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        jobTitleView = view.findViewById(R.id.tvJobTitle)
        jobDescView = view.findViewById(R.id.tvJobDesc)
        requestedAmountView = view.findViewById(R.id.tvRequestedAmount)
        btnPayCash = view.findViewById(R.id.btnPayCash)
        btnPayBkash = view.findViewById(R.id.btnPayBkash)
        val ivBackArrow = view.findViewById<android.widget.ImageView>(R.id.ivBackArrow)

        ivBackArrow.setOnClickListener { findNavController().navigateUp() }

        database = FirebaseDatabase.getInstance().getReference("Job").child(args.jobId)

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val jobTitle = snapshot.child("jobCat").getValue(String::class.java) ?: "Unknown Job"
                val jobDesc = snapshot.child("jobDesc").getValue(String::class.java) ?: ""
                requestedAmount = snapshot.child("handypay").getValue(String::class.java) ?: ""

                jobTitleView.text = jobTitle
                jobDescView.text = jobDesc

                if (requestedAmount.isBlank()) {
                    requestedAmountView.text = "Handyman has not set a requested amount yet."
                    btnPayCash.isEnabled = false
                    btnPayBkash.isEnabled = false
                    Toast.makeText(
                        context,
                        "You cannot proceed until the handyman sets a requested amount.",
                        Toast.LENGTH_LONG,
                    ).show()
                } else {
                    requestedAmountView.text = "Requested Amount: BDT $requestedAmount"
                    btnPayCash.isEnabled = true
                    btnPayBkash.isEnabled = true
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load job data.", Toast.LENGTH_SHORT).show()
            }
        })

        btnPayCash.setOnClickListener { handleCashPayment() }
        btnPayBkash.setOnClickListener { handleBkashPayment() }
    }

    // ---------------- Cash flow (unchanged from before) ----------------

    private fun handleCashPayment() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_payment_input, null)
        val etAmount = dialogView.findViewById<EditText>(R.id.etAmount)
        dialogView.findViewById<android.widget.TextView>(R.id.tvPaymentTitle).text = "Pay with Cash"
        etAmount.hint = "Enter amount to pay in cash"

        AlertDialog.Builder(requireContext())
            .setTitle("Cash Payment")
            .setView(dialogView)
            .setPositiveButton("Pay") { _, _ ->
                val entered = etAmount.text.toString().trim()
                if (entered.isBlank()) {
                    Toast.makeText(context, "Please enter a payment amount.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (entered != requestedAmount) {
                    Toast.makeText(
                        context,
                        "Amount must exactly match BDT $requestedAmount",
                        Toast.LENGTH_LONG,
                    ).show()
                    return@setPositiveButton
                }
                writeCashPayment(entered)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun writeCashPayment(amount: String) {
        val updates = mapOf(
            "custpay" to amount,
            "jobPaymentOption" to "Cash",
            "paymentStatus" to "done",
        )
        database.updateChildren(updates)
            .addOnSuccessListener {
                Toast.makeText(context, "Payment recorded.", Toast.LENGTH_SHORT).show()
                navigateToSuccess()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Payment failed. Please try again.", Toast.LENGTH_SHORT).show()
            }
    }

    // ---------------- bKash flow via SSLCommerz sandbox ----------------

    private fun handleBkashPayment() {
        if (requestedAmount.isBlank()) {
            Toast.makeText(context, "No amount set yet.", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Pay with bKash")
            .setMessage("You'll be taken to the secure bKash checkout for BDT $requestedAmount.\n\nTap Continue to proceed.")
            .setPositiveButton("Continue") { _, _ -> startSslCommerzCheckout() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun startSslCommerzCheckout() {
        val progress = ProgressDialog(requireContext()).apply {
            setMessage("Preparing secure checkout…")
            setCancelable(false)
            show()
        }

        val amountValue = requestedAmount.toDoubleOrNull()
        if (amountValue == null) {
            progress.dismiss()
            Toast.makeText(context, "Invalid amount.", Toast.LENGTH_SHORT).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val resp = PaymentApi.service.initPayment(
                    InitPaymentRequest(
                        jobId = args.jobId,
                        customerId = args.customerId,
                        amount = amountValue,
                    ),
                )
                progress.dismiss()
                // Start watching for the IPN to flip paymentStatus to "done"
                startWatchingJobStatus()
                openCheckoutTab(resp.GatewayPageURL)
            } catch (e: Exception) {
                progress.dismiss()
                Toast.makeText(
                    context,
                    "Could not start payment: ${e.localizedMessage}",
                    Toast.LENGTH_LONG,
                ).show()
            }
        }
    }

    private fun openCheckoutTab(url: String) {
        val tab = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
        tab.launchUrl(requireContext(), Uri.parse(url))
    }

    /**
     * The Firebase Function updates Job/<id>/paymentStatus when the IPN arrives.
     * We listen for that change instead of trusting the redirect.
     */
    private fun startWatchingJobStatus() {
        awaitingPayment = true
        jobStatusListener?.let { database.removeEventListener(it) }
        jobStatusListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!awaitingPayment) return
                val status = snapshot.child("paymentStatus").getValue(String::class.java)
                when (status) {
                    "done" -> {
                        awaitingPayment = false
                        stopWatchingJobStatus()
                        Toast.makeText(
                            context,
                            "bKash payment confirmed.",
                            Toast.LENGTH_SHORT,
                        ).show()
                        navigateToSuccess()
                    }
                    "failed" -> {
                        awaitingPayment = false
                        stopWatchingJobStatus()
                        Toast.makeText(
                            context,
                            "Payment failed or cancelled.",
                            Toast.LENGTH_LONG,
                        ).show()
                    }
                    // "processing" — keep waiting
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // ignore
            }
        }
        database.addValueEventListener(jobStatusListener!!)
    }

    private fun stopWatchingJobStatus() {
        jobStatusListener?.let { database.removeEventListener(it) }
        jobStatusListener = null
    }

    private fun navigateToSuccess() {
        val action = CustomerJobPaymentFragmentDirections
            .actionCustomerJobPaymentFragmentToPaymentSuccessFragment(args.customerId, args.jobId)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopWatchingJobStatus()
    }
}
