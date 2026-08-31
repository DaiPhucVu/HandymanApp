package com.example.handyman

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.handyman.utils.LocaleHelper

private val Purple = Color(0xFF7D56F3)
private val Amber = Color(0xFFFFB703)

/**
 * Language picker.
 *
 * Used in two places:
 *  - full screen on first launch, before anything else (no [onDismiss])
 *  - reopened later from the landing page, where it can be closed without
 *    changing anything ([onDismiss] provided)
 *
 * Built for low-literacy users (the handyman app's target audience): each
 * language is written in its own script, tap targets are large, and one tap both
 * selects and continues. The prompt appears in both languages so it is readable
 * whichever one you speak.
 */
@Composable
fun LanguageSelectionScreen(
    onLanguageSelected: (String) -> Unit,
    currentLanguage: String? = null,
    onDismiss: (() -> Unit)? = null,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Purple)
            .systemBarsPadding(),
    ) {
        // Pinned to the screen corner rather than sitting in the centred column,
        // so it stays put regardless of how tall the content is.
        // Only offered when reopened later — on first launch a choice has to be
        // made before continuing.
        if (onDismiss != null) {
            Text(
                text = "✕",
                color = Color.White,
                fontSize = 22.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clickable(onClick = onDismiss)
                    .padding(16.dp),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Shown in both languages so it is legible to either audience.
            Text(
                text = "Choose your language",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "আপনার ভাষা নির্বাচন করুন",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(24.dp))

            LanguageOption(
                label = "English",
                selected = currentLanguage == LocaleHelper.LANG_ENGLISH,
                onClick = { onLanguageSelected(LocaleHelper.LANG_ENGLISH) },
            )

            Spacer(modifier = Modifier.height(12.dp))

            LanguageOption(
                label = "বাংলা",
                selected = currentLanguage == LocaleHelper.LANG_BANGLA,
                onClick = { onLanguageSelected(LocaleHelper.LANG_BANGLA) },
            )
        }
    }
}

@Composable
private fun LanguageOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            // Fixed height, not heightIn(min=). With only a minimum, the
            // fillMaxSize() Box below expands the card to fill the whole screen
            // and pushes the second option out of view.
            .height(64.dp)
            .then(
                if (selected) Modifier.border(3.dp, Amber, RoundedCornerShape(14.dp))
                else Modifier
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                color = Purple,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
