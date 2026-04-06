package com.example.handyman.chatbox.ui.composables

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.handyman.R
import com.example.handyman.utils.SessionManager
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class Message(
    val senderId: String,
    val receiverId: String,
    val timestamp: Timestamp,
    val content: String
)

class ChatClientViewModel : ViewModel() {
    private val database: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val chatID = mutableStateOf("")
    private val messages = mutableStateListOf<Message>()

    init {
        if (chatID.value != "") {
            listenForMessages()
        }
    }

    private fun listenForMessages() {
        database.collection("chats")
            .document(chatID.value)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshots, error ->
                if (snapshots == null || error != null) {
                    return@addSnapshotListener
                }

                messages.clear()
                snapshots.documents.mapNotNull { docRef ->
                    val senderId = docRef.getString("senderId")
                    val receiverId = docRef.getString("receiverId")
                    val timestamp = docRef.getTimestamp("timestamp")
                    val content = docRef.getString("content")
                    if (senderId != null && receiverId != null && timestamp != null && content != null) {
                        messages.add(
                            Message(
                                senderId = senderId,
                                receiverId = receiverId,
                                timestamp = timestamp,
                                content = content
                            )
                        )
                    }
                }
            }
    }

    fun getMessages(): SnapshotStateList<Message> {
        return messages
    }

    fun updateChatID(newID: String) {
        chatID.value = newID
        listenForMessages()
    }
}

@Composable
fun ChatClientScreen(
    chatID: String?,
    receiverName: String,
    onSendButtonClick: (String) -> Unit,
    viewModel: ChatClientViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    LaunchedEffect(chatID) {
        chatID?.let { viewModel.updateChatID(it) }
    }

    val messages = viewModel.getMessages()
    var messageInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(messages.size) {
        listState.animateScrollToItem(messages.size)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = Color.White)
    ) {
        // Name display and buttons on top of screen
        Box(
            modifier = Modifier
                .requiredHeight(height = 190.dp)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(
                        x = 32.dp,
                        y = 56.dp,
                    )
            ) {
//                BackButton(size = 35.dp, onClick = {})
            }
            Text(
                text = receiverName,
                color = Color(0xff4d4d4d),
                textAlign = TextAlign.Center,
                lineHeight = 2.5.em,
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier
                    .align(alignment = Alignment.TopCenter)
                    .offset(
                        y = 65.dp
                    )
            )
//            Image(
//                painter = painterResource(id = R.drawable.ic_overflow_dot_menu),
//                contentDescription = "Overflow menu icon",
//                modifier = Modifier
//                    .align(alignment = Alignment.TopStart)
//                    .offset(
//                        x = 375.dp,
//                        y = 56.dp
//                    )
//                    .requiredSize(size = 32.dp)
//            )
        }

        // Chat display
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(7.dp),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 120.dp, bottom = 150.dp)
        ) {
            items(messages) { message ->
                ChatBubble(
                    message = message.content,
//                    isUserMessage = message.senderId == FirebaseAuth.getInstance().currentUser?.uid
                    isUserMessage = message.senderId == SessionManager.currentUserID
                )
                Spacer(modifier = Modifier.height(7.dp))
            }
        }

        // Input field and send button
        Box(
            modifier = Modifier
                .align(alignment = Alignment.BottomCenter)
                .offset(
                    y = (-57).dp
                )
                .requiredWidth(width = 360.dp)
                .requiredHeight(height = 75.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                ChatInputField(
                    value = messageInput,
                    onValueChange = { messageInput = it },
                    modifier = Modifier.weight(4f)
                )
                SendButton(
                    size = 57.dp,
                    onClick = {
                        if (messageInput.isNotBlank()) {
                            onSendButtonClick(messageInput.trim())
                            messageInput = ""
                            keyboardController?.hide()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .offset(y = (7.5).dp)
                )
            }
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ChatBubble(message: String, isUserMessage: Boolean) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val halfWidth = maxWidth / 2
        Box(
            modifier = Modifier
                .width(halfWidth)
                .background(
                    color = if (isUserMessage) Color(0xFFDCF8C6) else Color.LightGray,
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(12.dp)
                .align(if (isUserMessage) Alignment.TopEnd else Alignment.TopStart)
        ) {
            Text(text = message)
        }
    }
}

@Composable
fun ChatInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = "Write a message...",
                style = TextStyle(
                    fontSize = 16.sp
                ),
                color = Color(0xffc1c1c1),
                modifier = Modifier
                    .offset(
                        y = (-2.5).dp
                    )
            )
        },
        textStyle = TextStyle(fontSize = 16.sp),
        singleLine = false,
        colors = OutlinedTextFieldDefaults
            .colors(
                //focusedContainerColor = Color(0xffc1c1c1),
                unfocusedBorderColor = Color(0xffc1c1c1)
            ),
        modifier = modifier
    )
}

@Composable
fun SendButton(size: Dp, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val iconRatio = 0.65f
    val buttonSize = size
    val iconSize = buttonSize * iconRatio

    IconButton(
        onClick = { onClick() },
        modifier = modifier
            .requiredSize(size)
            .border(
                width = (1.2).dp,
                color = Color(0xffc1c1c1),
                shape = CircleShape
            )
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_sendarrow),
            contentDescription = "Send button",
            tint = Color(0xffc1c1c1),
            modifier = Modifier.requiredSize(iconSize)
        )
    }
}

//@Preview(widthDp = 412, heightDp = 915)
//@Composable
//private fun ChatClientPreview() {
//    ChatClientScreen("User1", {})
//}