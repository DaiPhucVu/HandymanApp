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
import com.example.handyman.ChooseAccountType
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

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(context, 2)

        adapter = ServiceCategoryAdapter(customerId) { category ->
            // Handle item click here
            Log.d(TAG, "Clicked on: ${category.name}")
        }
        recyclerView.adapter = adapter

        adapter.submitList(serviceCategoryViewModel.categories)

        val avatar = view.findViewById<View>(R.id.ivAvatar)
        avatar.setOnClickListener {
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
            SessionManager.clearSessionXML(requireContext())
            Log.d("Navigation:", "User clicks Logout")


            // Redirect to ChooseAccountType activity
            val intent = Intent(requireContext(), ChooseAccountTypeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

        }

        return view
    }
}
