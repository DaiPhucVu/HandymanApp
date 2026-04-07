package com.example.handyman

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.handyman.customer_pages.CustomerProfileScreen
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.handyman.customer_pages.CustomerEditProfile
import com.example.handyman.ui.theme.HandymanTheme

class CustomerProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                HandymanTheme {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "customerProfile") {
                        composable("customerProfile") {
                            CustomerProfileScreen(navController = navController)
                        }
                        composable("customerEditProfile") {
                            CustomerEditProfile(navController = navController)
                        }
                    }
                }
            }
        }
    }
}
