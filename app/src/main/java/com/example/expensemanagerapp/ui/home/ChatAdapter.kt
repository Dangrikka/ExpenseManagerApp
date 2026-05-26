package com.example.expensemanager.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.expensemanager.R
import com.google.android.material.card.MaterialCardView

// Dữ liệu 1 tin nhắn
data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val isLoading: Boolean = false // Hiệu ứng AI đang gõ chữ
)

class ChatAdapter : ListAdapter<ChatMessage, ChatAdapter.ChatViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardAi = itemView.findViewById<View>(R.id.layoutAiMessage)
        private val tvAiText = itemView.findViewById<TextView>(R.id.tvAiMessage)
        private val cardUser = itemView.findViewById<View>(R.id.layoutUserMessage)
        private val tvUserText = itemView.findViewById<TextView>(R.id.tvUserMessage)

        fun bind(message: ChatMessage) {
            if (message.isUser) {
                cardUser.visibility = View.VISIBLE
                cardAi.visibility = View.GONE
                tvUserText.text = message.text
            } else {
                cardUser.visibility = View.GONE
                cardAi.visibility = View.VISIBLE
                tvAiText.text = message.text

                if (message.isLoading) {
                    tvAiText.setTypeface(null, android.graphics.Typeface.ITALIC) // Chữ nghiêng khi chờ AI
                } else {
                    tvAiText.setTypeface(null, android.graphics.Typeface.NORMAL)
                }
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<ChatMessage>() {
            override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage) = oldItem == newItem
            override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage) = oldItem.text == newItem.text
        }
    }
}