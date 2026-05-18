package com.example.expensemanager.ui.report

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.expensemanager.databinding.FragmentReportBinding
import com.example.expensemanager.ui.home.HomeViewModel
import com.example.expensemanager.utils.Resource
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class ReportFragment : Fragment() {

    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!

    // Dùng HomeViewModel cho việc hiển thị danh sách và biểu đồ
    private val viewModel: HomeViewModel by viewModels()

    // Dùng ReportViewModel để gọi AI Groq
    private val reportViewModel: ReportViewModel by viewModels()

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    private lateinit var categoryAdapter: CategoryReportAdapter
    private var selectedCalendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTabLayout()
        setupRecyclerView()
        setupMonthFilter()
        setupAI() // Kích hoạt Cố vấn AI
        observeData()
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryReportAdapter()
        binding.rvCategoryStats.apply {
            adapter = categoryAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupMonthFilter() {
        updateMonthText()

        binding.btnPrevMonth.setOnClickListener {
            selectedCalendar.add(Calendar.MONTH, -1)
            updateMonthText()
            viewModel.loadTransactions()
        }

        binding.btnNextMonth.setOnClickListener {
            selectedCalendar.add(Calendar.MONTH, 1)
            updateMonthText()
            viewModel.loadTransactions()
        }
    }

    private fun updateMonthText() {
        val format = SimpleDateFormat("MM/yyyy", Locale.getDefault())
        binding.tvCurrentMonth.text = "Tháng ${format.format(selectedCalendar.time)}"
    }

    private fun setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        binding.layoutAccount.visibility = View.VISIBLE
                        binding.layoutAnalysis.visibility = View.GONE
                    }
                    1 -> {
                        binding.layoutAccount.visibility = View.GONE
                        binding.layoutAnalysis.visibility = View.VISIBLE
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    // HÀM MỚI: Thiết lập nút bấm và đếm ngược cho AI
    private fun setupAI() {
        binding.btnAnalyzeAI.setOnClickListener {
            // Lấy tháng và năm đang được chọn trên màn hình
            val month = selectedCalendar.get(Calendar.MONTH) + 1 // Tháng trong Calendar bắt đầu từ 0
            val year = selectedCalendar.get(Calendar.YEAR)

            // Gọi AI phân tích
            reportViewModel.getAdviceFromAI(month, year)

            // Bắt đầu chu trình đếm ngược 30 giây chống nghẽn mạng
            viewLifecycleOwner.lifecycleScope.launch {
                binding.btnAnalyzeAI.isEnabled = false // Khóa nút

                for (i in 30 downTo 1) {
                    binding.btnAnalyzeAI.text = "Thử lại sau ${i}s"
                    delay(1000)
                }

                // Mở khóa nút sau 30s
                binding.btnAnalyzeAI.isEnabled = true
                binding.btnAnalyzeAI.text = "Phân tích lại"
            }
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                // 1. Quan sát dữ liệu giao dịch (logic cũ của bạn)
                launch {
                    viewModel.transactions.collect { resource ->
                        when (resource) {
                            is Resource.Success -> {
                                val allTransactions = resource.data ?: emptyList()

                                val filteredList = allTransactions.filter { tx ->
                                    val txCal = Calendar.getInstance()
                                    txCal.time = tx.date
                                    txCal.get(Calendar.MONTH) == selectedCalendar.get(Calendar.MONTH) &&
                                            txCal.get(Calendar.YEAR) == selectedCalendar.get(Calendar.YEAR)
                                }

                                val totalIncome = filteredList.filter { it.type.name == "INCOME" }.sumOf { it.amount }
                                val totalExpense = filteredList.filter { it.type.name == "EXPENSE" }.sumOf { it.amount }

                                binding.tvNetWorth.text = currencyFormat.format(totalIncome - totalExpense)
                                binding.tvTotalIncome.text = "+${currencyFormat.format(totalIncome)}"
                                binding.tvTotalExpense.text = "-${currencyFormat.format(totalExpense)}"

                                val expensesOnly = filteredList.filter { it.type.name == "EXPENSE" }
                                val categoryReportList = expensesOnly.groupBy { it.category }
                                    .map { (catName, list) ->
                                        val sum = list.sumOf { it.amount }
                                        CategoryReport(
                                            name = catName,
                                            amount = sum,
                                            percent = if (totalExpense > 0) (sum.toFloat() / totalExpense.toFloat() * 100) else 0f
                                        )
                                    }.sortedByDescending { it.amount }

                                categoryAdapter.submitList(categoryReportList)
                            }
                            is Resource.Loading -> { }
                            is Resource.Error -> { }
                            else -> {}
                        }
                    }
                }

                // 2. Quan sát trạng thái Loading của AI
                launch {
                    reportViewModel.isAiLoading.collect { isLoading ->
                        if (isLoading) {
                            binding.pbAiLoading.visibility = View.VISIBLE
                            binding.tvAiAdvice.visibility = View.GONE
                        } else {
                            binding.pbAiLoading.visibility = View.GONE
                        }
                    }
                }

                // 3. Quan sát và hiển thị câu trả lời từ AI
                launch {
                    reportViewModel.aiAdvice.collect { advice ->
                        if (advice.isNotEmpty()) {
                            binding.tvAiAdvice.visibility = View.VISIBLE
                            binding.tvAiAdvice.text = advice
                        }
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