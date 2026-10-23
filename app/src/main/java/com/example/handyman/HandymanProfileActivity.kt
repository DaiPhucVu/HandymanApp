package com.example.handyman

import android.content.Context
import com.example.handyman.utils.LocaleHelper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.handyman.handyman_pages.HandymanProfileScreen
import com.example.handyman.handyman_pages.HandymanEditProfile

class HandymanProfileActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrap(newBase))
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(navController = navController, startDestination = "handymanProfile") {
                        composable("handymanProfile") {
                            HandymanProfileScreen(navController = navController)
                        }
                        composable("handymanEditProfile") {
                            HandymanEditProfile(navController = navController)
                        }
                        // Add a fallback for the verification landing if clicked from here
                        composable("handymanKYCLanding") {
                            // Since this activity is just for profile, 
                            // we can finish and let the main activity handle it
                            // or we can implement the screen here.
                            // For now, let's just pop back or finish.
                            LaunchedEffect(Unit) {
                                finish()
                            }
                        }
                    }
                }
            }
        }
    }
}
