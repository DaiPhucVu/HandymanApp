package com.example.handyman

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class PaymentSuccessFragment : Fragment() {

    private val args by navArgs<PaymentSuccessFragmentArgs>()
    private lateinit var database: DatabaseReference
    private var handymanId: String? = null
    private var jobId: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_payment_success, container, false)

        database = FirebaseDatabase.getInstance().getReference()
        
        val ratingBar = view.findViewById<RatingBar>(R.id.ratingBar)
        val etComment = view.findViewById<EditText>(R.id.etReviewComment)
        val btnSubmit = view.findViewById<Button>(R.id.btnSubmitReview)
        val btnDownloadInvoice = view.findViewById<Button>(R.id.btnDownloadInvoice)

        // Using jobId from args
        jobId = args.jobId
        
        fetchHandymanId()

        btnDownloadInvoice.setOnClickListener {
            downloadInvoice()
        }

        btnSubmit.setOnClickListener {
            val rating = ratingBar.rating
            val comment = etComment.text.toString().trim()
            
            if (rating == 0f) {
                Toast.makeText(context, getString(R.string.please_select_rating_message), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (handymanId != null) {
                submitReview(handymanId!!, rating, comment)
            } else {
                Toast.makeText(context, getString(R.string.error_handyman_id_not_found_message), Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun fetchHandymanId() {
        if (jobId == null) return
        
        database.child("Job").child(jobId!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    handymanId = snapshot.child("assignedTo").getValue(String::class.java)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun downloadInvoice() {
        if (jobId == null) {
            Toast.makeText(context, getString(R.string.job_id_not_found_message), Toast.LENGTH_SHORT).show()
            return
        }

        database.child("Job").child(jobId!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val jobTitle = snapshot.child("jobCat").getValue(String::class.java) ?: "N/A"
                val amount = snapshot.child("custpay").getValue(String::class.java) ?: "0"
                val date = snapshot.child("dateFrom").getValue(String::class.java) ?: "N/A"
                val location = snapshot.child("location").getValue(String::class.java) ?: "N/A"
                val paymentMethod = snapshot.child("jobPaymentOption").getValue(String::class.java) ?: "N/A"

                val invoiceContent = getString(
                    R.string.invoice_content_format,
                    jobTitle, date, location, amount, paymentMethod
                )

                // Displaying in a dialog for now, could be saved to a file or shared
                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.your_invoice_title))
                    .setMessage(invoiceContent)
                    .setPositiveButton(getString(R.string.close_btn), null)
                    .setNeutralButton(getString(R.string.copy_to_clipboard_btn)) { _, _ ->
                        val clipboard = requireContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText(getString(R.string.invoice_clip_label), invoiceContent)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, getString(R.string.invoice_copied_message), Toast.LENGTH_SHORT).show()
                    }
                    .show()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, getString(R.string.failed_to_fetch_invoice_message), Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun submitReview(handymanId: String, rating: Float, comment: String) {
        val reviewId = database.child("Reviews").push().key ?: return
        val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(Date())
        
        val review = Review(
            reviewId = reviewId,
            jobId = jobId ?: "",
            customerId = args.customerId,
            handymanId = handymanId,
            rating = rating,
            comment = comment,
            timestamp = timestamp
        )

        database.child("Reviews").child(reviewId).setValue(review)
            .addOnSuccessListener {
                updateHandymanRating(handymanId, rating)
                Toast.makeText(context, getString(R.string.thank_you_for_review_message), Toast.LENGTH_SHORT).show()
                navigateToHome()
            }
            .addOnFailureListener {
                Toast.makeText(context, getString(R.string.failed_to_submit_review_message), Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateHandymanRating(handymanId: String, newRating: Float) {
        val handymanRef = database.child("Handyman").child(handymanId)
        handymanRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val currentRating = (mutableData.child("averageRating").getValue(Double::class.java) ?: 0.0)
                val currentCount = (mutableData.child("reviewCount").getValue(Int::class.java) ?: 0)
                
                val newCount = currentCount + 1
                val newAverage = ((currentRating * currentCount) + newRating.toDouble()) / newCount
                
                mutableData.child("averageRating").value = newAverage
                mutableData.child("reviewCount").value = newCount
                
                return Transaction.success(mutableData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                if (error != null) {
                    // Log error
                }
            }
        })
    }

    private fun navigateToHome() {
        val action = PaymentSuccessFragmentDirections
            .actionPaymentSuccessFragmentToCustomerJobListFragment(args.customerId)
        findNavController().navigate(action)
    }
}
