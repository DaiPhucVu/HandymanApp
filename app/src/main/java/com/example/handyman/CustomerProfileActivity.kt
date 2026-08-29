package com.example.handyman

import android.content.Context
import com.example.handyman.utils.LocaleHelper

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class CustomerProfileActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrap(newBase))
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_profile)

        val userId = intent.getStringExtra("userId")
        
        val fragment = CustomerProfileFragment()
        // Pass arguments if needed, though CustomerProfileScreen already checks intent
        
        supportFragmentManager.beginTransaction()
            .replace(R.id.profile_container, fragment)
            .commit()
    }
}
