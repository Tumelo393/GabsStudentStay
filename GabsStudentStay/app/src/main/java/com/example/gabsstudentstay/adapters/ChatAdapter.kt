package com.example.gabsstudentstay.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gabsstudentstay.R
import com.example.gabsstudentstay.models.Message

class ChatAdapter(
    private val messages: List<Message>,
    private val currentUserId: String
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val layoutReceived: LinearLayout = view.findViewById(R.id.layoutReceived)
        val tvReceivedMessage: TextView = view.findViewById(R.id.tvReceivedMessage)
        val tvReceivedTime: TextView = view.findViewById(R.id.tvReceivedTime)

        val layoutSent: LinearLayout = view.findViewById(R.id.layoutSent)
        val tvSentMessage: TextView = view.findViewById(R.id.tvSentMessage)
        val tvSentTime: TextView = view.findViewById(R.id.tvSentTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]

        // Check if the message was sent by the current user
        if (message.senderId == currentUserId) {
            holder.layoutSent.visibility = View.VISIBLE
            holder.layoutReceived.visibility = View.GONE
            holder.tvSentMessage.text = message.text
            holder.tvSentTime.text = message.timestamp
        } else {
            holder.layoutSent.visibility = View.GONE
            holder.layoutReceived.visibility = View.VISIBLE
            holder.tvReceivedMessage.text = message.text
            holder.tvReceivedTime.text = message.timestamp
        }
    }

    override fun getItemCount() = messages.size
}
