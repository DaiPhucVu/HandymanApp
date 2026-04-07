package com.example.handyman

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.database.*

class CustomerJobPaymentFragment : Fragment() {

    private val args by navArgs<CustomerJobPaymentFragmentArgs>()
    private lateinit var database: DatabaseReference
    private lateinit var requestedAmount: String
    private lateinit var jobTitleView: android.widget.TextView
    private lateinit var jobDescView: android.widget.TextView
    private lateinit var requestedAmountView: android.widget.TextView
    private lateinit var btnPayCash: android.widget.Button
    private lateinit var btnPayBkash: android.widget.Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_customer_job_payment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        jobTitleView = view.findViewById(R.id.tvJobTitle)
        jobDescView = view.findViewById(R.id.tvJobDesc)
        requestedAmountView = view.findViewById(R.id.tvRequestedAmount)
        btnPayCash = view.findViewById(R.id.btnPayCash)
        btnPayBkash = view.findViewById(R.id.btnPayBkash)
        val ivBackArrow = view.findViewById<android.widget.ImageView>(R.id.ivBackArrow)

        ivBackArrow.setOnClickListener {
            findNavController().navigateUp()
        }

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
                    Toast.makeText(context, "You cannot proceed until the handyman sets a requested amount.", Toast.LENGTH_LONG).show()
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

        btnPayCash.setOnClickListener { handlePayment("Cash") }
        btnPayBkash.setOnClickListener { handlePayment("bKash") }
    }

    private fun handlePayment(method: String) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_payment_input, null)
        val etAmount = dialogView.findViewById<EditText>(R.id.etAmount)
        val tvTitle = dialogView.findViewById<android.widget.TextView>(R.id.tvPaymentTitle)
        
        if (method == "bKash") {
            tvTitle.text = "Pay with bKash"
            etAmount.hint = "Enter amount to pay via bKash"
        } else {
            tvTitle.text = "Pay with Cash"
            etAmount.hint = "Enter amount to pay in cash"
        }

        AlertDialog.Builder(requireContext())
            .setTitle(if (method == "bKash") "bKash Payment" else "Cash Payment")
            .setView(dialogView)
            .setPositiveButton("Pay") { _, _ ->
                val enteredAmount = etAmount.text.toString().trim()

                if (enteredAmount.isBlank()) {
                    Toast.makeText(context, "Please enter a payment amount.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (enteredAmount == requestedAmount) {
                    if (method == "bKash") {
                        showBkashDetailsDialog(enteredAmount)
                    } else {
                        confirmPayment(enteredAmount, method)
                    }
                } else {
                    Toast.makeText(context, "Amount must exactly match BDT $requestedAmount", Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showBkashDetailsDialog(amount: String) {
        val input = EditText(requireContext())
        input.hint = "Enter bKash Number (e.g. 017xxxxxxxx)"
        input.inputType = InputType.TYPE_CLASS_PHONE

        AlertDialog.Builder(requireContext())
            .setTitle("bKash Payment - BDT $amount")
            .setMessage("Please enter your bKash mobile number")
            .setView(input)
            .setPositiveButton("Next") { _, _ ->
                val phone = input.text.toString().trim()
                if (phone.length == 11) {
                    showBkashPinDialog(amount)
                } else {
                    Toast.makeText(context, "Invalid bKash number", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showBkashPinDialog(amount: String) {
        val input = EditText(requireContext())
        input.hint = "Enter 5-digit PIN"
        input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD

        AlertDialog.Builder(requireContext())
            .setTitle("bKash PIN")
            .setMessage("Amount: BDT $amount\nEnter your bKash PIN to confirm")
            .setView(input)
            .setPositiveButton("Confirm") { _, _ ->
                val pin = input.text.toString().trim()
                if (pin.length == 5) {
                    confirmPayment(amount, "bKash")
                } else {
                    Toast.makeText(context, "Invalid PIN", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmPayment(amount: String, method: String) {
        val updates = mapOf(
            "custpay" to amount,
            "jobPaymentOption" to method,
            "paymentStatus" to "done"
        )

        database.updateChildren(updates).addOnSuccessListener {
            Toast.makeText(context, "Payment successful via $method!", Toast.LENGTH_SHORT).show()
            val action = CustomerJobPaymentFragmentDirections
                .actionCustomerJobPaymentFragmentToPaymentSuccessFragment(args.customerId, args.jobId)
            findNavController().navigate(action)
        }.addOnFailureListener {
            Toast.makeText(context, "Payment failed. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }
}
