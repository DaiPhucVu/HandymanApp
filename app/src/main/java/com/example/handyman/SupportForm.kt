package com.example.handyman

import android.content.Context
import com.example.handyman.utils.LocaleHelper

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class SupportForm : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrap(newBase))
    }

    private lateinit var editName: EditText
    private lateinit var editEmail: EditText
    private lateinit var editSubject: EditText
    private lateinit var editMessage: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var btnSubmit: Button

    // Canonical English values — stored in Firebase and used as the source of
    // truth. Never shown directly; see categoryDisplayLabels() for what's
    // actually rendered in the spinner.
    private val categories = listOf(
        "Technical Issue",
        "Billing & Payments",
        "Account Management",
        "Feature Request",
        "General Inquiry"
    )

    private fun categoryDisplayLabels(): List<String> = listOf(
        getString(R.string.support_category_technical_issue),
        getString(R.string.support_category_billing_payments),
        getString(R.string.support_category_account_management),
        getString(R.string.support_category_feature_request),
        getString(R.string.support_category_general_inquiry)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_support_form)

        // Initialize views
        editName = findViewById(R.id.editName)
        editEmail = findViewById(R.id.editEmail)
        editSubject = findViewById(R.id.editSubject)
        editMessage = findViewById(R.id.editMessage)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        btnSubmit = findViewById(R.id.btnSubmit)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Set up Spinner — display localized labels, but the value stored to
        // Firebase must stay the canonical English category so existing/other
        // data isn't split across languages. Read the selection by position,
        // not by the displayed text.
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryDisplayLabels())
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter

        btnSubmit.setOnClickListener {
            val name = editName.text.toString().trim()
            val email = editEmail.text.toString().trim()
            val subject = editSubject.text.toString().trim()
            val category = categories.getOrElse(spinnerCategory.selectedItemPosition) { "" }
            val message = editMessage.text.toString().trim()

            val error = FormValidator.validate(this, name, email, subject, message, category)
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val database = FirebaseDatabase.getInstance("https://handymanapplicationcos40006-default-rtdb.firebaseio.com/")
            val supportRequestsRef = database.getReference("support_requests")

            val dateTimeId = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(Date())
            val id = "REQ-$dateTimeId"

            val createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())
            val lastUpdatedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(Date())

            val supportRequest = mapOf(
                "id" to id,
                "email" to email,
                "subject" to subject,
                "message" to message,
                "category" to category,
                "status" to "Open",
                "createdAt" to createdAt,
                "lastUpdatedAt" to lastUpdatedAt
            )

            supportRequestsRef.child(id).setValue(supportRequest)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, getString(R.string.support_request_submitted_message), Toast.LENGTH_LONG).show()
                        clearForm()
                    } else {
                        Toast.makeText(this, getString(R.string.failed_to_submit_support_request_message), Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    private fun clearForm() {
        editName.text.clear()
        editEmail.text.clear()
        editSubject.text.clear()
        editMessage.text.clear()
        spinnerCategory.setSelection(0)
    }
}
