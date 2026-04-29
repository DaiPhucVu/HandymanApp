package com.example.handyman

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.navigation.Navigation
import com.example.handyman.utils.SessionManager
import com.example.handyman.MainJobBoard
import android.content.Intent




private const val TAG = "ServiceCategoryFragment"

class ServiceCategoryFragment : Fragment() {
    private val serviceCategoryViewModel: ServiceCategoryViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ServiceCategoryAdapter
    private lateinit var customerId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "Total service categories: ${serviceCategoryViewModel.categories.size}")
        Log.d("Navigation:", "ServiceCategoryFragment launched")

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_service_category, container, false)
        
        customerId = SessionManager.getLoggedInUserId(requireContext())
        val userName = SessionManager.getLoggedInUserName(requireContext())

        // Update greeting
        view.findViewById<android.widget.TextView>(R.id.tvGreeting)?.text = "Hello, $userName"

        val tvLocation = view.findViewById<android.widget.TextView>(R.id.tvLocation)
        val currentCity = SessionManager.getLoggedInCity(requireContext())
        if (currentCity.isNotEmpty()) {
            tvLocation.text = currentCity
        }

        // Listen for city updates from Firebase
        val customerRef = com.google.firebase.database.FirebaseDatabase.getInstance().getReference("User").child(customerId)
        customerRef.child("city").addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val city = snapshot.getValue(String::class.java)
                if (!city.isNullOrEmpty()) {
                    tvLocation.text = city
                    SessionManager.saveLoggedInCity(requireContext(), city)
                }
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        })

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(context, 2)

        adapter = ServiceCategoryAdapter(customerId) { category ->
            // Handle item click here
            Log.d(TAG, "Clicked on: ${category.name}")
        }
        recyclerView.adapter = adapter

        adapter.submitList(serviceCategoryViewModel.categories)

        val avatar = view.findViewById<View>(R.id.ivAvatar)
        val tvGreeting = view.findViewById<View>(R.id.tvGreeting)
        val profileClickListener = View.OnClickListener {
            val action = ServiceCategoryFragmentDirections
                .actionServiceCategoryFragmentToCustomerProfileFragment()
            Navigation.findNavController(view).navigate(action)
        }
        avatar.setOnClickListener(profileClickListener)
        tvGreeting.setOnClickListener(profileClickListener)

        val btnAllJobs = view.findViewById<View>(R.id.btnAllJobs)
        btnAllJobs.setOnClickListener {
            val action = ServiceCategoryFragmentDirections
                .actionServiceCategoryFragmentToCustomerJobListFragment(customerId)
            Navigation.findNavController(view).navigate(action)
        }
        val fabSupport = view.findViewById<View>(R.id.fabSupport)
        fabSupport.setOnClickListener {
            val intent = Intent(requireContext(), SupportForm::class.java)
            startActivity(intent)
        }


        val logoutIcon = view.findViewById<View>(R.id.ivLogout)
        logoutIcon.setOnClickListener {

            // Clear session
            SessionManager.clearSession(requireContext())
            Log.d("Navigation:", "User clicks Logout")


            // Redirect to MainActivity with startDestination=chooseAccountType
            val intent = Intent(requireContext(), com.example.handyman.chatbox.MainActivity::class.java)
            intent.putExtra("startDestination", "chooseAccountType")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

        }

        return view
    }
}
