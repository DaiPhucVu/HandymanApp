package com.example.handyman.customer_pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.handyman.R
import com.example.handyman.components.StepCircle
import com.example.handyman.components.DividerLine
import com.example.handyman.utils.SessionManager

@Composable
fun CustomerKYCLanding(modifier: Modifier = Modifier, navController: NavController) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Top Row with Back Button and Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                painter = painterResource(id = R.drawable.arrow_back),
                contentDescription = "Back",
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 16.dp)
                    .clickable {
                        navController.navigate("customerHomeUnverified")
                    }
            )
            Text("Account verification", fontSize = 20.sp, fontWeight = FontWeight.Medium)

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Logout",
                color = Color.Red,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    SessionManager.clearSession(context)
                    navController.navigate("chooseAccountType") {
                        popUpTo(0)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Progress Indicator
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            StepCircle(stepNumber = 1, isActive = true)
            DividerLine()
            StepCircle(stepNumber = 2, isActive = false)
            DividerLine()
            StepCircle(stepNumber = 3, isActive = false)
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Header Texts
        Text("Let’s verify your identity", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "You are required to verify your identity before you can use the application. Your information will be encrypted and stored securely",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(48.dp))

        // ID Card Illustration
        Image(
            painter = painterResource(id = R.drawable.id_card_icon),
            contentDescription = "ID Card Icon",
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Upload Button
        Button(
            onClick = {
                navController.navigate("customerKycCaptureID")

            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB703))
        ) {
            Text("Upload ID", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
        }
    }
}
