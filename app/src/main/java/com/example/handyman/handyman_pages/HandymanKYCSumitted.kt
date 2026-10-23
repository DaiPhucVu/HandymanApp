package com.example.handyman.handyman_pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.handyman.R

@Composable
fun HandymanKYCSubmitted(modifier: Modifier = Modifier,navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF6495ED)) // Blue background
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Top Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "<",
                fontSize = 24.sp,
                color = Color.White,
                modifier = Modifier
                    .clickable { navController.popBackStack() }
                    .padding(end = 8.dp)
            )
            Text(
                text = stringResource(R.string.account_verification_title),
                fontSize = 18.sp,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Document Icon
        Image(
            painter = painterResource(id = R.drawable.document_icon),
            contentDescription = stringResource(R.string.cd_document_icon_kyc),
            modifier = Modifier.size(120.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Heading
        Text(
            text = stringResource(R.string.kyc_application_submitted_title),
            fontSize = 24.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Subtext
        Text(
            text = stringResource(R.string.kyc_submitted_hint_handyman),
            fontSize = 14.sp,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Return Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color(0xFF30386D), RoundedCornerShape(50))
                .clickable {
                    navController.navigate("handymanHomeKYCProcessing") {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(stringResource(R.string.return_home), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
