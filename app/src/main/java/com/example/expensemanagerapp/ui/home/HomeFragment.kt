package com.example.expensemanager.ui.home

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.expensemanager.R
import com.example.expensemanager.databinding.FragmentHomeBinding
import com.example.expensemanager.domain.model.TransactionModel
import com.example.expensemanager.domain.model.TransactionType
import com.example.expensemanager.utils.Resource
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var transactionAdapter: TransactionAdapter

    private var selectedCalendar = Calendar.getInstance()
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateMonthYearText()
        setupRecyclerView()
        setupListeners()
        observeData()
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter()
        binding.rvTransactions.apply {
            adapter = transactionAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupListeners() {
        binding.ivSearch.setOnClickListener {
            findNavController().navigate(R.id.action_global_searchFragment)
        }

        val showDateListener = View.OnClickListener { showDatePicker() }
        binding.ivCalendar.setOnClickListener(showDateListener)
        binding.llMonthFilter.setOnClickListener(showDateListener)

        // LẮNG NGHE NHẬP LIỆU THÔNG MINH
        binding.btnSmartSend.setOnClickListener {
            val input = binding.etSmartInput.text.toString().trim()
            if (input.isNotEmpty()) {
                viewModel.processSmartEntry(input)
                binding.etSmartInput.text.clear()
            }
        }

        // LẮNG NGHE NÚT 3 GẠCH -> MỞ MENU SETTINGS
        binding.ivMenu.setOnClickListener {
            showSettingsDialog()
        }
    }

    // ================= HÀM MỚI ĐƯỢC THÊM VÀO =================
    private fun showSettingsDialog() {
        // Khởi tạo BottomSheet (Menu vuốt từ dưới lên)
        val bottomSheetDialog = com.google.android.material.bottomsheet.BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_settings, null)
        bottomSheetDialog.setContentView(view)

        // Ánh xạ các nút bấm trong Menu
        val switchTheme = view.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.switchTheme)
        val llCurrencyLang = view.findViewById<View>(R.id.llCurrencyLang)
        val llAbout = view.findViewById<View>(R.id.llAbout)

        // --- CHỨC NĂNG 1: Sáng / Tối ---
        val prefs = requireContext().getSharedPreferences("theme_prefs", android.content.Context.MODE_PRIVATE)
        switchTheme.isChecked = prefs.getBoolean("is_dark_mode", false)

        switchTheme.setOnCheckedChangeListener { _, isChecked ->
            com.example.expensemanager.utils.ThemeHelper.toggleDarkMode(requireContext(), isChecked)
            bottomSheetDialog.dismiss() // Tự động đóng menu sau khi đổi màu
        }

        // --- CHỨC NĂNG 2: Tiền tệ & Ngôn ngữ ---
        llCurrencyLang.setOnClickListener {
            bottomSheetDialog.dismiss()
            Toast.makeText(requireContext(), "Tính năng đổi VNĐ -> USD đang được cập nhật!", Toast.LENGTH_SHORT).show()
        }

        // --- CHỨC NĂNG 3: Thông tin ứng dụng (About) ---
        llAbout.setOnClickListener {
            bottomSheetDialog.dismiss()
            // Hiển thị một Dialog nhỏ chứa thông tin của bạn
            AlertDialog.Builder(requireContext())
                .setTitle("Thông tin ứng dụng")
                .setMessage("Ứng dụng: Quản lý Chi tiêu AI\n" +
                        "Phiên bản: 1.0.0 (Bản thử nghiệm)\n" +
                        "Sinh viên thực hiện: Lê Hải Đăng\n" +
                        "Bản quyền: Đồ án cơ sở 3 - Khoa KHMT (2026)")
                .setPositiveButton("Đóng", null)
                .show()
        }

        // Hiển thị Menu lên màn hình
        bottomSheetDialog.show()
    }
    // =========================================================

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Chọn tháng muốn xem")
            .setSelection(selectedCalendar.timeInMillis)
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            selectedCalendar.timeInMillis = selection
            updateMonthYearText()
            viewModel.loadTransactions()
        }
        datePicker.show(parentFragmentManager, "HOME_DATE_PICKER")
    }

    private fun updateMonthYearText() {
        val monthFormatter = SimpleDateFormat("MMM", Locale("vi", "VN"))
        val yearFormatter = SimpleDateFormat("yyyy", Locale.getDefault())
        binding.tvFilterMonth.text = monthFormatter.format(selectedCalendar.time).replace("thg ", "Thg ")
        binding.tvFilterYear.text = yearFormatter.format(selectedCalendar.time)
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // 1. Quan sát danh sách giao dịch
                launch {
                    viewModel.transactions.collect { resource ->
                        when (resource) {
                            is Resource.Loading -> { binding.emptyStateLayout.visibility = View.GONE }
                            is Resource.Success -> {
                                val allTransactions = resource.data ?: emptyList()
                                val currentMonthTransactions = allTransactions.filter { tx ->
                                    val txCal = Calendar.getInstance().apply { time = tx.date }
                                    txCal.get(Calendar.MONTH) == selectedCalendar.get(Calendar.MONTH) &&
                                            txCal.get(Calendar.YEAR) == selectedCalendar.get(Calendar.YEAR)
                                }

                                if (currentMonthTransactions.isEmpty()) {
                                    binding.emptyStateLayout.visibility = View.VISIBLE
                                    binding.rvTransactions.visibility = View.GONE
                                    binding.tvTotalIncome.text = "0 đ"
                                    binding.tvTotalExpense.text = "0 đ"
                                    binding.tvBalance.text = "0 đ"
                                } else {
                                    binding.emptyStateLayout.visibility = View.GONE
                                    binding.rvTransactions.visibility = View.VISIBLE
                                    transactionAdapter.submitList(currentMonthTransactions)

                                    val totalIncome = currentMonthTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                                    val totalExpense = currentMonthTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

                                    binding.tvTotalIncome.text = currencyFormat.format(totalIncome)
                                    binding.tvTotalExpense.text = currencyFormat.format(totalExpense)
                                    binding.tvBalance.text = currencyFormat.format(totalIncome - totalExpense)
                                }
                            }
                            is Resource.Error -> {
                                Toast.makeText(requireContext(), "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                // 2. QUAN SÁT KẾT QUẢ TỪ AI
                launch {
                    viewModel.smartEntryState.collect { resource ->
                        when (resource) {
                            is Resource.Loading -> {
                                Toast.makeText(requireContext(), "AI đang phân tích...", Toast.LENGTH_SHORT).show()
                            }
                            is Resource.Success -> {
                                resource.data?.let { data ->
                                    showConfirmDialog(data)

                                    // CHỐT CHẶN: Reset ngay sau khi hiện Dialog để lần sau quay lại không bị hiện nữa
                                    viewModel.resetSmartEntryState()
                                }
                            }
                            is Resource.Error -> {
                                Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                                // Cũng nên reset khi lỗi để sạch dữ liệu
                                viewModel.resetSmartEntryState()
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }

    private fun showConfirmDialog(data: Map<String, Any>) {
        val title = data["title"] as? String ?: "Giao dịch mới"
        val amount = (data["amount"] as? Number)?.toDouble() ?: 0.0
        val category = data["category"] as? String ?: "Khác"
        val typeStr = data["type"] as? String ?: "EXPENSE"
        val type = if (typeStr == "INCOME") TransactionType.INCOME else TransactionType.EXPENSE

        val message = "Bạn muốn thêm giao dịch này?\n\n" +
                "Nội dung: $title\n" +
                "Số tiền: ${currencyFormat.format(amount)}\n" +
                "Danh mục: $category\n" +
                "Loại: ${if (type == TransactionType.INCOME) "Thu nhập" else "Chi tiêu"}"

        AlertDialog.Builder(requireContext())
            .setTitle("Xác nhận nhập liệu AI")
            .setMessage(message)
            .setPositiveButton("Thêm ngay") { _, _ ->
                val newTransaction = TransactionModel(
                    id = 0, // SỬA TẠI ĐÂY: Đổi "" (String) thành 0 (Int) để hết lỗi mismatch
                    title = title,
                    amount = amount,
                    category = category,
                    type = type,
                    date = Date(),
                    note = "Nhập nhanh bằng AI",
                    userId = viewModel.getCurrentUserId() // Đảm bảo gán userId để lọc đa tài khoản
                )
                viewModel.addTransaction(newTransaction) // Bây giờ sẽ hết lỗi Unresolved
                Toast.makeText(requireContext(), "Đã thêm thành công!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}