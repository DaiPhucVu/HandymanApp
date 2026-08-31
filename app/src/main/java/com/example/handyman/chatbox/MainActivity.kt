package com.example.handyman.chatbox



import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.handyman.Navigation
import com.example.handyman.ui.theme.HandymanTheme
import com.example.handyman.LanguageSelectionScreen
import com.example.handyman.utils.LocaleHelper
import com.google.firebase.auth.FirebaseAuth
import com.example.handyman.utils.updateSessionMetrics
import com.example.handyman.utils.getCurrentYearMonth

//import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {
    private var sessionStartTime: Long = 0

    // Applies the saved language to this screen. Must be present on every
    // Activity for the choice to take effect there.
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val startDestination = intent.getStringExtra("startDestination") ?: "landingPage"

        setContent {
            HandymanTheme {
                // On the very first launch, ask for a language before anything
                // else. Once chosen it is remembered and this never shows again.
                if (!LocaleHelper.hasChosenLanguage(this)) {
                    LanguageSelectionScreen(
                        onLanguageSelected = { language ->
                            LocaleHelper.setLanguage(this, language)
                            // Recreate so attachBaseContext re-runs with the new
                            // locale and the app redraws in that language.
                            recreate()
                        }
                    )
                } else {
                    Navigation(
                        modifier = Modifier.fillMaxSize(),
                        startDestination = startDestination
                    )
                }
            }
        }
    }


    override fun onStart() {
        super.onStart()
        sessionStartTime = System.currentTimeMillis()
    }

    override fun onStop() {
        super.onStop()

        FirebaseAuth.getInstance().currentUser ?: return
        val sessionEnd = System.currentTimeMillis()
        val durationMin = (sessionEnd - sessionStartTime) / 60000.0
        val isBounce = durationMin < 1.0

        val (year, month) = getCurrentYearMonth()
        updateSessionMetrics(durationMin, isBounce, year, month)
    }

}
