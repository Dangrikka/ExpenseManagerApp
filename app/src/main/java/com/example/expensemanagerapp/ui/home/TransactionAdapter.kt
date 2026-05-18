package com.example.expensemanager.ui.home

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.expensemanager.R
import com.example.expensemanager.databinding.ItemTransactionBinding
import com.example.expensemanager.domain.model.TransactionModel
import com.example.expensemanager.domain.model.TransactionType
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionAdapter : ListAdapter<TransactionModel, TransactionAdapter.TransactionViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TransactionViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

        fun bind(transaction: TransactionModel) {
            binding.apply {
                tvTitle.text = transaction.title

                // Nếu transaction.date của bạn là kiểu Long thì dùng: dateFormat.format(java.util.Date(transaction.date))
                tvDate.text = dateFormat.format(transaction.date)

                // SỬA LỖI Ở ĐÂY: Dùng ivCategoryIcon (ImageView) thay vì tvCategoryIcon
                if (transaction.type == TransactionType.INCOME) {
                    tvAmount.text = "+ ${currencyFormat.format(transaction.amount)}"
                    tvAmount.setTextColor(Color.parseColor("#4CAF50")) // Màu xanh lá

                    // Gán icon mặc định của hệ thống cho giao dịch THU
                    ivCategoryIcon.setImageResource(android.R.drawable.ic_input_add)
                } else {
                    tvAmount.text = "- ${currencyFormat.format(transaction.amount)}"
                    tvAmount.setTextColor(Color.parseColor("#F44336")) // Màu đỏ

                    // Gán icon mặc định của hệ thống cho giao dịch CHI
                    ivCategoryIcon.setImageResource(android.R.drawable.ic_menu_send)
                }

                // ==========================================
                // THÊM SỰ KIỆN CLICK MỞ CHI TIẾT GIAO DỊCH
                // ==========================================
                root.setOnClickListener {
                    val bundle = Bundle().apply {
                        putInt("id", transaction.id)
                        putString("title", transaction.title)
                        putDouble("amount", transaction.amount)
                        putString("type", transaction.type.name)
                        putString("category", transaction.category)

                        // Cẩn thận: Nếu transaction.date là kiểu Long, hãy bỏ chữ .time đi
                        putLong("date", transaction.date.time)

                        putString("note", transaction.note ?: "")
                    }

                    Navigation.findNavController(root).navigate(
                        R.id.action_homeFragment_to_transactionDetailFragment,
                        bundle
                    )
                }
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<TransactionModel>() {
            override fun areItemsTheSame(oldItem: TransactionModel, newItem: TransactionModel): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: TransactionModel, newItem: TransactionModel): Boolean {
                return oldItem == newItem
            }
        }
    }
}