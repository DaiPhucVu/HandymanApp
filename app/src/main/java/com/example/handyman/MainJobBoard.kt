package com.example.handyman

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.example.handyman.utils.SessionManager

class MainJobBoard : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.job_board_main)
        Log.d("Navigation", "MainBoard Launch")

        // Set corresponding nav graph based on user type
//        val userType = SessionManager.currentUserType
        val userType = intent.getStringExtra("user_type")
        Log.d("Navigation", "$userType")

        if (userType != null) {
            val currentEmail = SessionManager.getLoggedInEmail(this)
            val table = if (userType == "customer") "User" else "Handyman"
            val userRef = com.google.firebase.database.FirebaseDatabase.getInstance().getReference(table)
            userRef.orderByChild("email").equalTo(currentEmail)
                .addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
                    override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                        for (child in snapshot.children) {
                            val city = child.child("city").getValue(String::class.java) ?: ""
                            if (city.isNotEmpty()) {
                                SessionManager.saveLoggedInCity(this@MainJobBoard, city)
                            }
                        }
                    }
                    override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
                })
        }

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        val navGraph = navHostFragment.navController.navInflater.inflate(R.navigation.nav_graph)

        // Navigations
        val startDestination = when(userType) {
            "customer" -> {
                val targetFragment = intent.getStringExtra("target_fragment")
                if (targetFragment == "customerJobList") {
                    R.id.customerJobListFragment
                } else {
                    R.id.serviceCategoryFragment
                }
            }
            "handyman" -> R.id.handymanJobBoardFragment

            else -> {
                Log.e("Navigation", "Unknown or missing user_type. Falling back.")
                R.id.handymanJobBoardFragment
            }
        }

        navGraph.setStartDestination(startDestination)
        navHostFragment.navController.graph = navGraph
    }
}
