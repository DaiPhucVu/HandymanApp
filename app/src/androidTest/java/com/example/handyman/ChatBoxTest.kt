package com.example.handyman

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.handyman.chatbox.sendMessage
import com.example.handyman.utils.SessionManager
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChatBoxTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.handyman", appContext.packageName)
    }

    @Test
    fun sendMessageTest() {
        val chatID = "unitTest"
        val testMessage1 = "This is test message from testUser1"
        val testMessage2 = "This is test message from testUser2"

        FirebaseApp.initializeApp(InstrumentationRegistry.getInstrumentation().targetContext)

        SessionManager.currentUserID = "1"
        sendMessage(chatID = chatID, receiverID = "2", content = testMessage1)

        SessionManager.currentUserID = "2"
        sendMessage(chatID = chatID, receiverID = "1", content = testMessage2)

        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("chats")
            .document(chatID)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshots, error ->
                if (snapshots == null || error != null) {
                    return@addSnapshotListener
                }

                snapshots.documents.mapNotNull { messageRef ->
                    val senderID = messageRef.getString("senderId")
                    val receiverID = messageRef.getString("receiverId")
                    val content = messageRef.getString("content")
                    if (receiverID == "2") {
                        assertEquals("1", senderID)
                        assertEquals(testMessage1, content)
                    }
                    else if (receiverID == "1") {
                        assertEquals("2", senderID)
                        assertEquals(testMessage2, content)
                    }
                }
            }
    }
}
