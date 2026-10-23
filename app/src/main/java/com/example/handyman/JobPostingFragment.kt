package com.example.handyman

import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
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

private val jobPostingStepRoutes = listOf(
    "jobPostingDescription",
    "jobPostingLocation",
    "jobPostingSalary",
    "jobPostingPhotos",
    "jobPostingReview"
)

private fun stepForRoute(route: String?): Int {
    val index = jobPostingStepRoutes.indexOf(route)
    return if (index >= 0) index + 1 else 1
}

class JobPostingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val args = JobPostingFragmentArgs.fromBundle(requireArguments())
        val serviceName = args.serviceCategory

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val navController = rememberNavController()
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentStep = stepForRoute(backStackEntry?.destination?.route)
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

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .statusBarsPadding()
                        .padding(horizontal = 24.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    JobPostingTopBar(
                        navController = navController,
                        title = stringResource(R.string.describe_your_problem),
                        navigationIcon = if (currentStep == 1) R.drawable.ic_close else R.drawable.arrow_back,
                        navigationContentDescription = if (currentStep == 1) stringResource(R.string.cd_close) else stringResource(R.string.cd_back),
                        onBack = {
                            if (currentStep == 1 || !navController.popBackStack()) {
                                viewModel.isEditing = false
                                viewModel.clearData()
                                findNavController().popBackStack()
                            }
                        }
                    )
                    JobPostingProgressBar(currentStep = currentStep, onStepSelected = { step ->
                        val route = jobPostingStepRoutes.getOrNull(step - 1) ?: return@JobPostingProgressBar
                        if (route != backStackEntry?.destination?.route) {
                            if (step < currentStep) {
                                if (!navController.popBackStack(route, false)) {
                                    navController.navigate(route) {
                                        launchSingleTop = true
                                    }
                                }
                            }
                        }
                    })

                    NavHost(
                        navController = navController,
                        startDestination = "jobPostingDescription",
                        modifier = Modifier.weight(1f),
                        enterTransition = {
                            val initialStep = stepForRoute(initialState.destination.route)
                            val targetStep = stepForRoute(targetState.destination.route)
                            slideIntoContainer(
                                if (targetStep >= initialStep) {
                                    AnimatedContentTransitionScope.SlideDirection.Left
                                } else {
                                    AnimatedContentTransitionScope.SlideDirection.Right
                                },
                                animationSpec = tween(300)
                            )
                        },
                        exitTransition = {
                            val initialStep = stepForRoute(initialState.destination.route)
                            val targetStep = stepForRoute(targetState.destination.route)
                            slideOutOfContainer(
                                if (targetStep >= initialStep) {
                                    AnimatedContentTransitionScope.SlideDirection.Left
                                } else {
                                    AnimatedContentTransitionScope.SlideDirection.Right
                                },
                                animationSpec = tween(300)
                            )
                        },
                        popEnterTransition = {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(300)
                            )
                        },
                        popExitTransition = {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(300)
                            )
                        }
                    ) {
                        composable("jobPostingDescription") {
                            JobPostingDescriptionScreen(
                                navController = navController,
                                viewModel = viewModel
                            )
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
    }

    // This remains available for use in the Compose screen via context
    fun createImageFileUri(): Uri {
        val fileName = "${UUID.randomUUID()}.jpg"
        val file = File(requireContext().cacheDir, fileName)
        return FileProvider.getUriForFile(requireContext(), requireContext().packageName + ".fileprovider", file)
    }
}
