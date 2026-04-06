package com.example.handyman.chatbox.ui.composables

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot

data class ChatChannel(
    val chatID: String?,
    val uid: String?,
    val username: String?,
    val lastMessage: String?,
//    val profilePictureURL: String
)

class ChatListingViewModel : ViewModel() {
    var channelList = mutableStateListOf<ChatChannel>()

    init {
        fetchChatChannels()
    }

    private fun fetchChatChannels() {
        // Old version that list every user
//        FirebaseFirestore.getInstance().collection("users").get().addOnCompleteListener { task ->
//            val currentUID = FirebaseAuth.getInstance().currentUser?.uid
//            if (task.isSuccessful && task.result != null) {
//                channelList.clear()
//                for (snapshot: QueryDocumentSnapshot in task.result) {
//                    snapshot.getString("username")?.let { Log.d("Snapshot", it) }
//
//                    if (currentUID.equals(snapshot.id)) {
//                        continue
//                    }
//
//                    val uid = snapshot.getString("uid")
//                    val username = snapshot.getString("username")
//
//                    if (uid != null && username != null) {
//                        channelList.add(
//                            ChatChannel(
//                                uid = uid,
//                                username = username,
//                                lastMessage = "Test message",
////                            profilePictureURL = R.mipmap.test_profile.toString()
//                            )
//                        )
//                    }
//                }
//            } else {
//                Log.e("Firestore", "Failed to fetch ChatChannels: " + task.exception)
//            }
//        }

        FirebaseFirestore.getInstance().collection("chats").get().addOnCompleteListener { task ->
            val currentUID = com.example.handyman.utils.SessionManager.currentUserID

            if (task.isSuccessful && task.result != null) {
                channelList.clear()
                for (snapshot: QueryDocumentSnapshot in task.result) {
                    val members: List<Map<String, String>> = snapshot.get("memberInfos") as List<Map<String, String>>

                    for ((index, member) in members.withIndex()) {
                        if (member["uid"] == currentUID) {
                            if (index == members.size - 1) {
                                channelList.add(
                                    ChatChannel(
                                        chatID = snapshot.get("chatID") as String?,
                                        uid = members[0]["uid"],
                                        username = members[0]["username"],
                                        lastMessage = "Test Message"
                                    )
                                )
                            }
                            else {
                                channelList.add(
                                    ChatChannel(
                                        chatID = snapshot.get("chatID") as String?,
                                        uid = members[1]["uid"],
                                        username = members[1]["username"],
                                        lastMessage = "Test Message"
                                    )
                                )
                            }
                        }
                    }
                }
            }
            else {
                Log.e("Firestore", "Failed to fetch ChatChannels: " + task.exception)
            }
        }
    }
}

@Composable
fun ChatListingItem(chatChannel: ChatChannel, onClick: (ChatChannel) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .clickable { onClick(chatChannel) }
    ) {
//        Image(
//            painter = painterResource(chatChannel.profilePictureURL.toInt()),
//            contentDescription = "Profile picture",
//            modifier = Modifier.size(50.dp).clip(CircleShape),
//            contentScale = ContentScale.Crop
//        )

        Column(modifier = Modifier.padding(start = 16.dp, top = 5.dp)) {
            chatChannel.username?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            chatChannel.lastMessage?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun ChatListingScreen(
    viewModel: ChatListingViewModel = viewModel(),
    onChannelClick: (ChatChannel) -> Unit,
    modifier: Modifier = Modifier.requiredWidth(345.dp).fillMaxHeight()
) {
    val chatChannels = viewModel.channelList

    if (chatChannels.isEmpty()) {
        CircularProgressIndicator(
            modifier = modifier.requiredWidth(100.dp).offset(y = 230.dp)
        )
    } else {
        LazyColumn(modifier = modifier) {
            items(chatChannels) { chatChannel ->
                ChatListingItem(chatChannel = chatChannel, onClick = onChannelClick)
                HorizontalDivider()
            }
        }
    }
}
