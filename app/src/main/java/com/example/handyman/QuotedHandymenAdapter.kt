// QuotedHandymenAdapter.kt

package com.example.handyman

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.compose.ui.platform.InterceptPlatformTextInput
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.handyman.chatbox.ChatClientActivity
import com.example.handyman.utils.SessionManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore

class QuotedHandymenAdapter(
    private val handymanList: List<String>,
    private val jobId: String,
    initialAssignedId: String,
    private val customerId: String,
    private val context: Context
) : RecyclerView.Adapter<QuotedHandymenAdapter.ViewHolder>() {

    private var assignedId: String = initialAssignedId

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvSubtitle: TextView = itemView.findViewById(R.id.tvSubtitle)
        val tvRating: TextView = itemView.findViewById(R.id.tvRating)
        val assignBtn: Button = itemView.findViewById(R.id.btnAssign)
        val messageBtn: Button = itemView.findViewById(R.id.btnMessage)
        val viewProfileBtn: Button = itemView.findViewById(R.id.btnViewProfile)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.quoted_handymen_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = handymanList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val handymanId = handymanList[position]
        
        // Fetch Handyman details
        val handymanRef = FirebaseDatabase.getInstance().getReference("Handyman").child(handymanId)
        handymanRef.get().addOnSuccessListener { snapshot ->
            val firstName = snapshot.child("firstName").getValue(String::class.java)
            val lastName = snapshot.child("lastName").getValue(String::class.java) ?: ""
            val trade = snapshot.child("primaryTrade").getValue(String::class.java) ?: "Handyman"
            val rating = snapshot.child("averageRating").getValue(Double::class.java) ?: 0.0
            
            if (firstName != null) {
                holder.tvTitle.text = "$firstName $lastName"
                holder.tvSubtitle.text = trade
                holder.tvRating.text = String.format("%.1f", rating)
            } else {
                holder.tvTitle.text = "Handyman"
            }
        }

        holder.viewProfileBtn.setOnClickListener {
            val intent = Intent(context, HandymanProfileActivity::class.java).apply {
                putExtra("handymanId", handymanId)
            }
            context.startActivity(intent)
        }

        val isAssigned = assignedId.isNotBlank()
        val isThisAssigned = (handymanId == assignedId)

        when {
            isThisAssigned -> {
                holder.assignBtn.text = "Assigned"
                holder.assignBtn.isEnabled = false
                ViewCompat.setBackgroundTintList(
                    holder.assignBtn,
                    ColorStateList.valueOf(Color.parseColor("#50C878"))
                )
            }

            isAssigned -> {
                holder.assignBtn.text = "Assign"
                holder.assignBtn.isEnabled = false
                ViewCompat.setBackgroundTintList(
                    holder.assignBtn,
                    ColorStateList.valueOf(Color.parseColor("#FFCCCCCC"))
                )
            }

            else -> {
                holder.assignBtn.text = "Assign"
                holder.assignBtn.isEnabled = true
                ViewCompat.setBackgroundTintList(
                    holder.assignBtn,
                    ColorStateList.valueOf(Color.parseColor("#ff33b5e5"))
                )
            }
        }

        holder.assignBtn.setOnClickListener {
            val jobRef = FirebaseDatabase.getInstance()
                .getReference("Job")
                .child(jobId)

            jobRef.child("assignedTo")
                .setValue(handymanId)
                .addOnSuccessListener {
                    assignedId = handymanId
                    notifyDataSetChanged()

                    val handymanRef = FirebaseDatabase.getInstance()
                        .getReference("Handyman")
                        .child(handymanId)
                    handymanRef.child("acceptedJobs")
                        .push()
                        .setValue(jobId)
                        .addOnFailureListener { e2 ->
                            Toast.makeText(
                                holder.itemView.context,
                                "Assigned, but failed in acceptedJobs: ${e2.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                    // remove from quotedJobs
                    handymanRef.child("quotedJobs")
                        .orderByValue().equalTo(jobId)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                snapshot.children.forEach { it.ref.removeValue() }
                            }

                            override fun onCancelled(error: DatabaseError) {}
                        })

                    // move in customer’s lists: notAssignedJobs → assignedJobs
                    val custRef = FirebaseDatabase.getInstance()
                        .getReference("User")
                        .child(customerId)

                    custRef.child("notAssignedJobs")
                        .orderByValue().equalTo(jobId)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(ns: DataSnapshot) {
                                ns.children.forEach { it.ref.removeValue() }
                                // now push into assignedJobs
                                custRef.child("assignedJobs")
                                    .push()
                                    .setValue(jobId)
                                    .addOnFailureListener { e4 ->
                                        Toast.makeText(
                                            holder.itemView.context,
                                            "Assigned, but failed to record in customer’s assignedJobs: ${e4.message}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                            }

                            override fun onCancelled(error: DatabaseError) {}
                        })

                    // feedback
                    Toast.makeText(
                        holder.itemView.context,
                        "Assigned to $handymanId",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener { e1 ->
                    Toast.makeText(
                        holder.itemView.context,
                        "Failed to assign: ${e1.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }

        // Message button to open chat in job detail of client
        holder.messageBtn.setOnClickListener {
            val compositeChatId = "${jobId}_${handymanId}"
            val db = FirebaseFirestore.getInstance()
            val chatRef = db.collection("chats").document(compositeChatId)

            chatRef.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // Chat already exists, just open it
                    val memberInfos = documentSnapshot.get("memberInfos") as? List<Map<String, String>>
                    val otherMember = memberInfos?.find { it["uid"] != SessionManager.currentUserID }
                    
                    val intent = Intent(context, ChatClientActivity::class.java).apply {
                        putExtra("chatID", compositeChatId)
                        putExtra("uid", otherMember?.get("uid"))
                        putExtra("username", otherMember?.get("username") ?: "Handyman")
                    }
                    context.startActivity(intent)
                } else {
                    // Chat doesn't exist, we need to fetch the handyman's name first to create it
                    val handymanRef = FirebaseDatabase.getInstance().getReference("Handyman").child(handymanId)
                    handymanRef.child("firstName").get().addOnSuccessListener { nameSnapshot ->
                        val hName = nameSnapshot.getValue(String::class.java) ?: "Handyman"
                        
                        // Create the chat document
                        val newChat = hashMapOf(
                            "chatID" to compositeChatId,
                            "jobId" to jobId,
                            "memberInfos" to listOf(
                                mapOf("uid" to SessionManager.currentUserID!!, "username" to (SessionManager.getLoggedInUserName(context))),
                                mapOf("uid" to handymanId, "username" to hName)
                            )
                        )
                        
                        chatRef.set(newChat).addOnSuccessListener {
                            val intent = Intent(context, ChatClientActivity::class.java).apply {
                                putExtra("chatID", compositeChatId)
                                putExtra("uid", handymanId)
                                putExtra("username", hName)
                            }
                            context.startActivity(intent)
                        }
                    }
                }
            }.addOnFailureListener { e ->
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
