package com.example.handyman

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
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
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.handyman.chatbox.ChatClientActivity
import com.example.handyman.utils.SessionManager
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import android.location.Geocoder
import java.util.UUID
import java.util.Locale

class HandymanJobListDetailsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_handyman_job_list_details, container, false)
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
        val argLat = args.latitude
        val argLng = args.longitude

        val jobTitle = view.findViewById<TextView>(R.id.tvJobTitle)
        jobTitle.text = if (!serviceName.isNullOrEmpty()) serviceName else "Untitled Job"
        val salaryDisplay = view.findViewById<TextView>(R.id.tvPrice)
        val jobRef = FirebaseDatabase.getInstance().getReference("Job").child(jobId)

        val jobDescDisplay = view.findViewById<TextView>(R.id.tvJobSubtitle)
        jobDescDisplay.text = if (!jobDescription.isNullOrEmpty()) jobDescription else ""
        val dateDisplay = view.findViewById<TextView>(R.id.tvDate)
        if (dateFrom == dateTo) {
            dateDisplay.text = "$dateFrom"
        } else {
            dateDisplay.text = "$dateFrom — $dateTo"
        }
        val timeDisplay = view.findViewById<TextView>(R.id.tvTime)
        timeDisplay.text = "$timeFrom — $timeTo"
        val locationDisplay = view.findViewById<TextView>(R.id.tvAddress)
        val isAssigned = assignedTo == SessionManager.getLoggedInUserId(requireContext())
        
        // Removed initial redundant jobRef.get() here as it's handled in the main map update block
        locationDisplay.text = "Loading location..."

        val customerNameDisplay = view.findViewById<TextView>(R.id.tvTitle)
        val customerRatingDisplay = view.findViewById<TextView>(R.id.tvRating)
        val btnViewProfile = view.findViewById<Button>(R.id.btnViewProfile)
        val ivProfile = view.findViewById<ImageView>(R.id.ivProfile)
        
        customerNameDisplay.text = "Loading..."
        
        // Fetch customer name and rating from Firebase
        val userRef = FirebaseDatabase.getInstance().getReference("User").child(customerId)
        userRef.get().addOnSuccessListener { snapshot ->
            if (!isAdded) return@addOnSuccessListener
            val firstName = snapshot.child("firstName").getValue(String::class.java)
            val lastName = snapshot.child("lastName").getValue(String::class.java) ?: ""
            val rating = (snapshot.child("averageRating").getValue(Double::class.java) ?: 0.0)
            val profileImageUrl = snapshot.child("profileImageUrl").getValue(String::class.java)
            
            if (firstName != null) {
                customerNameDisplay.text = "$firstName $lastName"
                customerRatingDisplay.text = String.format(Locale.getDefault(), "%.1f", rating)
            } else {
                customerNameDisplay.text = "Customer"
            }

            if (!profileImageUrl.isNullOrEmpty()) {
                Glide.with(this)
                    .load(profileImageUrl)
                    .placeholder(R.drawable.sample_handyman)
                    .circleCrop()
                    .into(ivProfile)
            }
        }

        btnViewProfile.setOnClickListener {
            val intent = Intent(requireContext(), CustomerProfileActivity::class.java).apply {
                putExtra("userId", customerId)
            }
            startActivity(intent)
        }

        val btnMessage: Button = view.findViewById(R.id.btnMessage)

        val btnReturn: Button = view.findViewById(R.id.btnReturn)

        // Setup MapView
        val mapView = view.findViewById<MapView>(R.id.mapView)
        mapView.setMultiTouchControls(true)

        jobRef.get().addOnSuccessListener { snapshot ->
            if (!isAdded) return@addOnSuccessListener
            
            // Handle Map Coordinates first to use in fallback geocoding
            var lat = snapshot.child("latitude").getValue(Double::class.java)
            var lng = snapshot.child("longitude").getValue(Double::class.java)

            // If not in snapshot, fallback to arguments
            if (lat == null || lat == 0.0) lat = argLat.toDouble()
            if (lng == null || lng == 0.0) lng = argLng.toDouble()

            val citySuburb = snapshot.child("citySuburb").getValue(String::class.java)
            if (isAssigned) {
                locationDisplay.text = "$location, Melbourne, VIC"
            } else {
                if (!citySuburb.isNullOrBlank()) {
                    locationDisplay.text = "$citySuburb (Approximate)"
                } else if (lat != null && lng != null && lat != 0.0 && lng != 0.0) {
                    // Fallback Geocoding
                    try {
                        val geocoder = Geocoder(requireContext(), Locale.getDefault())
                        val addresses = geocoder.getFromLocation(lat, lng, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val address = addresses[0]
                            val city = address.locality ?: address.subLocality ?: address.subAdminArea ?: address.adminArea ?: "Approximate Location"
                            locationDisplay.text = if (city != "Approximate Location") "$city (Approximate)" else city
                        } else {
                            locationDisplay.text = "Approximate Location"
                        }
                    } catch (e: Exception) {
                        locationDisplay.text = "Approximate Location"
                    }
                } else {
                    locationDisplay.text = "Approximate Location"
                }
            }

            if (lat != null && lng != null && lat != 0.0 && lng != 0.0) {
                mapView.visibility = View.VISIBLE
                val jobLocation = GeoPoint(lat, lng)
                
                val currentHandymanId = SessionManager.getLoggedInUserId(requireContext())
                val isAssigned = snapshot.child("assignedTo").getValue(String::class.java) == currentHandymanId

                if (isAssigned) {
                    mapView.controller.setZoom(16.0)
                    mapView.controller.setCenter(jobLocation)

                    val marker = Marker(mapView)
                    marker.position = jobLocation
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    marker.title = "Job Location"
                    mapView.overlays.add(marker)
                } else {
                    mapView.controller.setZoom(10.0) // Zoom out further for 5km radius
                    mapView.controller.setCenter(jobLocation)

                    val circle = org.osmdroid.views.overlay.Polygon()
                    circle.points = org.osmdroid.views.overlay.Polygon.pointsAsCircle(jobLocation, 5000.0) // 5km radius
                    circle.fillPaint.color = Color.argb(50, 0, 0, 255)
                    circle.outlinePaint.color = Color.BLUE
                    circle.outlinePaint.strokeWidth = 2f
                    mapView.overlays.add(circle)
                }
                mapView.invalidate()
            } else {
                mapView.visibility = View.GONE
            }

            val paymentStatus = snapshot.child("paymentStatus").getValue(String::class.java) ?: ""
            val custpay = snapshot.child("custpay").getValue(String::class.java) ?: ""

            if (paymentStatus == "done" && custpay.isNotBlank()) {
                salaryDisplay.text = "Paid: BDT $custpay"
            } else if (salaryFrom.isNotBlank() && salaryTo.isNotBlank()) {
                salaryDisplay.text = if (paymentOption == "Per Day")
                    "BDT $salaryFrom-$salaryTo/day"
                else
                    "BDT $salaryFrom-$salaryTo"
            } else {
                salaryDisplay.text = "To be negotiated"
            }
        }

        btnMessage.setOnClickListener {
            val context = requireContext()
            val currentHandymanId = SessionManager.currentUserID ?: return@setOnClickListener
            val compositeChatId = "${jobId}_${currentHandymanId}"

            // Fetch document from Firestore that contains chatroom of job
            val chatRef = FirebaseFirestore.getInstance().collection("chats").document(compositeChatId)
            chatRef.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // Load memberInfos array from document
                    val memberInfos = documentSnapshot.get("memberInfos") as? List<Map<String, String>> ?: emptyList()

                    // Find the other member (the customer)
                    val otherMember = memberInfos.find { it["uid"] != currentHandymanId }
                    
                    val intent = Intent(context, ChatClientActivity::class.java).apply {
                        putExtra("chatID", compositeChatId)
                        putExtra("uid", otherMember?.get("uid") ?: customerId)
                        putExtra("username", otherMember?.get("username") ?: "Customer")
                    }
                    context.startActivity(intent)
                }
                else {
                    // Chat doesn't exist, create it
                    // First fetch customer name
                    val userRef = FirebaseDatabase.getInstance().getReference("User").child(customerId)
                    userRef.child("firstName").get().addOnSuccessListener { snapshot ->
                        val cName = snapshot.getValue(String::class.java) ?: "Customer"
                        createAndOpenChat(context, compositeChatId, jobId, currentHandymanId, customerId, cName)
                    }.addOnFailureListener {
                        createAndOpenChat(context, compositeChatId, jobId, currentHandymanId, customerId, "Customer")
                    }
                }
            }
        }

        // Set a click listener on the return button
        btnReturn.setOnClickListener {
            findNavController().navigateUp()
        }

        displayJobImages(view, jobId)
    }

    private fun createAndOpenChat(context: android.content.Context, chatId: String, jobId: String, handymanId: String, customerId: String, customerName: String) {
        val hName = SessionManager.getLoggedInUserName(context)
        val newChat = hashMapOf(
            "chatID" to chatId,
            "jobId" to jobId,
            "memberInfos" to listOf(
                mapOf("uid" to customerId, "username" to customerName),
                mapOf("uid" to handymanId, "username" to hName)
            )
        )
        FirebaseFirestore.getInstance().collection("chats").document(chatId)
            .set(newChat)
            .addOnSuccessListener {
                val intent = Intent(context, ChatClientActivity::class.java).apply {
                    putExtra("chatID", chatId)
                    putExtra("uid", customerId)
                    putExtra("username", customerName)
                }
                context.startActivity(intent)
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to create chat: ${e.message}", Toast.LENGTH_SHORT).show()
            }
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
                        if (!isAdded) return@addOnSuccessListener
                        val context = context ?: return@addOnSuccessListener

                        // Convert URI list to a list of string URLs.
                        val imageUrls = uriList.map { it.toString() }

                        // Show or hide the scroll view based on whether we have images.
                        photoScroll.visibility = if (imageUrls.isNotEmpty()) View.VISIBLE else View.GONE

                        // Clear any existing views in the container.
                        photosContainer.removeAllViews()

                        // Dynamically create ImageViews and load the images using Glide.
                        for (url in imageUrls) {
                            val imageView = ImageView(context)
                            val params = LinearLayout.LayoutParams(400, 400)
                            params.setMargins(8, 8, 8, 8)
                            imageView.layoutParams = params
                            imageView.scaleType = ImageView.ScaleType.CENTER_CROP

                            Glide.with(this)
                                .load(url)
                                .into(imageView)

                            imageView.setOnClickListener {
                                showEnlargedImage(url)
                            }

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

    private fun showEnlargedImage(imageUrl: String) {
        val context = context ?: return
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_enlarged_image, null)
        val enlargedImageView = dialogView.findViewById<ImageView>(R.id.ivEnlargedImage)
        val btnClose = dialogView.findViewById<ImageView>(R.id.ivCloseDialog)
        val progressBar = dialogView.findViewById<ProgressBar>(R.id.pbLoading)

        progressBar.visibility = View.VISIBLE

        Glide.with(this)
            .load(imageUrl)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    if (!isAdded) return false
                    progressBar.visibility = View.GONE
                    Log.e("GlideError", "Failed to load image: $imageUrl", e)
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    if (!isAdded) return false
                    progressBar.visibility = View.GONE
                    return false
                }
            })
            .into(enlargedImageView)

        val dialog = AlertDialog.Builder(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
            .setView(dialogView)
            .create()

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
