package com.example.gabsstudentstay.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gabsstudentstay.R
import com.example.gabsstudentstay.adapters.ChatAdapter
import com.example.gabsstudentstay.firebase.FirestoreHelper
import com.example.gabsstudentstay.models.Message
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

/**
 * Activity that facilitates real-time messaging between a student and a landlord.
 * Uses Firestore snapshots to update the message list instantly without refreshing.
 */
class ChatActivity : AppCompatActivity() {

    private lateinit var tvBackChat: TextView
    private lateinit var tvChatRecipientName: TextView
    private lateinit var rvMessages: RecyclerView
    private lateinit var etMessageInput: EditText
    private lateinit var btnSendMessage: Button

    private val messageList = mutableListOf<Message>()
    private lateinit var chatAdapter: ChatAdapter
    
    private val auth = FirebaseAuth.getInstance()
    private val firestoreHelper = FirestoreHelper()
    private var chatId: String = ""
    private var recipientId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // Initialize UI components
        tvBackChat          = findViewById(R.id.tvBackChat)
        tvChatRecipientName = findViewById(R.id.tvChatRecipientName)
        rvMessages          = findViewById(R.id.rvMessages)
        etMessageInput      = findViewById(R.id.etMessageInput)
        btnSendMessage      = findViewById(R.id.btnSendMessage)

        // Extract intent data passed from ListingDetails or ChatList
        val recipientName = intent.getStringExtra("CHAT_LANDLORD_NAME") ?: "Chat"
        recipientId = intent.getStringExtra("RECIPIENT_ID") ?: ""
        tvChatRecipientName.text = recipientName

        val myId = auth.currentUser?.uid ?: ""
        
        // --- CHAT ID GENERATION ---
        // Generates a consistent ID by sorting UIDs, ensuring both users open the same document
        if (recipientId.isNotEmpty()) {
            val ids = listOf(myId, recipientId).sorted()
            chatId = "${ids[0]}_${ids[1]}"
        } else {
            chatId = "general"
        }

        tvBackChat.setOnClickListener { finish() }

        setupChatRecyclerView(myId)
        
        // Start listening to the cloud for new messages
        startListeningForMessages()

        btnSendMessage.setOnClickListener {
            sendMessage()
        }
    }

    /**
     * Configures the RecyclerView with the ChatAdapter and snaps scrolling to the bottom.
     */
    private fun setupChatRecyclerView(currentUserId: String) {
        chatAdapter = ChatAdapter(messageList, currentUserId)
        rvMessages.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true // Align messages to the bottom
        }
        rvMessages.adapter = chatAdapter
    }

    /**
     * Sets up a Firestore snapshot listener to detect new messages in real-time.
     */
    private fun startListeningForMessages() {
        if (chatId.isEmpty()) return
        
        firestoreHelper.listenForMessages(chatId) { messages ->
            messageList.clear()
            messageList.addAll(messages)
            chatAdapter.notifyDataSetChanged()
            if (messageList.isNotEmpty()) {
                rvMessages.scrollToPosition(messageList.size - 1)
            }
        }
    }

    /**
     * Creates a Message object and pushes it to Firestore.
     * Captures current user ID and recipient ID for participant discovery.
     */
    private fun sendMessage() {
        val messageText = etMessageInput.text.toString().trim()
        val myId = auth.currentUser?.uid ?: return

        if (messageText.isEmpty() || recipientId.isEmpty()) {
            if (recipientId.isEmpty()) Toast.makeText(this, "No recipient selected", Toast.LENGTH_SHORT).show()
            return
        }

        // Format the current time for the chat bubble
        val timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        
        val newMessage = Message(
            senderId = myId,
            receiverId = recipientId,
            text = messageText,
            timestamp = timestamp
        )

        val participants = listOf(myId, recipientId)

        // Save to cloud
        firestoreHelper.sendMessage(chatId, newMessage, participants) { success ->
            if (success) {
                etMessageInput.text.clear() // Clear input on success
            } else {
                Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
