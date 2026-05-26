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
import androidx.recyclerview.widget.RecyclerView
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
        var dX = 0f
        var dY = 0f
        var initialX = 0f
        var initialY = 0f
        val CLICK_DRAG_TOLERANCE = 15f // Sai số pixel để phân biệt giữa "Bấm" và "Kéo"

        binding.fabAiChat.setOnTouchListener { view, event ->
            when (event.actionMasked) {
                android.view.MotionEvent.ACTION_DOWN -> {

                    dX = view.x - event.rawX
                    dY = view.y - event.rawY
                    initialX = event.rawX
                    initialY = event.rawY
                    true
                }
                android.view.MotionEvent.ACTION_MOVE -> {
                    // Cập nhật tọa độ của nút theo ngón tay
                    view.animate()
                        .x(event.rawX + dX)
                        .y(event.rawY + dY)
                        .setDuration(0)
                        .start()
                    true
                }
                android.view.MotionEvent.ACTION_UP -> {
                    // Khi nhấc ngón tay lên, kiểm tra xem người dùng vừa "Bấm" hay "Kéo"
                    val moveX = Math.abs(event.rawX - initialX)
                    val moveY = Math.abs(event.rawY - initialY)

                    if (moveX < CLICK_DRAG_TOLERANCE && moveY < CLICK_DRAG_TOLERANCE) {
                        // Nếu ngón tay di chuyển rất ít -> Đây là thao tác CLICK
                        showAiChatDialog()
                    }
                    // Nếu muốn nút tự dính vào lề màn hình (Snap to edge) thì viết thêm logic ở đây
                    true
                }
                else -> false
            }
        }
    }

    // ================= HÀM MỚI ĐƯỢC THÊM VÀO =================
    private fun showSettingsDialog() {
        // Khởi tạo BottomSheet (Menu vuốt từ dưới lên)
        val bottomSheetDialog = com.google.android.material.bottomsheet.BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_settings, null)
        bottomSheetDialog.setContentView(view)

        // Ánh xạ các nút bấm trong Menu
        val switchTheme = view.findViewById<com.google.android.material.materialswitch.MaterialSwitch>(R.id.switchTheme)
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
            com.example.expensemanager.utils.CurrencyUtils.toggleCurrency(requireContext())
            val isUsd = com.example.expensemanager.utils.CurrencyUtils.isUSD(requireContext())
            Toast.makeText(requireContext(), if (isUsd) "Đã chuyển sang USD" else "Đã chuyển sang VNĐ", Toast.LENGTH_SHORT).show()
            
            // Tải lại adapter để format lại tiền tệ
            binding.rvTransactions.adapter = transactionAdapter
            viewModel.loadTransactions()
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

                                    binding.tvTotalIncome.text = com.example.expensemanager.utils.CurrencyUtils.formatCurrency(requireContext(), totalIncome)
                                    binding.tvTotalExpense.text = com.example.expensemanager.utils.CurrencyUtils.formatCurrency(requireContext(), totalExpense)
                                    binding.tvBalance.text = com.example.expensemanager.utils.CurrencyUtils.formatCurrency(requireContext(), totalIncome - totalExpense)
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
                "Số tiền: ${com.example.expensemanager.utils.CurrencyUtils.formatCurrency(requireContext(), amount)}\n" +
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
                viewModel.addTransaction(newTransaction)
                Toast.makeText(requireContext(), "Đã thêm thành công!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun showAiChatDialog() {
        val bottomSheetDialog = com.google.android.material.bottomsheet.BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_ai_chat, null)
        bottomSheetDialog.setContentView(view)

        val rvChat = view.findViewById<RecyclerView>(R.id.rvChat)
        val etChatInput = view.findViewById<android.widget.EditText>(R.id.etChatInput)
        val btnSendChat = view.findViewById<android.widget.ImageButton>(R.id.btnSendChat)

        val chatAdapter = ChatAdapter()
        rvChat.adapter = chatAdapter
        rvChat.layoutManager = LinearLayoutManager(requireContext())

        // Cập nhật danh sách tin nhắn mượt mà
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.chatMessages.collect { messages ->
                chatAdapter.submitList(messages)
                if (messages.isNotEmpty()) {
                    rvChat.scrollToPosition(messages.size - 1)
                }
            }
        }

        btnSendChat.setOnClickListener {
            val msg = etChatInput.text.toString().trim()
            if (msg.isNotEmpty()) {
                // LẤY SỐ DƯ THẬT ĐƯA CHO AI
                val currentBalance = binding.tvBalance.text.toString()
                viewModel.sendChatMessage(msg, currentBalance)
                etChatInput.text.clear()
            }
        }

        // Ép BottomSheet mở rộng hết cỡ
        bottomSheetDialog.behavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
        bottomSheetDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}