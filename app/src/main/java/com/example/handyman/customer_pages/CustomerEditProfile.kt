package com.example.handyman.customer_pages

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.handyman.utils.SessionManager
import com.google.firebase.database.FirebaseDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerEditProfile(navController: NavController) {
    val context = LocalContext.current
    val userId = SessionManager.getLoggedInUserId(context) ?: ""
    val database = FirebaseDatabase.getInstance().getReference("User").child(userId)

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        database.get().addOnSuccessListener { snapshot ->
            firstName = snapshot.child("firstName").getValue(String::class.java) ?: ""
            lastName = snapshot.child("lastName").getValue(String::class.java) ?: ""
            bio = snapshot.child("bio").getValue(String::class.java) ?: ""
            city = snapshot.child("city").getValue(String::class.java) ?: ""
            phone = snapshot.child("phoneNumber").getValue(String::class.java) ?: ""
            isLoading = false
        }.addOnFailureListener {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { 
                        if (!navController.popBackStack()) {
                            (context as? android.app.Activity)?.finish()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color.White)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("City") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    maxLines = 5
                )
                
                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        val updates = mapOf(
                            "firstName" to firstName,
                            "lastName" to lastName,
                            "city" to city,
                            "phoneNumber" to phone,
                            "bio" to bio
                        )
                        database.updateChildren(updates).addOnSuccessListener {
                            Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }.addOnFailureListener {
                            Toast.makeText(context, "Update failed", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB703)),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text("Save Changes", color = Color.DarkGray, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
