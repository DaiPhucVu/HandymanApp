package com.example.handyman

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.Spinner
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import java.util.Calendar
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.FileProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.time.LocalDateTime
import java.util.UUID

class JobEditFragment : Fragment() {

    // URLs we loaded from Storage at startup:
    private val originalImageUrls   = mutableListOf<String>()
    // URIs the user just picked in this session:
    private val newImageUris        = mutableListOf<Uri>()
    // Originals the user has removed by tapping “✕”:
    private val removedOriginalUrls = mutableListOf<String>()

    private lateinit var addPhoto: ImageView
    private lateinit var photoFrame: FrameLayout
    private lateinit var placeholderContainer: LinearLayout
    private lateinit var photoScroll: HorizontalScrollView
    private lateinit var attachPhotosContainer: LinearLayout
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>

    private var cameraImageUri: Uri? = null
//    private val imageUris = mutableListOf<Uri>()

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { pickedUri ->
            newImageUris.add(pickedUri)
            // Hide placeholder and show the ScrollView when a photo is selected
            photoFrame.background = null
            placeholderContainer.visibility = View.GONE
            photoScroll.visibility = View.VISIBLE

            // Inflate thumbnail layout
            val thumbnailView = layoutInflater.inflate(R.layout.photo_thumbnail, attachPhotosContainer, false)
            val ivThumbnail = thumbnailView.findViewById<ImageView>(R.id.ivThumbnail)

            Glide.with(requireContext())
                .load(uri)
                .into(ivThumbnail)

            // Delete button
            val ivDelete = thumbnailView.findViewById<ImageView>(R.id.ivDelete)
            ivDelete.setOnClickListener {
                attachPhotosContainer.removeView(thumbnailView)
                newImageUris.remove(pickedUri)
                if (attachPhotosContainer.childCount == 0) {
                    photoFrame.setBackgroundResource(R.drawable.frame)
                    placeholderContainer.visibility = View.VISIBLE
                    photoScroll.visibility = View.GONE
                }
            }

            // Add thumbnail to container
            attachPhotosContainer.addView(thumbnailView)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_job_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && cameraImageUri != null) {
                newImageUris.add(cameraImageUri!!)
                // The image was captured successfully.
                // You can add the image thumbnail just like the gallery selection.
                photoFrame.background = null
                placeholderContainer.visibility = View.GONE
                photoScroll.visibility = View.VISIBLE

                val thumbnailView = layoutInflater.inflate(R.layout.photo_thumbnail, attachPhotosContainer, false)
                val ivThumbnail = thumbnailView.findViewById<ImageView>(R.id.ivThumbnail)

                Glide.with(requireContext())
                    .load(cameraImageUri)
                    .into(ivThumbnail)

                val ivDelete = thumbnailView.findViewById<ImageView>(R.id.ivDelete)
                ivDelete.setOnClickListener {
                    attachPhotosContainer.removeView(thumbnailView)
                    newImageUris.remove(cameraImageUri)  // Remove from our list too
                    if (attachPhotosContainer.childCount == 0) {
                        photoFrame.setBackgroundResource(R.drawable.frame)
                        placeholderContainer.visibility = View.VISIBLE
                        photoScroll.visibility = View.GONE
                    }
                }

                attachPhotosContainer.addView(thumbnailView)
            }
        }

        val args = JobEditFragmentArgs.fromBundle(requireArguments())
        val customerId = args.customerId
        val jobId = args.jobId
        val serviceName = args.serviceCategory
        val jobDescription = args.problemDesc
        val dateFrom = args.dateFrom
        val dateTo = args.dateTo
        val timeFrom = args.timeFrom
        val timeTo = args.timeTo
        val location = args.location
        val salaryFrom = args.salaryFrom
        val salaryTo = args.salaryTo
        val paymentOption = args.paymentOption
        val assignedTo = args.assignedTo
        val jobStatus = args.jobStatus

        val etDescribeProblem = view.findViewById<EditText>(R.id.etDescribeProblem)
        etDescribeProblem.setText(jobDescription)
        val etDateFrom = view.findViewById<EditText>(R.id.etDateFrom)
        etDateFrom.setText(dateFrom)
        val etDateTo = view.findViewById<EditText>(R.id.etDateTo)
        etDateTo.setText(dateTo)
        val etTimeFrom = view.findViewById<EditText>(R.id.etTimeFrom)
        etTimeFrom.setText(timeFrom)
        val etTimeTo = view.findViewById<EditText>(R.id.etTimeTo)
        etTimeTo.setText(timeTo)
        val spinner = view.findViewById<Spinner>(R.id.spinnerLocation)
        addPhoto = view.findViewById(R.id.ivAddPhoto)
        photoFrame = view.findViewById(R.id.photoFrame)
        placeholderContainer = view.findViewById(R.id.placeholderContainer)
        photoScroll = view.findViewById(R.id.photoScroll)
        attachPhotosContainer = view.findViewById(R.id.attachPhotosContainer)

        val storage = Firebase.storage
        val imagesFolderRef = storage.reference.child("jobImages/$jobId")

        imagesFolderRef.listAll()
            .addOnSuccessListener { listResult ->
                if (listResult.items.isNotEmpty()) {
                    // hide the “tap to add” placeholder
                    photoFrame.background = null
                    placeholderContainer.visibility = View.GONE
                    // show the scroll view
                    photoScroll.visibility = View.VISIBLE
                }
                listResult.items.forEach { fileRef ->
                    fileRef.downloadUrl
                        .addOnSuccessListener { uri ->
                            val url = uri.toString()
                            originalImageUrls += url

                            val thumb = layoutInflater.inflate(
                                R.layout.photo_thumbnail,
                                attachPhotosContainer,
                                false
                            )
                            val ivThumb = thumb.findViewById<ImageView>(R.id.ivThumbnail)
                            val ivDelete = thumb.findViewById<ImageView>(R.id.ivDelete)

                            Glide.with(requireContext())
                                .load(uri)
                                .into(ivThumb)

                            ivDelete.setOnClickListener {
                                removedOriginalUrls += url
                                originalImageUrls.remove(url)
                                attachPhotosContainer.removeView(thumb)
                                if (attachPhotosContainer.childCount == 0) {
                                    photoFrame.setBackgroundResource(R.drawable.frame)
                                    placeholderContainer.visibility = View.VISIBLE
                                    photoScroll.visibility = View.GONE
                                }
                            }

                            attachPhotosContainer.addView(thumb)
                        }
                        .addOnFailureListener { e ->

                        }
                }
            }
            .addOnFailureListener { e ->

            }

        etDateFrom.setOnClickListener {
            // Get current date as default
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                // Format selected date and set to EditText
                // Note: selectedMonth is zero-indexed
                val formattedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
                Log.d("JobPostingFragment", "Chosen date from: $formattedDate")
                etDateFrom.setText(formattedDate)
            }, year, month, day)

            datePickerDialog.show()
        }

        etDateTo.setOnClickListener {
            // Get current date as default
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                // Format selected date and set to EditText
                // Note: selectedMonth is zero-indexed
                val formattedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
                Log.d("JobPostingFragment", "Chosen date to: $formattedDate")
                etDateTo.setText(formattedDate)
            }, year, month, day)

            datePickerDialog.show()
        }

        etTimeFrom.setOnClickListener {
            // Get current time as default
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val timePickerDialog = TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
                // Format selected time and set to EditText
                val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                Log.d("JobPostingFragment", "Chosen time: $formattedTime")
                etTimeFrom.setText(formattedTime)
            }, hour, minute, true) // true for 24-hour format; false for 12-hour format

            timePickerDialog.show()
        }

        etTimeTo.setOnClickListener {
            // Get current time as default
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val timePickerDialog = TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
                // Format selected time and set to EditText
                val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                Log.d("JobPostingFragment", "Chosen time: $formattedTime")
                etTimeTo.setText(formattedTime)
            }, hour, minute, true) // true for 24-hour format; false for 12-hour format

            timePickerDialog.show()
        }

        val etFromSalary = view.findViewById<EditText>(R.id.etFromSalary)
        if (args.salaryFrom.isNotBlank()) {
            etFromSalary.setText(salaryFrom)
        }
        val etToSalary = view.findViewById<EditText>(R.id.etToSalary)
        if (args.salaryTo.isNotBlank()) {
            etToSalary.setText(salaryTo)
        }
        val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroupExample)
        if (salaryFrom.isNotBlank() && salaryTo.isNotBlank()) {
            when (paymentOption) {
                "Per Day" -> radioGroup.check(R.id.radioOption1)
                "Job Completed" -> radioGroup.check(R.id.radioOption2)
                else -> radioGroup.clearCheck()
            }
        }
        val checkBox = view.findViewById<CheckBox>(R.id.checkboxExample)
        if (salaryFrom.isBlank() && salaryTo.isBlank()) {
            checkBox.isChecked = true
            etFromSalary.isEnabled = false
            etToSalary.isEnabled = false
            for (i in 0 until radioGroup.childCount) {
                radioGroup.getChildAt(i).isEnabled = false
            }
        }

        checkBox.setOnCheckedChangeListener { _, isChecked ->
            etFromSalary.isEnabled = !isChecked
            etToSalary.isEnabled = !isChecked
            for (i in 0 until radioGroup.childCount) {
                radioGroup.getChildAt(i).isEnabled = !isChecked
            }
            if (isChecked) {
                etFromSalary.text.clear()
                etToSalary.text.clear()
                radioGroup.clearCheck()
            }
        }

        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.location_array,
            android.R.layout.simple_spinner_item
        ).also { adap ->
            adap.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinner.adapter = adapter

        val position = adapter.getPosition(location)
        if (position >= 0) {
            spinner.setSelection(position)
        }

        addPhoto.setOnClickListener { pickImageLauncher.launch("image/*") }

        val ivTakePhoto = view.findViewById<ImageView>(R.id.ivTakePhoto)
        ivTakePhoto.setOnClickListener {
            cameraImageUri = createImageFileUri()
            takePictureLauncher.launch(cameraImageUri)
        }

        val cancel = view.findViewById<Button>(R.id.btnCancel)
        cancel.setOnClickListener {
            findNavController().navigateUp()
        }
        val update = view.findViewById<Button>(R.id.btnUpdate)
        update.setOnClickListener {
            if (etDescribeProblem.text.isBlank()) {
                Toast.makeText(requireContext(), "Please describe your problem", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            if (etDateFrom.text.isBlank()) {
                Toast.makeText(requireContext(), "Please select a start date", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (etDateTo.text.isBlank()) {
                Toast.makeText(requireContext(), "Please select an end date", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (etTimeFrom.text.isBlank()) {
                Toast.makeText(requireContext(), "Please select a start time", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            if (etTimeTo.text.isBlank()) {
                Toast.makeText(requireContext(), "Please select an end time", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            if (etFromSalary.text.isNotBlank() && etToSalary.text.isNotBlank()) {
                if (etFromSalary.text.toString().toInt() >= etToSalary.text.toString().toInt()) {
                    Toast.makeText(
                        requireContext(),
                        "Please select an appropriate salary range",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                if (radioGroup.checkedRadioButtonId == -1) {
                    Toast.makeText(
                        requireContext(),
                        "Please select an option for salary payment",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
            }
            if (radioGroup.checkedRadioButtonId != -1) {
                if (etFromSalary.text.isBlank() or etToSalary.text.isBlank()) {
                    Toast.makeText(
                        requireContext(),
                        "Please choose a salary",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
            }

            if (spinner.selectedItem.toString() == "Select a location") {
                Toast.makeText(requireContext(), "Please select a location", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val jobDescription = etDescribeProblem.text.toString()
            val dateFrom = etDateFrom.text.toString()
            val dateTo = etDateTo.text.toString()
            val timeFrom = etTimeFrom.text.toString()
            val timeTo = etTimeTo.text.toString()
            val location = spinner.selectedItem.toString()
            val salaryFrom = if (etFromSalary.text.isNotBlank()) {
                etFromSalary.text.toString()
            } else {
                ""
            }
            val salaryTo = if (etToSalary.text.isNotBlank()) {
                etToSalary.text.toString()
            } else {
                ""
            }
            val paymentOption = if (radioGroup.checkedRadioButtonId != -1) {
                val selectedRadio =
                    view.findViewById<android.widget.RadioButton>(radioGroup.checkedRadioButtonId)
                selectedRadio.text.toString()
            } else {
                ""
            }

            val updates = mapOf<String, Any>(
                "jobDesc"   to jobDescription,
                "jobDateFrom"      to dateFrom,
                "jobDateTo"        to dateTo,
                "jobTimeFrom"      to timeFrom,
                "jobTimeTo"        to timeTo,
                "jobLocation"      to location,
                "jobSalaryFrom"    to salaryFrom,
                "jobSalaryTo"      to salaryTo,
                "jobPaymentOption" to paymentOption,
                "lastUpdate"       to LocalDateTime.now().toString()
            )

            val dbRef = Firebase.database
                .getReference("Job")
                .child(jobId)

            dbRef.updateChildren(updates)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Details saved", Toast.LENGTH_SHORT).show()

                    val storageRef = Firebase.storage
                        .reference
                        .child("jobImages/$jobId")

                    removedOriginalUrls.forEach { url ->
                        Firebase.storage
                            .getReferenceFromUrl(url)
                            .delete()
                    }

                    newImageUris.forEach { uri ->
                        val newRef = storageRef.child("${UUID.randomUUID()}.jpg")
                        newRef.putFile(uri)
                            .addOnSuccessListener {
                                // (Optional) retrieve downloadUrl and save to RTDB
                            }
                            .addOnFailureListener { e ->
                                Log.w("JobEditFragment", "Upload failed for $uri", e)
                            }
                    }

                    findNavController().navigateUp()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(),
                        "Failed to save: ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }

    }

    private fun createImageFileUri(): Uri {
        val fileName = "${UUID.randomUUID()}.jpg"
        // Create a file in the cache directory
        val file = File(requireContext().cacheDir, fileName)
        // Get the content URI using FileProvider
        return FileProvider.getUriForFile(requireContext(), "com.example.handyman.fileprovider", file)
    }
}
