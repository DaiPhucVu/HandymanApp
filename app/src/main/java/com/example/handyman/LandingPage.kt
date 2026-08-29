package com.example.handyman

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.handyman.R
import com.example.handyman.utils.LocaleHelper

@Composable
fun LandingPage(modifier: Modifier = Modifier, navController: NavController) {
    val context = LocalContext.current
    var showLanguagePicker by remember { mutableStateOf(false) }

    // Reopening the picker from here is what lets the user change language on
    // any launch after the first, not just on install.
    if (showLanguagePicker) {
        LanguageSelectionScreen(
            currentLanguage = LocaleHelper.currentLanguage(context),
            onDismiss = { showLanguagePicker = false },
            onLanguageSelected = { language ->
                LocaleHelper.setLanguage(context, language)
                // Recreate so attachBaseContext re-runs and the app redraws in
                // the newly chosen language.
                LocaleHelper.findActivity(context)?.recreate()
            },
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF7D56F3))
            .systemBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        // Current language, tap to change. Kept at the top of the first screen
        // so it is reachable before login and on every app open.
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = if (LocaleHelper.currentLanguage(context) == LocaleHelper.LANG_BANGLA) {
                    "বাংলা ▾"
                } else {
                    "English ▾"
                },
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .border(1.dp, Color.White, RoundedCornerShape(20.dp))
                    .clickable { showLanguagePicker = true }
                    .padding(horizontal = 14.dp, vertical = 7.dp),
            )
        }

        Text("Welcome to", fontSize = 20.sp, color = Color.White)

        // ProFix Logo
        Image(
            painter = painterResource(id = R.drawable.profix_logo_1),
            contentDescription = "ProFix Logo",
            modifier = Modifier.height(120.dp)
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
