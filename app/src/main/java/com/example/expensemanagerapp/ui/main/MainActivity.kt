package com.example.expensemanager.ui.main

import android.content.Intent // THÊM IMPORT INTENT
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.expensemanager.R
import com.example.expensemanager.databinding.ActivityMainBinding
import com.example.expensemanager.utils.ThemeHelper
import com.google.firebase.auth.FirebaseAuth // THÊM IMPORT FIREBASE
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject // THÊM IMPORT INJECT

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    // INJECT FIREBASE AUTH VÀO ĐÂY ĐỂ DÙNG CHO HÀM LOGOUT
    @Inject
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        ThemeHelper.applyThemeOnStartup(this)

        setContentView(R.layout.activity_main)
        setContentView(binding.root)

        // 1. Thiết lập Toolbar làm thanh tiêu đề chính
        setSupportActionBar(binding.toolbar)

        // Lấy NavController từ NavHostFragment
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.loginFragment,
                R.id.homeFragment,
                R.id.statsFragment,
                R.id.reportFragment,
                R.id.profileFragment
            )
        )

        // 3. Kết nối Toolbar với NavController
        setupActionBarWithNavController(navController, appBarConfiguration)

        // 4. Kết nối Bottom Navigation
        binding.bottomNavigationView.setupWithNavController(navController)

        // Loại bỏ background và XÓA BÓNG (elevation) để BottomNav trong suốt tuyệt đối
        binding.bottomNavigationView.background = null
        binding.bottomNavigationView.elevation = 0f

        // Khóa item rỗng ở giữa (chừa chỗ cho nút FAB)
        binding.bottomNavigationView.menu.getItem(2).isEnabled = false

        // 🌟 FIX LỖI KHÔNG BẤM ĐƯỢC FAB 🌟
        // Tước quyền "hấp thụ click" của cái ô menu tàng hình ở giữa để chạm xuyên qua nó
        val menuView = binding.bottomNavigationView.getChildAt(0) as? android.view.ViewGroup
        if (menuView != null && menuView.childCount > 2) {
            val middleItem = menuView.getChildAt(2)
            middleItem.isClickable = false
            middleItem.isFocusable = false
        }

        // Nhấc nút FAB lên lớp trên cùng của màn hình để ưu tiên nhận sự kiện chạm
        binding.fabAdd.bringToFront()

        // 5. Xử lý sự kiện bấm vào nút Cộng màu vàng (FAB)
        binding.fabAdd.setOnClickListener {
            navController.navigate(R.id.addTransactionFragment)
        }

        // 6. Xử lý hiển thị/ẩn thanh điều hướng, nút + và Toolbar tùy theo màn hình
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                // TRANG CHỦ: Ẩn Toolbar mặc định (vì đã có header riêng), giữ lại Menu dưới
                R.id.homeFragment -> {
                    supportActionBar?.hide()
                    binding.bottomAppBar.visibility = View.VISIBLE
                    binding.fabAdd.show()
                }

                // CÁC TRANG CHÍNH KHÁC: Hiện toàn bộ
                R.id.statsFragment,
                R.id.reportFragment,
                R.id.profileFragment -> {
                    supportActionBar?.show()
                    binding.bottomAppBar.visibility = View.VISIBLE
                    binding.fabAdd.show()
                }

                // CÁC TRANG XÁC THỰC: Ẩn sạch sẽ
                R.id.loginFragment,
                R.id.registerFragment -> {
                    supportActionBar?.hide()
                    binding.bottomAppBar.visibility = View.GONE
                    binding.fabAdd.hide()
                }

                // CÁC TRANG PHỤ KHÁC: Chỉ hiện Toolbar có nút Back
                else -> {
                    supportActionBar?.show()
                    binding.bottomAppBar.visibility = View.GONE
                    binding.fabAdd.hide()
                }
            }
        }
    }

    // HÀM LOGOUT ĐÃ ĐƯỢC SỬA LẠI CHUẨN XÁC
    fun logout() {
        auth.signOut()

        // Khởi động lại MainActivity và xóa sạch toàn bộ lịch sử (Backstack) cũ
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)

        // Kết thúc Activity hiện tại để ViewModel được giải phóng hoàn toàn
        finish()
    }

    // Giúp nút Back (mũi tên) trên Toolbar hoạt động
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}