package com.example.handyman.chatbox

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.handyman.chatbox.ui.composables.ChatChannel
import com.example.handyman.chatbox.ui.composables.ChatListingScreen
import com.example.handyman.ui.theme.HandymanTheme

class ChatListingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HandymanTheme {

                val handleChannelSelection: (ChatChannel) -> Unit = { openChatClient(it) }

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth()
                        .background(color = Color.White)
                ) {
                    Box(
                        modifier = Modifier.requiredHeight(190.dp).fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .align(alignment = Alignment.TopStart)
                                .offset(
                                    x = 32.dp,
                                    y = 56.dp,
                                )
                        ) {
//                            BackButton(size = 35.dp, onClick = {})
                        }
                    }

                    ChatListingScreen(
                        onChannelClick = handleChannelSelection,
                        modifier = Modifier
                            .requiredWidth(345.dp)
                            .fillMaxHeight()
                            .offset(
                                y = 175.dp
                            )
                            .align(Alignment.TopCenter)
                    )
                }
            }
        }
    }

    private fun openChatClient(channel: ChatChannel) {
        val intent = Intent(this@ChatListingActivity, ChatClientActivity::class.java)
        intent.putExtra("chatID", channel.chatID)
        intent.putExtra("uid", channel.uid)
        intent.putExtra("username", channel.username)
        startActivity(intent)
    }
}