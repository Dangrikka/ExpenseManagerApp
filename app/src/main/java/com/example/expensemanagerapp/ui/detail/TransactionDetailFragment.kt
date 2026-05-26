package com.example.expensemanager.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.expensemanager.R
import com.example.expensemanager.databinding.FragmentTransactionDetailBinding
import com.example.expensemanager.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class TransactionDetailFragment : Fragment() {

    private var _binding: FragmentTransactionDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TransactionDetailViewModel by viewModels()

    // Đã đổi sang Int cho khớp với Room
    private var transactionId: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let { bundle ->
            // SỬA Ở ĐÂY: Nhận ID dạng số nguyên (Int)
            transactionId = bundle.getInt("id", 0)

            val title = bundle.getString("title")
            val amount = bundle.getDouble("amount", 0.0)
            val type = bundle.getString("type")
            val category = bundle.getString("category")
            val dateMillis = bundle.getLong("date", 0L)
            val note = bundle.getString("note")

            binding.tvTitle.text = title
            binding.tvCategory.text = category
            binding.tvNote.text = if (note.isNullOrBlank()) "Không có ghi chú" else note

            binding.tvAmount.text = com.example.expensemanager.utils.CurrencyUtils.formatCurrency(requireContext(), amount)

            if (type == "INCOME" || type == "THU") {
                binding.tvType.text = "Thu nhập"
                binding.tvType.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
            } else {
                binding.tvType.text = "Chi tiêu"
                binding.tvType.setTextColor(android.graphics.Color.parseColor("#F44336"))
            }

            val cal = Calendar.getInstance().apply { timeInMillis = dateMillis }
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            binding.tvDate.text = sdf.format(cal.time)
        }

        setupListeners()
        observeData()
    }

    private fun setupListeners() {
        // MỚI THÊM: Xử lý nút Sửa (Bạn kiểm tra xem trong file XML id của nút Sửa có phải btnEdit không nhé)
        binding.btnEdit.setOnClickListener {
            // "Chìa khóa" nằm ở đây: Truyền lại y nguyên gói arguments sang trang Add/Edit
            findNavController().navigate(
                R.id.action_transactionDetailFragment_to_addTransactionFragment,
                arguments
            )
        }

        binding.btnDelete.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Xoá giao dịch")
                .setMessage("Bạn có chắc chắn muốn xoá giao dịch này không?")
                .setPositiveButton("Xoá") { _, _ ->
                    // Ép sang chuỗi tạm thời vì ViewModel đang nhận tham số String
                    if (transactionId > 0) {
                        viewModel.deleteTransaction(transactionId.toString())
                    }
                }
                .setNegativeButton("Hủy", null)
                .show()
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.deleteState.collect { state ->
                    when (state) {
                        is Resource.Success -> {
                            Toast.makeText(requireContext(), "Đã xoá thành công", Toast.LENGTH_SHORT).show()
                            findNavController().popBackStack()
                        }
                        is Resource.Error -> {
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}