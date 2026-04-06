package com.example.handyman

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
//Customer pages
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.handyman.*
import com.example.handyman.customer_pages.*
import com.example.handyman.handyman_pages.*


@Composable
fun Navigation(modifier: Modifier = Modifier, startDestination: String = "landingPage") {
    val navController = rememberNavController()
    val jobPostingViewModel: JobPostingViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        builder = {
        composable("landingPage") {
            LandingPage(Modifier.fillMaxSize(), navController)
        }
        composable("chooseAccountType") {
            ChooseAccountType(Modifier.fillMaxSize(), navController)
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

        composable("handymanSignup") {
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
        composable("handymanKycCodeOTP") {
            HandymanKYCCodeOTP(Modifier.fillMaxSize().systemBarsPadding(), navController)
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
        composable("handymanProfilePictureUpload") {
            HandymanProfilePictureUpload(navController)
        }


//        Customer pages

        composable("customerLogin") {
            CustomerLogin(Modifier.fillMaxSize().systemBarsPadding(), navController)
        }
        composable("customerSignup") {
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
        composable ("customerKycCodeOTP" ){
            CustomerKYCCodeOTP(Modifier.fillMaxSize().systemBarsPadding(), navController)
        }
        composable("customerKycSubmitted") {
            CustomerKYCSubmitted(Modifier.fillMaxSize().systemBarsPadding(), navController)
        }
        composable("customerKycSuccess") {
            CustomerKYCSuccess(Modifier.fillMaxSize().systemBarsPadding(), navController)
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

    })
    
}
