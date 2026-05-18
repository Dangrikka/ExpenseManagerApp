package com.example.expensemanager.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.expensemanager.R
import com.example.expensemanager.databinding.FragmentProfileBinding
import com.example.expensemanager.domain.model.TransactionModel
import com.example.expensemanager.domain.model.TransactionType
import com.example.expensemanager.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateUI()
        setupListeners()
    }

    private fun updateUI() {
        val user = viewModel.getCurrentUser()

        val avatarFile = File(requireContext().filesDir, "profile_avatar.jpg")
        if (avatarFile.exists()) {
            binding.ivAvatar.setImageURI(Uri.fromFile(avatarFile))
        }

        if (user != null) {
            binding.tvUserName.text = user.displayName ?: "Thành viên"
            binding.tvUserEmail.text = user.email
            binding.layoutAuthButtons.visibility = View.GONE
            binding.btnLogout.visibility = View.VISIBLE
            binding.dividerLogout.visibility = View.VISIBLE
        } else {
            binding.tvUserName.text = "Khách truy cập"
            binding.tvUserEmail.text = "Đăng nhập để đồng bộ dữ liệu đám mây"
            binding.layoutAuthButtons.visibility = View.VISIBLE
            binding.btnLogout.visibility = View.GONE
            binding.dividerLogout.visibility = View.GONE
        }
    }

    private fun setupListeners() {
        binding.btnSettings.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_settingsFragment)
        }

        binding.btnLogin.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        }

        binding.btnRegister.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_registerFragment)
        }

        binding.btnLogout.setOnClickListener {
            viewModel.logout()
            Toast.makeText(requireContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show()

            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }

        binding.btnSyncCloud.setOnClickListener {
            if (viewModel.getCurrentUser() != null) {
                Toast.makeText(requireContext(), "Đang tiến hành đồng bộ dữ liệu...", Toast.LENGTH_SHORT).show()
                viewModel.syncDataFromCloud()
            } else {
                Toast.makeText(requireContext(), "Vui lòng đăng nhập để đồng bộ!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.ivAvatar.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.tvUserName.setOnClickListener {
            val user = viewModel.getCurrentUser()
            if (user != null) {
                val currentName = user.displayName ?: ""
                showEditNameDialog(currentName)
            } else {
                Toast.makeText(requireContext(), "Bạn cần đăng nhập mới có thể đổi tên!", Toast.LENGTH_LONG).show()
                findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
            }
        }

        binding.btnExport.setOnClickListener {
            Toast.makeText(requireContext(), "Đang chuẩn bị dữ liệu...", Toast.LENGTH_SHORT).show()

            lifecycleScope.launch {
                val transactions = viewModel.getAllTransactions()
                if (transactions.isNotEmpty()) {
                    exportToCSV(transactions)
                } else {
                    Toast.makeText(requireContext(), "Không có dữ liệu để xuất", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun exportToCSV(transactions: List<TransactionModel>) {
        try {
            val fileName = "BaoCaoChiTieu_${System.currentTimeMillis()}.csv"
            val file = File(requireContext().cacheDir, fileName)

            FileWriter(file).use { writer ->
                writer.append('\uFEFF')
                writer.append("Ngày,Danh mục,Loại,Tên giao dịch,Số tiền (VND),Ghi chú\n")

                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

                for (tx in transactions) {
                    val dateStr = sdf.format(tx.date)
                    val typeStr = if (tx.type == TransactionType.INCOME) "Thu" else "Chi"
                    val safeTitle = "\"${tx.title.replace("\"", "\"\"")}\""
                    val safeNote = "\"${(tx.note ?: "").replace("\"", "\"\"")}\""

                    writer.append("$dateStr,${tx.category},$typeStr,$safeTitle,${tx.amount},$safeNote\n")
                }
            }

            shareFile(file)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Lỗi tạo file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareFile(file: File) {
        val uri: Uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, "Báo cáo chi tiêu Expense Manager")
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(shareIntent, "Xuất báo cáo qua:"))
    }

    private val pickImageLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            saveImageToInternalStorage(it)
        }
    }

    private fun saveImageToInternalStorage(uri: Uri) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val file = File(requireContext().filesDir, "profile_avatar.jpg")
            val outputStream = java.io.FileOutputStream(file)

            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()

            binding.ivAvatar.setImageURI(Uri.fromFile(file))
            Toast.makeText(requireContext(), "Cập nhật ảnh thành công!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Lỗi tải ảnh: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showEditNameDialog(currentName: String) {
        val editText = android.widget.EditText(requireContext()).apply {
            setText(currentName)
            setHint("Nhập tên mới của bạn")
            setPadding(48, 48, 48, 48)
        }

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Đổi tên hiển thị")
            .setView(editText)
            .setPositiveButton("Lưu") { _, _ ->
                val newName = editText.text.toString().trim()
                if (newName.isNotEmpty() && newName != currentName) {
                    viewModel.updateUserName(newName) { isSuccess, errorMsg ->
                        if (isSuccess) {
                            Toast.makeText(requireContext(), "Đã cập nhật tên", Toast.LENGTH_SHORT).show()
                            updateUI()
                        } else {
                            Toast.makeText(requireContext(), "Lỗi: $errorMsg", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}