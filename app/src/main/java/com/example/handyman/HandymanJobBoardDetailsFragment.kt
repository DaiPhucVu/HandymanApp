package com.example.handyman

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.handyman.chatbox.ChatClientActivity
import com.example.handyman.utils.SessionManager
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class HandymanJobBoardDetailsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_handyman_job_board_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ImageView>(R.id.ivBack).setOnClickListener {
            findNavController().navigateUp()
        }

        val args = HandymanJobBoardDetailsFragmentArgs.fromBundle(requireArguments())

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

        val jobTitle = view.findViewById<TextView>(R.id.tvJobTitle)
        jobTitle.text = serviceName
        val salaryDisplay = view.findViewById<TextView>(R.id.tvPrice)
        if (salaryFrom != "" && salaryTo != "") {
            if (paymentOption == "Per Day") {
                salaryDisplay.text = "BDT $salaryFrom-$salaryTo/day"
            } else {
                salaryDisplay.text = "BDT $salaryFrom-$salaryTo"
            }
        } else {
            salaryDisplay.text = "To be negotiated"
        }
        val jobDescDisplay = view.findViewById<TextView>(R.id.tvJobSubtitle)
        jobDescDisplay.text = jobDescription
        val dateDisplay = view.findViewById<TextView>(R.id.tvDate)
        if (dateFrom == dateTo) {
            dateDisplay.text = "$dateFrom"
        } else {
            dateDisplay.text = "$dateFrom — $dateTo"
        }
        val timeDisplay = view.findViewById<TextView>(R.id.tvTime)
        timeDisplay.text = "$timeFrom — $timeTo"
        val locationDisplay = view.findViewById<TextView>(R.id.tvAddress)
        locationDisplay.text = "$location, Melbourne, VIC"

        val btnMessage: Button = view.findViewById(R.id.btnMessage)

        val btnReturn: Button = view.findViewById(R.id.btnReturn)

        btnMessage.setOnClickListener {
            val context = requireContext()

            // Fetch document from Firestore that contains chatroom of job
            val chatRef = FirebaseFirestore.getInstance().collection("chats").document(jobId)
            chatRef.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // Load memberInfos array from document
                    val memberInfos: List<Map<String, String>> = documentSnapshot.get("memberInfos") as List<Map<String, String>>

                    // Loop through all maps in memberInfos to check if this chatroom belongs to current user
                    for ((index, member) in memberInfos.withIndex()) {
                        if (member["uid"] == SessionManager.currentUserID) {
                            // If current user is in the second (last) map of array
                            // open ChatClientActivity with information of the other user
                            // (whose information should be stored in first map of array)
                            if (index == memberInfos.size - 1) {
                                val intent = Intent(context, ChatClientActivity::class.java).apply {
                                    putExtra("chatID", jobId)
                                    putExtra("uid", memberInfos[0]["uid"])
                                    putExtra("username", memberInfos[0]["username"])
                                }
                                context.startActivity(intent)
                            }
                            else {
                                // Else if current user is in the first map of array
                                // open ChatClientActivity with information of the other user
                                // (whose information should be stored in second (last) map of array)
                                val intent = Intent(context, ChatClientActivity::class.java).apply {
                                    putExtra("chatID", jobId)
                                    putExtra("uid", memberInfos[1]["uid"])
                                    putExtra("username", memberInfos[1]["username"])
                                }
                                context.startActivity(intent)
                            }
                        }
                    }
                }
                else {
                    Log.e(
                        "PrototypeIssue",
                        "Due to limitations during development of this prototype\n" +
                                "The chatroom for this job is *not* automatically created on the Firestore\n" +
                                "The chatroom must be manually added by creating a new document with id the same as this job id: $jobId \n" +
                                "Add into the document a String field named 'chatID' whose value is the same as the document id\n" +
                                "Then add user infos by including an array that contains two maps, each map containing a 'uid' key and a 'username' key\n" +
                                "The 'uid' values must match the users that are part of the job (1 customer and 1 handyman)\n" +
                                "The chat feature does not currently support more than two members per chatroom"
                    )
                    Toast.makeText(context, "Please read the log with tag 'PrototypeIssue'", Toast.LENGTH_LONG).show()
                }
            }
        }

        // Set a click listener on the return button
        btnReturn.setOnClickListener {
            findNavController().navigateUp()
        }

        displayJobImages(view, jobId)
    }

    private fun displayJobImages(rootView: View, jobId: String) {
        // Get references to your HorizontalScrollView and its LinearLayout container.
        val photoScroll = rootView.findViewById<HorizontalScrollView>(R.id.photoScroll)
        val photosContainer = rootView.findViewById<LinearLayout>(R.id.attachPhotosContainer)

        // Create a reference to the folder "jobImages/<jobId>" in Firebase Storage.
        val storageRef = FirebaseStorage.getInstance().getReference("jobImages").child(jobId)

        // List all files in that folder.
        storageRef.listAll()
            .addOnSuccessListener { listResult ->
                val downloadUrlTasks = mutableListOf<com.google.android.gms.tasks.Task<Uri>>()

                // Add a download URL task for each item found.
                listResult.items.forEach { itemRef ->
                    downloadUrlTasks.add(itemRef.downloadUrl)
                }

                // Wait for all download URL tasks to succeed.
                Tasks.whenAllSuccess<Uri>(downloadUrlTasks)
                    .addOnSuccessListener { uriList ->
                        // Convert URI list to a list of string URLs.
                        val imageUrls = uriList.map { it.toString() }

                        // Show or hide the scroll view based on whether we have images.
                        photoScroll.visibility = if (imageUrls.isNotEmpty()) View.VISIBLE else View.GONE

                        // Clear any existing views in the container.
                        photosContainer.removeAllViews()

                        // Dynamically create ImageViews and load the images using Glide.
                        for (url in imageUrls) {
                            val imageView = ImageView(requireContext())
                            val params = LinearLayout.LayoutParams(400, 400)
                            params.setMargins(8, 8, 8, 8)
                            imageView.layoutParams = params
                            imageView.scaleType = ImageView.ScaleType.CENTER_CROP

                            Glide.with(this)
                                .load(url)
                                .into(imageView)

                            // Add the ImageView to the LinearLayout container.
                            photosContainer.addView(imageView)
                        }
                    }
                    .addOnFailureListener { exception ->
                        // Handle error while getting download URLs.
                        photoScroll.visibility = View.GONE
                    }
            }
            .addOnFailureListener { exception ->
                // Handle error when listing files.
                photoScroll.visibility = View.GONE
            }
    }
}