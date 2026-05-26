package com.example.expensemanager.ui.report

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.expensemanager.databinding.ItemCategoryReportBinding
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

data class CategoryReport(
    val name: String,
    val amount: Double,
    val percent: Float
)

class CategoryReportAdapter : ListAdapter<CategoryReport, CategoryReportAdapter.ViewHolder>(DiffCallback) {

    private val categoryColorMap = mapOf(
        "Ăn uống" to "#F44336",   // Đỏ
        "Đi lại" to "#2196F3",    // Xanh dương
        "Mua sắm" to "#E91E63",   // Hồng
        "Lương" to "#4CAF50",     // Xanh lá
        "Giải trí" to "#9C27B0",  // Tím
        "Hóa đơn" to "#FF9800",   // Cam
        "Sức khỏe" to "#00BCD4"   // Xanh mòng két
    )

    // 2. Bảng màu dự phòng cho các danh mục do người dùng tự tạo
    private val fallbackPalette = listOf(
        "#3F51B5", "#009688", "#8BC34A", "#CDDC39",
        "#FFC107", "#FF5722", "#795548", "#607D8B"
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemCategoryReportBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class ViewHolder(private val binding: ItemCategoryReportBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CategoryReport) {
            // Đổ dữ liệu chữ
            binding.tvCategoryName.text = item.name
            binding.tvCategoryAmount.text = com.example.expensemanager.utils.CurrencyUtils.formatCurrency(binding.root.context, item.amount)
            binding.tvCategoryPercent.text = "${String.format("%.1f", item.percent)}%"

            val hexColor = categoryColorMap[item.name] ?: fallbackPalette[abs(item.name.hashCode()) % fallbackPalette.size]

            // Đổi màu cho vòng tròn
            binding.viewCategoryColor.backgroundTintList = ColorStateList.valueOf(Color.parseColor(hexColor))
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<CategoryReport>() {
        override fun areItemsTheSame(oldItem: CategoryReport, newItem: CategoryReport) = oldItem.name == newItem.name
        override fun areContentsTheSame(oldItem: CategoryReport, newItem: CategoryReport) = oldItem == newItem
    }
}