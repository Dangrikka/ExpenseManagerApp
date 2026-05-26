package com.example.expensemanager.ui.stats

import android.annotation.SuppressLint
import android.graphics.Color
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
import com.example.expensemanager.databinding.FragmentStatsBinding
import com.example.expensemanager.utils.Resource
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@AndroidEntryPoint
class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StatsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeStats()

        binding.btnPreviousMonth.setOnClickListener {
            viewModel.changeMonth(isNext = false)
        }

        binding.btnNextMonth.setOnClickListener {
            viewModel.changeMonth(isNext = true)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.currentMonth.collect { month ->
                        val year = viewModel.currentYear.value
                        binding.tvCurrentMonth.text = "Tháng $month, $year"
                    }
                }

                launch {
                    viewModel.currentYear.collect { year ->
                        val month = viewModel.currentMonth.value
                        binding.tvCurrentMonth.text = "Tháng $month, $year"
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun observeStats() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.stats.collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {}
                        is Resource.Success -> {
                            resource.data?.let { stats ->
                                binding.tvBalance.text = com.example.expensemanager.utils.CurrencyUtils.formatCurrency(requireContext(), stats.balance)
                                binding.tvTotalIncome.text = "+ ${com.example.expensemanager.utils.CurrencyUtils.formatCurrency(requireContext(), stats.totalIncome)}"
                                binding.tvTotalExpense.text = "- ${com.example.expensemanager.utils.CurrencyUtils.formatCurrency(requireContext(), stats.totalExpense)}"
                                setupPieChart(stats.expenseByCategory)
                            }
                        }
                        is Resource.Error -> {
                            Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun setupPieChart(expenseByCategory: Map<String, Float>) {
        val entries = ArrayList<PieEntry>()
        for ((category, amount) in expenseByCategory) {
            if (amount > 0f) {
                entries.add(PieEntry(amount, category))
            }
        }

        if (entries.isEmpty()) {
            binding.pieChart.clear()
            binding.pieChart.setNoDataText("Chưa có dữ liệu chi tiêu tháng này")
            binding.pieChart.setNoDataTextColor(Color.GRAY)
            return
        }

        val dataSet = PieDataSet(entries, "")
        val colors = arrayListOf(
            Color.parseColor("#42A5F5"), Color.parseColor("#66BB6A"),
            Color.parseColor("#FFA726"), Color.parseColor("#EF5350"),
            Color.parseColor("#AB47BC"), Color.parseColor("#26C6DA"), Color.parseColor("#FFCA28")
        )

        dataSet.apply {
            this.colors = colors
            valueTextSize = 12f
            valueTextColor = Color.WHITE
            sliceSpace = 3f
        }

        val pieData = PieData(dataSet)
        pieData.setValueFormatter(PercentFormatter(binding.pieChart))

        binding.pieChart.apply {
            data = pieData
            description.isEnabled = false
            setUsePercentValues(true)
            isDrawHoleEnabled = true
            setHoleColor(Color.TRANSPARENT)
            holeRadius = 58f
            transparentCircleRadius = 61f
            setDrawCenterText(true)
            centerText = "Cơ cấu\nChi tiêu"
            setCenterTextSize(14f)
            setCenterTextColor(Color.parseColor("#424242"))
            animateY(1400, Easing.EaseInOutQuad)
            legend.apply {
                isEnabled = true
                verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER
                orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
                isWordWrapEnabled = true
                textSize = 11f
                xEntrySpace = 10f
            }
            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}