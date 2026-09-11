package com.example.handyman

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
//Customer pages
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.handyman.customer_pages.*
import com.example.handyman.handyman_pages.*
import com.example.handyman.utils.LocaleHelper


@Composable
fun Navigation(modifier: Modifier = Modifier, startDestination: String = "landingPage") {
    val navController = rememberNavController()
    val jobPostingViewModel: JobPostingViewModel = viewModel()
    val context = LocalContext.current
    var showLanguagePicker by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.fillMaxSize(),
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(350)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(350)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(350)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(350)
            )
        },
        builder = {
        composable(
            route = "landingPage",
            exitTransition = {
                if (targetState.destination.route == "chooseAccountType") {
                    ExitTransition.None
                } else {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(350)
                    )
                }
            },
            popEnterTransition = {
                if (initialState.destination.route == "languageSelection") {
                    EnterTransition.None
                } else {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(350)
                    )
                }
            }
        ) {
            LandingPage(
                modifier = Modifier.fillMaxSize(),
                navController = navController,
                onGetStarted = { showLanguagePicker = true }
            )
        }
        composable(
            route = "languageSelection",
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(350)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(350)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(350)
                )
            }
        ) {
            LanguageSelectionRoute(navController)
        }
        composable(
            route = "chooseAccountType",
            enterTransition = {
                if (
                    initialState.destination.route == "languageSelection" ||
                    initialState.destination.route == "landingPage"
                ) {
                    EnterTransition.None
                } else {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(350)
                    )
                }
            },
            popEnterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(350)
                )
            }
        ) {
            ChooseAccountType(
                modifier = Modifier.fillMaxSize(),
                navController = navController,
                onBackToLanguageSelection = { showLanguagePicker = true }
            )
        }

        // New Job Posting Flow
        composable("jobPostingDescription") {
            JobPostingDescriptionScreen(navController, jobPostingViewModel)
        }
        composable("jobPostingLocation") {
            JobPostingLocationScreen(navController, jobPostingViewModel)
        }
        composable("jobPostingSalary") {
            JobPostingSalaryScreen(navController, jobPostingViewModel)
        }
        composable("jobPostingPhotos") {
            JobPostingPhotoScreen(navController, jobPostingViewModel)
        }
        composable("jobPostingReview") {
            JobPostingReviewScreen(navController, jobPostingViewModel)
        }

//        Handyman pages

        composable(
            route = "handymanSignup",
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(350)
                )
            }
        ) {
            HandymanSignup(Modifier.fillMaxSize().systemBarsPadding(), navController)
        }
        composable("handymanSkills") {
            HandymanSkillsScreen(navController)
        }
        composable("handymanLogin") {
            HandymanLogin(Modifier.fillMaxSize().systemBarsPadding(), navController)
        }
        composable("handymanHomeUnverified") {
            HandymanHomeUnverified(Modifier.fillMaxSize().systemBarsPadding(), navController)
        }
        composable("handymanKYCLanding") {
            HandymanKYCLanding(Modifier.fillMaxSize().systemBarsPadding(), navController)
        }
        composable("handymanKYCCaptureID") {
            HandymanKYCCaptureID(Modifier.fillMaxSize().systemBarsPadding(), navController)
        }
        composable("handymanKYCAddressForm") {
            HandymanKYCAddressForm(Modifier.fillMaxSize().systemBarsPadding(), navController)
        }
        composable("handymanKycPhoneNumber") {
            HandymanKYCPhoneNumber(Modifier.fillMaxSize().systemBarsPadding(), navController)
        }
        composable("handymanKycCodeOTP/{verificationId}/{phoneNumber}") { backStackEntry ->
            val verificationId = backStackEntry.arguments?.getString("verificationId") ?: ""
            val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""
            HandymanKYCCodeOTP(Modifier.fillMaxSize().systemBarsPadding(), navController, verificationId, phoneNumber)
        }
        composable("handymanKycSubmitted") {
            HandymanKYCSubmitted(Modifier.fillMaxSize().systemBarsPadding(), navController)
        }
        composable("handymanHomeKYCProcessing") {
            HandymanKYCProcessing(Modifier.fillMaxSize().systemBarsPadding(), navController)
        }
        composable("handymanKYCCertificates") {
            HandymanKYCCertificates(Modifier.fillMaxSize().systemBarsPadding(), navController)
        }
        composable("handymanProfile") {
            HandymanProfileScreen(navController)
        }
        composable("handymanEditProfile") {
            HandymanEditProfile(navController)
        }
        composable("handymanProfilePictureUpload") {
            HandymanProfilePictureUpload(navController)
        }


//        Customer pages

        composable("customerLogin") {
            CustomerLogin(Modifier.fillMaxSize().systemBarsPadding(), navController)
        }
        composable(
            route = "customerSignup",
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(350)
                )
            }
        ) {
            CustomerSignup(Modifier.fillMaxSize().systemBarsPadding(), navController)
        }
        composable("customerHome") {
            CustomerHome(Modifier.fillMaxSize().systemBarsPadding(), navController, jobPostingViewModel)
        }
        composable("customerHomeUnverified") {
            CustomerHomeUnverified(Modifier.fillMaxSize().systemBarsPadding(), navController)
        }
        composable("customerKycLanding") {
            CustomerKYCLanding(Modifier.fillMaxSize().systemBarsPadding(), navController)
        }
        composable("customerKycCaptureID") {
            CustomerKYCCaptureID(navController, Modifier.fillMaxSize().systemBarsPadding())
        }
        composable("customerKycAddressForm") {
            CustomerKYCAddressForm(Modifier.fillMaxSize().systemBarsPadding(), navController)
        }
        composable("customerKycPhoneNumber") {
            CustomerKYCPhoneNumber(Modifier.fillMaxSize().systemBarsPadding(), navController)
        }

        //OTP 
        composable("customerKycCodeOTP/{verificationId}/{phoneNumber}") { backStackEntry ->
            val verificationId = backStackEntry.arguments?.getString("verificationId") ?: ""
            val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""
            CustomerKYCCodeOTP(Modifier.fillMaxSize().systemBarsPadding(), navController, verificationId, phoneNumber)
        }

        composable("customerKycSubmitted") {
            CustomerKYCSubmitted(Modifier.fillMaxSize().systemBarsPadding(), navController)
        }
        composable("customerKycSuccess") {
            CustomerKYCSuccess(navController)
        }
        composable ("customerHomeKYCProcessing"){
            CustomerHomeKYCProcessing(Modifier.fillMaxSize().systemBarsPadding(), navController)
        }
        composable("customerProfilePictureUpload") {
            CustomerProfilePictureUpload(navController)
        }
        composable("customerProfile") {
            CustomerProfileScreen(navController)
        }
        composable("customerEditProfile") {
            CustomerEditProfile(navController)
        }
        composable("handymanApprovedSplash") {
            HandymanApprovedSplashScreen(navController)
        }

    })
        AnimatedVisibility(
            visible = showLanguagePicker,
            enter = slideInVertically(
                animationSpec = tween(350),
                initialOffsetY = { it }
            ),
            exit = slideOutVertically(
                animationSpec = tween(350),
                targetOffsetY = { it }
            )
        ) {
            LanguageSelectionScreen(
                currentLanguage = LocaleHelper.currentLanguage(context),
                onDismiss = { showLanguagePicker = false },
                onLanguageSelected = { language ->
                    LocaleHelper.setLanguage(context, language)
                    // Recreate so the new locale is actually applied — resources are
                    // resolved from the Activity's base Configuration (set in
                    // attachBaseContext), which doesn't change on its own just because
                    // the preference was saved. Without this, the chosen language
                    // silently doesn't take effect until the app is next restarted.
                    LocaleHelper.findActivity(context)?.recreate()

                    if (navController.currentBackStackEntry?.destination?.route != "chooseAccountType") {
                        navController.navigate("chooseAccountType") {
                            popUpTo("landingPage") { inclusive = false }
                            launchSingleTop = true
                        }
                    }

                    showLanguagePicker = false
                }
            )
        }
    }
    
}
