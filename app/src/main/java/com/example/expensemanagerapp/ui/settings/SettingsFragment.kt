package com.example.expensemanager.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.expensemanager.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    // Giả sử bạn sẽ tạo SettingsViewModel để quản lý logic
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadCurrentSettings()
        setupListeners()
    }

    private fun loadCurrentSettings() {
        // Lấy dữ liệu cũ đã lưu để hiển thị lên màn hình
        val currentLimit = viewModel.getMonthlyLimit()
        val isReminderEnabled = viewModel.getReminderStatus()

        binding.etMonthlyLimit.setText(currentLimit.toString())
        binding.switchReminder.isChecked = isReminderEnabled
    }

    private fun setupListeners() {
        // Xử lý khi bấm nút LƯU CÀI ĐẶT
        binding.btnSaveSettings.setOnClickListener {
            val limitStr = binding.etMonthlyLimit.text.toString()
            val isReminder = binding.switchReminder.isChecked

            if (limitStr.isNotEmpty()) {
                val limit = limitStr.toDouble()
                viewModel.saveSettings(limit, isReminder)

                Toast.makeText(requireContext(), "Đã lưu cài đặt!", Toast.LENGTH_SHORT).show()

                requireActivity().onBackPressedDispatcher.onBackPressed()
            } else {
                Toast.makeText(requireContext(), "Vui lòng nhập hạn mức", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}