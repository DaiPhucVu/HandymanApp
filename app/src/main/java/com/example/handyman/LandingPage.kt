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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController


@Composable
fun LandingPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    onGetStarted: (() -> Unit)? = null
) {
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
        Text(stringResource(R.string.welcome_to_label), fontSize = 20.sp, color = Color.White)

        // ProFix Logo
        Image(
            painter = painterResource(id = R.drawable.profix_logo_1),
            contentDescription = stringResource(R.string.cd_profix_logo),
            modifier = Modifier.height(120.dp)
        )


        // Hero Image
        Image(
            painter = painterResource(id = R.drawable.hands),
            contentDescription = stringResource(R.string.cd_hands_holding_tools),
            modifier = Modifier.size(240.dp)
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                stringResource(R.string.get_things_done_hint),
                fontSize = 16.sp,
                color = Color.White,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Text(stringResource(R.string.technicians_label), fontSize = 22.sp, color = Color.White)
        }

        Button(
            // Choosing a language is the first step of getting started, rather
            // than a separate control on this screen.
            onClick = { onGetStarted?.invoke() ?: navController.navigate("languageSelection") },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB703)),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(stringResource(R.string.get_started_title), fontSize = 18.sp, color = Color(0xFF283618))
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
