package com.example.expensemanager.ui.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.expensemanager.R
import com.example.expensemanager.databinding.FragmentAddTransactionBinding
import com.example.expensemanager.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddTransactionFragment : Fragment() {

    private var _binding: FragmentAddTransactionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddTransactionViewModel by viewModels()

    // Các biến dùng để lưu trữ dữ liệu cũ (nếu ở chế độ SỬA)
    private var currentTransactionId: Int = 0
    private var oldDate: Long = 0L
    private var oldCategory: String = "Khác"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // CHÚ Ý: Gọi checkEditMode() đầu tiên để lấy được ID giao dịch cũ
        checkEditMode()

        // Sau đó mới gọi các hàm thiết lập giao diện
        setupHeader()
        setupCategoryDropdown()
        setupListeners()
        observeState()
    }

    // KIỂM TRA XEM ĐÂY LÀ THÊM MỚI HAY SỬA
    private fun checkEditMode() {
        arguments?.let { bundle ->
            // MẸO: Lấy id an toàn dù màn hình trước truyền sang là Int hay String
            val idObj = bundle.get("id")
            currentTransactionId = if (idObj is Int) idObj else idObj?.toString()?.toIntOrNull() ?: 0

            // Nếu ID > 0 nghĩa là đang ở chế độ SỬA
            if (currentTransactionId > 0) {
                val title = bundle.getString("title") ?: ""
                val amount = bundle.getDouble("amount", 0.0)
                val type = bundle.getString("type") ?: ""
                val note = bundle.getString("note") ?: ""

                oldDate = bundle.getLong("date", System.currentTimeMillis())
                oldCategory = bundle.getString("category") ?: "Khác"

                binding.etTitle.setText(title)
                binding.etAmount.setText(amount.toLong().toString())
                binding.etNote.setText(note)
                binding.etCategory.setText(oldCategory, false)

                if (type == "INCOME" || type == "THU") {
                    binding.toggleGroupType.check(R.id.btnIncome)
                } else {
                    binding.toggleGroupType.check(R.id.btnExpense)
                }

                binding.btnSave.text = "Cập nhật Giao Dịch"
            }
        }
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val amountStr = binding.etAmount.text.toString().trim()
            val note = binding.etNote.text.toString().trim()

            // LẤY GIÁ TRỊ TỪ Ô DANH MỤC MỚI
            val category = binding.etCategory.text.toString()

            val type = if (binding.toggleGroupType.checkedButtonId == R.id.btnIncome) {
                "INCOME"
            } else {
                "EXPENSE"
            }

            if (currentTransactionId > 0) { // Sửa từ != null thành > 0
                viewModel.updateTransaction(
                    id = currentTransactionId, // Truyền thẳng Int vào
                    title = title,
                    amountStr = amountStr,
                    typeStr = type,
                    note = note,
                    oldDate = oldDate,
                    oldCategory = category
                )
            } else {
                viewModel.saveTransaction(title, amountStr, type, note, category)
            }
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.addState.collect { state ->
                    when (state) {
                        is Resource.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.btnSave.isEnabled = false
                        }
                        is Resource.Success -> {
                            binding.progressBar.visibility = View.GONE

                            // Hiện thông báo tùy theo Thêm hay Sửa
                            val msg = if (currentTransactionId != null) "Đã cập nhật giao dịch!" else "Đã thêm giao dịch!"
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

                            // Lưu xong thì quay lại trang trước
                            findNavController().popBackStack()
                        }
                        is Resource.Error -> {
                            binding.progressBar.visibility = View.GONE
                            binding.btnSave.isEnabled = true
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                        }
                        null -> {}
                    }
                }
            }
        }
    }

    private fun setupHeader() {
        // Nút quay lại
        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // Đổi tiêu đề nếu là chế độ Sửa
        if (currentTransactionId != null) {
            binding.tvHeaderTitle.text = "Sửa giao dịch"
        }
    }

    private fun setupCategoryDropdown() {
        // Danh sách các loại chi tiêu/thu nhập
        val categories = arrayOf("Ăn uống", "Mua sắm", "Đi lại", "Lương", "Giải trí", "Sức khỏe", "Giáo dục", "Khác")

        // Tạo Adapter để hiển thị danh sách
        val adapter = android.widget.ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            categories
        )

        // Gắn adapter vào ô AutoCompleteTextView
        binding.etCategory.setAdapter(adapter)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}