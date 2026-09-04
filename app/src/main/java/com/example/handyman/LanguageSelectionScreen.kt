package com.example.handyman

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import android.content.Intent
import com.example.handyman.chatbox.MainActivity
import com.example.handyman.utils.LocaleHelper

private val PurpleDeep = Color(0xFF4A2FB8)
private val Purple = Color(0xFF7D56F3)
private val PurpleLight = Color(0xFF9B7BF7)
private val Amber = Color(0xFFFFB703)

/**
 * The "languageSelection" destination.
 *
 * The picker is a real navigation route rather than a flag inside the landing
 * page, so it can be returned to from anywhere — the back arrow on the account
 * type screen, or any future entry point — instead of only being reachable once
 * on the way through onboarding.
 */
@Composable
fun LanguageSelectionRoute(navController: NavController) {
    val context = LocalContext.current

    LanguageSelectionScreen(
        currentLanguage = LocaleHelper.currentLanguage(context),
        onDismiss = { navController.popBackStack() },
        onLanguageSelected = { language ->
            val changed = language != LocaleHelper.currentLanguage(context)
            LocaleHelper.setLanguage(context, language)

            if (changed) {
                // The locale is applied in attachBaseContext, which only runs
                // when the Activity is created — so relaunch it, carrying the
                // next destination so the user still lands where they were going.
                LocaleHelper.findActivity(context)?.let { activity ->
                    activity.startActivity(
                        Intent(activity, MainActivity::class.java).apply {
                            putExtra("startDestination", "chooseAccountType")
                        }
                    )
                    activity.finish()
                }
            } else {
                // Same language as before — no need to restart and flash the
                // screen, just carry on.
                navController.navigate("chooseAccountType")
            }
        },
    )
}

/**
 * Language picker, reached by tapping "Get started" on the landing page.
 *
 * Built for low-literacy users (the handyman app's target audience): each
 * language is written in its own script and labelled with its own alphabet's
 * first letter, tap targets are large, and one tap both selects and continues.
 * The prompt appears in both languages so it is readable whichever one you speak.
 */
@Composable
fun LanguageSelectionScreen(
    onLanguageSelected: (String) -> Unit,
    currentLanguage: String? = null,
    onDismiss: (() -> Unit)? = null,
) {
    // Animate in on first composition so the screen arrives rather than snaps.
    val entered = remember { MutableTransitionState(false).apply { targetState = true } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(PurpleDeep, Purple, PurpleLight))
            )
    ) {
        // Soft decorative circles — barely visible, they stop the large flat
        // gradient from reading as an empty screen.
        Box(
            modifier = Modifier
                .size(280.dp)
                .offset(x = (-90).dp, y = (-70).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.06f))
        )
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 70.dp, y = 60.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.05f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            // Close pinned to the corner. Only offered when the picker can be
            // backed out of — during onboarding a choice has to be made.
            if (onDismiss != null) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "✕",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 22.sp,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .clickable(onClick = onDismiss)
                            .padding(16.dp),
                    )
                }
            }

            AnimatedVisibility(
                visibleState = entered,
                enter = fadeIn(tween(450)) + slideInVertically(tween(450)) { it / 6 },
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    // Globe badge
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = "🌐", fontSize = 30.sp)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Shown in both languages so it is legible to either audience.
                    Text(
                        text = "Choose your language",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "আপনার ভাষা নির্বাচন করুন",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(36.dp))

                    LanguageOption(
                        badge = "A",
                        name = "English",
                        nativeName = "English",
                        selected = currentLanguage == LocaleHelper.LANG_ENGLISH,
                        onClick = { onLanguageSelected(LocaleHelper.LANG_ENGLISH) },
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    LanguageOption(
                        badge = "অ",
                        name = "বাংলা",
                        nativeName = "Bangla",
                        selected = currentLanguage == LocaleHelper.LANG_BANGLA,
                        onClick = { onLanguageSelected(LocaleHelper.LANG_BANGLA) },
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Text(
                        text = "You can change this later",
                        color = Color.White.copy(alpha = 0.55f),
                        fontSize = 12.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun LanguageOption(
    badge: String,
    name: String,
    nativeName: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            // Fixed height, not heightIn(min=). With only a minimum, a
            // fillMaxSize() child expands the card to fill the whole screen and
            // pushes the second option out of view.
            .height(76.dp)
            .then(
                if (selected) Modifier.border(2.5.dp, Amber, RoundedCornerShape(18.dp))
                else Modifier
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // The first letter of that language's own alphabet — a visual
            // anchor for anyone who cannot read the label itself.
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (selected) Amber else Purple.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = badge,
                    color = if (selected) Color.White else Purple,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    color = Color(0xFF1F2340),
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                )
                // Only useful when it differs from the label above it.
                if (nativeName != name) {
                    Text(
                        text = nativeName,
                        color = Color.Gray,
                        fontSize = 13.sp,
                    )
                }
            }

            if (selected) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(Amber),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "✓",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}
