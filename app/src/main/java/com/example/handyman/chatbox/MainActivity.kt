package com.example.handyman.chatbox



import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.systemBarsPadding
import com.example.handyman.MainJobBoard
import com.google.firebase.database.*
import com.example.handyman.Navigation
import com.example.handyman.ui.theme.HandymanTheme
import com.example.handyman.utils.SessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import java.util.Calendar
import java.util.Locale
import com.example.handyman.utils.updateSessionMetrics
import com.example.handyman.utils.getCurrentYearMonth

//import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private var sessionStartTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val sessionEmail = SessionManager.getLoggedInEmail(this)

        Log.d("Session", "Restoring session for: $sessionEmail")

        setContent {
            HandymanTheme {
                var startDestination by remember { mutableStateOf<String?>("landingPage") }

                Navigation(
                    modifier = Modifier.fillMaxSize(),
                    startDestination = startDestination ?: "landingPage"
                )
            }
        }
    }


    override fun onStart() {
        super.onStart()
        sessionStartTime = System.currentTimeMillis()
    }

    override fun onStop() {
        super.onStop()

        val user = FirebaseAuth.getInstance().currentUser ?: return
        val sessionEnd = System.currentTimeMillis()
        val durationMin = (sessionEnd - sessionStartTime) / 60000.0
        val isBounce = durationMin < 1.0

        val (year, month) = getCurrentYearMonth()
        updateSessionMetrics(durationMin, isBounce, year, month)
    }

    private fun Login(email: String, password: String) {
        // Login function using credentials stored in Realtime Database
        val database = FirebaseDatabase.getInstance()

        var isLogined = false

        val userRef = database.getReference("User")
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (userSnapshot in snapshot.children) {
                    if (email.trim() == userSnapshot.child("email").value && password.trim() == userSnapshot.child(
                            "password"
                        ).value
                    ) {
                        isLogined = true
                        SessionManager.currentUserID = userSnapshot.key
                        SessionManager.currentUserName = userSnapshot.child("firstName").value as String?
                        SessionManager.currentUserType = "customer"
                        val intent = Intent(this@MainActivity, MainJobBoard::class.java)
                        startActivity(intent)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DatabaseError", "$error")
                Toast.makeText(
                    this@MainActivity,
                    "An error occurred while login with ${email.trim()}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })

        if (isLogined) return

        val handymanRef = database.getReference("Handyman")
        handymanRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (handymanSnapshot in snapshot.children) {
                    if (email.trim() == handymanSnapshot.child("email").value && password.trim() == handymanSnapshot.child(
                            "password"
                        ).value
                    ) {
                        isLogined = true
                        SessionManager.currentUserID = handymanSnapshot.key
                        SessionManager.currentUserName =
                            handymanSnapshot.child("firstName").value as String?
                        SessionManager.currentUserType = "handyman"
                        val intent = Intent(this@MainActivity, MainJobBoard::class.java)
                        startActivity(intent)
                    }
                }
                if (!isLogined) {
                    Toast.makeText(
                        this@MainActivity,
                        "Cannot find provided login information",
                        Toast.LENGTH_LONG
                    )
                        .show()
                    Toast.makeText(
                        this@MainActivity,
                        "Please recheck your email and password",
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DatabaseError", "$error")
                Toast.makeText(
                    this@MainActivity,
                    "An error occurred while login with ${email.trim()}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })


// Login function using Firebase Authentication (completed except distinguishing between handyman and customer)
//        val auth = FirebaseAuth.getInstance()
//        auth.signInWithEmailAndPassword(email.trim(), password.trim())
//            .addOnCompleteListener(this) { task ->
//                if (task.isSuccessful) {
////                    val intent = Intent(this@MainActivity, ChatListingActivity::class.java)
//                    val intent = Intent(this@MainActivity, MainJobBoard::class.java).apply {
//                        putExtra("user_type", "handyman")
//                    }
////                    getFCMToken()
//                    startActivity(intent)
//                } else {
//                    Log.e("FirebaseAuth", "Sign-in failed", task.exception)
//                    Toast.makeText(this@MainActivity, "Failed to login as $email", Toast.LENGTH_LONG).show()
//                }
//            }

    }
}
