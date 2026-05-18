package com.example.expensemanager.ui.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.expensemanager.databinding.FragmentSearchBinding
import com.example.expensemanager.ui.home.TransactionAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SearchViewModel by viewModels()
    private lateinit var transactionAdapter: TransactionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        observeData()

    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter()
        binding.rvSearchResults.apply {
            adapter = transactionAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupListeners() {
        // Nút quay lại
        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // Nút X (Clear text)
        binding.ivClear.setOnClickListener {
            binding.etSearch.text?.clear()
        }

        // Xử lý khi nhấn vào các Chip gợi ý
        val chipClickListener = View.OnClickListener { view ->
            val chip = view as com.google.android.material.chip.Chip
            val text = chip.text.toString()
            binding.etSearch.setText(text)
            // Đưa con trỏ chuột về cuối chữ
            binding.etSearch.setSelection(text.length)
        }

        binding.chipFood.setOnClickListener(chipClickListener)
        binding.chipShopping.setOnClickListener(chipClickListener)
        binding.chipTravel.setOnClickListener(chipClickListener)
        binding.chipIncome.setOnClickListener(chipClickListener)

        // Lắng nghe người dùng gõ phím
        binding.etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString()?.trim() ?: ""

                if (query.isEmpty()) {
                    // KHI KHÔNG CÓ CHỮ: Hiện gợi ý, Ẩn X, Ẩn danh sách
                    binding.ivClear.visibility = View.GONE
                    binding.layoutSuggestions.visibility = View.VISIBLE
                    binding.rvSearchResults.visibility = View.GONE
                    binding.emptyStateLayout.visibility = View.GONE
                } else {
                    // KHI CÓ CHỮ: Hiện X, Ẩn gợi ý, Bắt đầu tìm kiếm
                    binding.ivClear.visibility = View.VISIBLE
                    binding.layoutSuggestions.visibility = View.GONE
                    viewModel.search(query)
                }
            }

            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.searchResults.collect { results ->
                    transactionAdapter.submitList(results)

                    // Cập nhật giao diện Trống nếu không có kết quả
                    if (results.isEmpty()) {
                        binding.emptyStateLayout.visibility = View.VISIBLE
                        binding.rvSearchResults.visibility = View.GONE
                    } else {
                        binding.emptyStateLayout.visibility = View.GONE
                        binding.rvSearchResults.visibility = View.VISIBLE
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