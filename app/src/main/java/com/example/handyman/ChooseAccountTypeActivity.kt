package com.example.handyman

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.handyman.customer_pages.CustomerSignup
import com.example.handyman.customer_pages.CustomerLogin
import com.example.handyman.handyman_pages.HandymanSignup
import com.example.handyman.handyman_pages.HandymanLogin



class ChooseAccountTypeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            com.example.handyman.ui.theme.HandymanTheme {
                Navigation(
                    modifier = Modifier.fillMaxSize(),
                    startDestination = "chooseAccountType"
                )
            }
        }
    }
}

