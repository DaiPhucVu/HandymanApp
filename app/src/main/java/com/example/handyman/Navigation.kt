package com.example.handyman

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
//Customer pages
import com.example.handyman.customer_pages.CustomerHome
import com.example.handyman.customer_pages.CustomerLogin
import com.example.handyman.customer_pages.CustomerSignup
import com.example.handyman.customer_pages.CustomerHomeUnverified
import com.example.handyman.customer_pages.CustomerKYCCodeOTP
import com.example.handyman.customer_pages.CustomerKYCSubmitted
import com.example.handyman.customer_pages.CustomerKYCSuccess
import com.example.handyman.customer_pages.CustomerHomeKYCProcessing
import com.example.handyman.customer_pages.CustomerKYCLanding
import com.example.handyman.customer_pages.CustomerKYCCaptureID
import com.example.handyman.customer_pages.CustomerKYCAddressForm
import com.example.handyman.customer_pages.CustomerKYCPhoneNumber

//Handyman pages
import com.example.handyman.handyman_pages.HandymanSignup
import com.example.handyman.handyman_pages.HandymanLogin
import com.example.handyman.handyman_pages.HandymanHomeUnverified
import com.example.handyman.handyman_pages.HandymanKYCLanding
import com.example.handyman.handyman_pages.HandymanKYCCaptureID
import com.example.handyman.handyman_pages.HandymanKYCAddressForm
import com.example.handyman.handyman_pages.HandymanKYCPhoneNumber
import com.example.handyman.handyman_pages.HandymanKYCCodeOTP
import com.example.handyman.handyman_pages.HandymanKYCSubmitted
import com.example.handyman.handyman_pages.HandymanKYCProcessing
import com.example.handyman.handyman_pages.HandymanKYCCertificates


@Composable
fun Navigation(modifier: Modifier = Modifier, startDestination: String = "landingPage") {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination, builder = {
        composable("landingPage") {
            LandingPage(Modifier.fillMaxSize(), navController)
        }
        composable("chooseAccountType") {
            ChooseAccountType(Modifier.fillMaxSize(), navController)
        }

//        Handyman pages

        composable("handymanSignup") {
            HandymanSignup(Modifier.fillMaxSize().systemBarsPadding(), navController)
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


//        Customer pages

        composable("customerLogin") {
            CustomerLogin(Modifier.fillMaxSize().systemBarsPadding(), navController)
        }
        composable("customerSignup") {
            CustomerSignup(Modifier.fillMaxSize().systemBarsPadding(), navController)
        }
        composable("customerHome") {
            CustomerHome(Modifier.fillMaxSize().systemBarsPadding(), navController)
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

    })
    
}
