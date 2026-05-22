package com.example.gabsstudentstay.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gabsstudentstay.R
import com.example.gabsstudentstay.activities.ChatActivity
import com.example.gabsstudentstay.firebase.FirestoreHelper
import com.google.firebase.auth.FirebaseAuth

class MessagesFragment : Fragment() {

    private lateinit var rvChatList: RecyclerView
    private val firestoreHelper = FirestoreHelper()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_messages, container, false)
        rvChatList = view.findViewById(R.id.rvChatList)
        rvChatList.layoutManager = LinearLayoutManager(context)

        loadMyChats()
        return view
    }

    private fun loadMyChats() {
        val myId = auth.currentUser?.uid ?: return
        firestoreHelper.getMyChats(myId) { chats ->
            val adapter = ChatListAdapter(chats, myId) { chatId, recipientId, recipientName ->
                val intent = Intent(context, ChatActivity::class.java).apply {
                    putExtra("CHAT_ID", chatId)
                    putExtra("RECIPIENT_ID", recipientId)
                    putExtra("CHAT_LANDLORD_NAME", recipientName)
                }
                startActivity(intent)
            }
            rvChatList.adapter = adapter
        }
    }

    // Inner class for Chat List Adapter
    class ChatListAdapter(
        private val chats: List<Map<String, Any>>,
        private val myId: String,
        private val onClick: (String, String, String) -> Unit
    ) : RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>() {

        class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(android.R.id.text1)
            val tvLastMsg: TextView = view.findViewById(android.R.id.text2)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false)
            return ChatViewHolder(view)
        }

        override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
            val chat = chats[position]
            val participants = chat["participants"] as? List<String>
            val recipientId = participants?.find { it != myId } ?: ""
            val chatId = chat["id"] as String
            
            holder.tvName.text = "Chat with Landlord" // In a real app, fetch the name from UID
            holder.tvLastMsg.text = chat["lastMessage"] as? String ?: "No messages"
            
            holder.itemView.setOnClickListener { onClick(chatId, recipientId, "Landlord") }
        }

        override fun getItemCount() = chats.size
    }
}
