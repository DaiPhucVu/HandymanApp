package com.example.handyman

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class CustomerJobDetailsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_customer_job_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ImageView>(R.id.ivBack).setOnClickListener {
            findNavController().navigateUp()
        }

        val args = CustomerJobDetailsFragmentArgs.fromBundle(requireArguments())

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
        jobTitle.text = if (serviceName.isNotBlank()) serviceName else "Untitled Job"
        val salaryDisplay = view.findViewById<TextView>(R.id.tvPrice)
        val jobRef = FirebaseDatabase.getInstance().getReference("Job").child(jobId)

        jobRef.get().addOnSuccessListener { snapshot ->
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

            // Update Map from coordinates in Firebase
            val lat = snapshot.child("latitude").getValue(Double::class.java)
            val lng = snapshot.child("longitude").getValue(Double::class.java)
            val mapView = view.findViewById<MapView>(R.id.mapView)
            mapView.setMultiTouchControls(true)

            if (lat != null && lng != null && lat != 0.0 && lng != 0.0) {
                mapView.visibility = View.VISIBLE
                val jobLocation = GeoPoint(lat, lng)
                mapView.controller.setZoom(16.0)
                mapView.controller.setCenter(jobLocation)

                val marker = Marker(mapView)
                marker.position = jobLocation
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                marker.title = "Job Location"
                mapView.overlays.add(marker)
                mapView.invalidate()
            } else {
                mapView.visibility = View.GONE
            }
        }

        val jobDescDisplay = view.findViewById<TextView>(R.id.tvJobSubtitle)
        jobDescDisplay.text = if (jobDescription.isNotBlank()) jobDescription else ""
        val dateDisplay = view.findViewById<TextView>(R.id.tvDate)
        if (dateFrom == dateTo) {
            dateDisplay.text = "$dateFrom"
        } else {
            dateDisplay.text = "$dateFrom — $dateTo"
        }
        val timeDisplay = view.findViewById<TextView>(R.id.tvTime)
        timeDisplay.text = "$timeFrom — $timeTo"
        val locationDisplay = view.findViewById<TextView>(R.id.tvAddress)
        locationDisplay.text = location

        // Edit location button
        val ivEditLocation = view.findViewById<ImageView>(R.id.ivEditLocation)
        ivEditLocation.setOnClickListener {
            val viewModel: JobPostingViewModel = ViewModelProvider(requireActivity())[JobPostingViewModel::class.java]
            viewModel.isEditing = true
            viewModel.jobId = jobId
            viewModel.customerId = customerId
            viewModel.serviceCategory = serviceName
            viewModel.problemDesc = jobDescription
            viewModel.dateFrom = dateFrom
            viewModel.dateTo = dateTo
            viewModel.timeFrom = timeFrom
            viewModel.timeTo = timeTo
            viewModel.locationAddress = location
            
            val action = CustomerJobDetailsFragmentDirections.actionCustomerJobDetailsFragmentToJobPostingFragment(serviceCategory = serviceName)
            findNavController().navigate(action)
        }

        val layoutHandyman = view.findViewById<LinearLayout>(R.id.layoutHandyman)
        val tvHandymanName = view.findViewById<TextView>(R.id.tvHandymanName)
        val btnViewProfile = view.findViewById<Button>(R.id.btnViewProfile)

        if (assignedTo.isNotBlank()) {
            layoutHandyman.visibility = View.VISIBLE
            FirebaseDatabase.getInstance().getReference("Handyman").child(assignedTo)
                .get().addOnSuccessListener { snapshot ->
                    val fName = snapshot.child("firstName").getValue(String::class.java) ?: ""
                    val lName = snapshot.child("lastName").getValue(String::class.java) ?: ""
                    tvHandymanName.text = "Handyman: $fName $lName"
                }
            
            btnViewProfile.setOnClickListener {
                val intent = Intent(requireContext(), HandymanProfileActivity::class.java).apply {
                    putExtra("handymanId", assignedTo)
                }
                startActivity(intent)
            }
        } else {
            layoutHandyman.visibility = View.GONE
        }

        displayJobImages(view, jobId)
    }

    private fun displayJobImages(rootView: View, jobId: String) {
        val photoScroll = rootView.findViewById<HorizontalScrollView>(R.id.photoScroll)
        val photosContainer = rootView.findViewById<LinearLayout>(R.id.attachPhotosContainer)
        val storageRef = FirebaseStorage.getInstance().getReference("jobImages").child(jobId)

        storageRef.listAll()
            .addOnSuccessListener { listResult ->
                val downloadUrlTasks = mutableListOf<com.google.android.gms.tasks.Task<Uri>>()
                listResult.items.forEach { itemRef ->
                    downloadUrlTasks.add(itemRef.downloadUrl)
                }

                Tasks.whenAllSuccess<Uri>(downloadUrlTasks)
                    .addOnSuccessListener { uriList ->
                        if (!isAdded) return@addOnSuccessListener
                        val context = context ?: return@addOnSuccessListener
                        val imageUrls = uriList.map { it.toString() }
                        photoScroll.visibility = if (imageUrls.isNotEmpty()) View.VISIBLE else View.GONE
                        photosContainer.removeAllViews()

                        for (url in imageUrls) {
                            val imageView = ImageView(context)
                            val params = LinearLayout.LayoutParams(400, 400)
                            params.setMargins(8, 8, 8, 8)
                            imageView.layoutParams = params
                            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                            Glide.with(this).load(url).into(imageView)
                            imageView.setOnClickListener { showEnlargedImage(url) }
                            photosContainer.addView(imageView)
                        }
                    }
                    .addOnFailureListener { photoScroll.visibility = View.GONE }
            }
            .addOnFailureListener { photoScroll.visibility = View.GONE }
    }

    private fun showEnlargedImage(imageUrl: String) {
        val context = context ?: return
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_enlarged_image, null)
        val enlargedImageView = dialogView.findViewById<ImageView>(R.id.ivEnlargedImage)
        val btnClose = dialogView.findViewById<ImageView>(R.id.ivCloseDialog)
        val progressBar = dialogView.findViewById<ProgressBar>(R.id.pbLoading)

        progressBar.visibility = View.VISIBLE

        Glide.with(this)
            .asDrawable()
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
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
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

        btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}
