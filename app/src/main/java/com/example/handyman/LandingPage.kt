package com.example.handyman

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.handyman.R

@Composable
fun LandingPage(modifier: Modifier = Modifier, navController: NavController) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF7D56F3))
            .systemBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text("Welcome to", fontSize = 20.sp, color = Color.White)

        // ProFix Logo
        Image(
            painter = painterResource(id = R.drawable.profix_logo_1),
            contentDescription = "ProFix Logo",
            modifier = Modifier.height(48.dp)
        )


        // Hero Image
        Image(
            painter = painterResource(id = R.drawable.hands),
            contentDescription = "Hands Holding Tools",
            modifier = Modifier.size(240.dp)
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Get things done right\nby our expert",
                fontSize = 16.sp,
                color = Color.White,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Text("Technicians", fontSize = 22.sp, color = Color.White)
        }

        Button(
            onClick = { navController.navigate("chooseAccountType") },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB703)),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Get started", fontSize = 18.sp, color = Color(0xFF283618))
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
