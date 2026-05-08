package com.example.handyman

import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.fragment.findNavController
import androidx.core.content.FileProvider
import androidx.preference.PreferenceManager
import com.example.handyman.utils.SessionManager
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import java.io.File
import java.util.UUID

class JobPostingFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize OSMDroid configuration
        Configuration.getInstance().load(requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val args = JobPostingFragmentArgs.fromBundle(requireArguments())
        val serviceName = args.serviceCategory

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val navController = rememberNavController()
                // Scope ViewModel to Activity so data persists when fragment is recreated (e.g. changing service)
                val viewModel: JobPostingViewModel = viewModel(viewModelStoreOwner = requireActivity())
                
                // Update service category if it changed
                LaunchedEffect(serviceName) {
                    if (viewModel.isEditing) {
                        viewModel.serviceCategory = serviceName
                        navController.navigate("jobPostingReview") {
                            popUpTo("jobPostingDescription") { inclusive = true }
                        }
                    } else {
                        viewModel.serviceCategory = serviceName
                    }
                }

                // P69-17: pre-fill the job location with the user's saved home address.
                // Only runs for a fresh job (no edits yet); user changes are preserved
                // and written to the job's jobLocation, never back to the profile.
                LaunchedEffect(Unit) {
                    if (!viewModel.isEditing && viewModel.locationAddress.isBlank() &&
                        viewModel.latitude == 0.0 && viewModel.longitude == 0.0) {
                        val ctx = requireContext()
                        val userId = SessionManager.currentUserID
                            ?: SessionManager.getLoggedInUserId(ctx)
                        if (userId.isNotBlank()) {
                            try {
                                val snapshot = withContext(Dispatchers.IO) {
                                    Tasks.await(
                                        FirebaseDatabase.getInstance()
                                            .getReference("User").child(userId).get()
                                    )
                                }
                                val parts = listOf("houseNumber", "street", "area", "city", "country")
                                    .map { snapshot.child(it).getValue(String::class.java).orEmpty() }
                                    .filter { it.isNotBlank() }
                                if (parts.isNotEmpty() && viewModel.locationAddress.isBlank()) {
                                    val homeAddress = parts.joinToString(", ")
                                    viewModel.locationAddress = homeAddress
                                    val results = withContext(Dispatchers.IO) {
                                        runCatching {
                                            Geocoder(ctx).getFromLocationName(homeAddress, 1)
                                        }.getOrNull()
                                    }
                                    if (!results.isNullOrEmpty() &&
                                        viewModel.latitude == 0.0 && viewModel.longitude == 0.0) {
                                        viewModel.latitude = results[0].latitude
                                        viewModel.longitude = results[0].longitude
                                    }
                                }
                            } catch (_: Exception) {
                                // No saved home address, fetch failed, or geocoder unavailable.
                                // Fall through — user can still set the location manually.
                            }
                        }
                    }
                }

                NavHost(navController = navController, startDestination = "jobPostingDescription") {
                    composable("jobPostingDescription") {
                        JobPostingDescriptionScreen(navController, viewModel)
                    }
                    composable("jobPostingLocation") {
                        JobPostingLocationScreen(navController, viewModel)
                    }
                    composable("jobPostingSalary") {
                        JobPostingSalaryScreen(navController, viewModel)
                    }
                    composable("jobPostingPhotos") {
                        JobPostingPhotoScreen(navController, viewModel)
                    }
                    composable("jobPostingReview") {
                        JobPostingReviewScreen(navController, viewModel)
                    }
                    // Route to go back to the service list (Fragment world)
                    composable("customerHome") {
                        findNavController().popBackStack()
                    }
                    composable("allJobsList") {
                        val customerId = SessionManager.getLoggedInUserId(requireContext())
                        val action = JobPostingFragmentDirections.actionJobPostingFragmentToCustomerJobListFragment(customerId)
                        findNavController().navigate(action)
                    }
                }
            }
        }
    }

    // This remains available for use in the Compose screen via context
    fun createImageFileUri(): Uri {
        val fileName = "${UUID.randomUUID()}.jpg"
        val file = File(requireContext().cacheDir, fileName)
        return FileProvider.getUriForFile(requireContext(), "com.example.handyman.fileprovider", file)
    }
}
