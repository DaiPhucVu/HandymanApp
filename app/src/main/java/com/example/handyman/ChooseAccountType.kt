package com.example.handyman

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun ChooseAccountType(modifier: Modifier = Modifier, navController: NavController) {
    val scrollState = rememberScrollState()
    Box(modifier = modifier.fillMaxSize().background(Color(0xFF7D56F3))) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "Get started",
            fontSize = 24.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp))
        // ProFix Logo
        Image(
            painter = painterResource(id = R.drawable.profix_logo_1),
            contentDescription = "ProFix Logo",
            modifier = Modifier.height(120.dp)
        )
        Text(
            text = "Select your account type",
            fontSize = 16.sp,
            color = Color.White,
            modifier = Modifier.padding(bottom = 24.dp, top = 64.dp)
        )

        // Customer button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(color = Color(0xFFFFB703), shape = RoundedCornerShape(16.dp))
                .clickable { navController.navigate("customerSignup") },
            contentAlignment = Alignment.Center
        ) {
            Column(        modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally)
            {
                Text("I'm a", fontSize = 24.sp, color = Color(0xFF30386D), fontWeight = FontWeight.Medium)
                Text(
                    "Customer",
                    fontSize = 48.sp,
                    color = Color(0xFF30386D),
                    fontWeight = FontWeight.Bold
                )
            }

        }

        Spacer(modifier = Modifier.height(24.dp))

        // Handyman button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(color = Color(0xFF648FFF), shape = RoundedCornerShape(16.dp))
                .clickable { navController.navigate("handymanSignup") },
            contentAlignment = Alignment.Center
        ) {
            Column(        modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally)
            {
                Text("I'm a", fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Medium)
                Text(
                    "Handyman",
                    fontSize = 48.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
    }

        // Back to the language picker. Overlaid rather than placed in the
        // scrolling column so it stays put when the content scrolls.
        Icon(
            painter = painterResource(id = R.drawable.arrow_back),
            contentDescription = "Back to language selection",
            tint = Color.White,
            modifier = Modifier
                .align(Alignment.TopStart)
                .systemBarsPadding()
                .padding(16.dp)
                .size(28.dp)
                .clickable { navController.navigate("languageSelection") }
        )
    }
}
