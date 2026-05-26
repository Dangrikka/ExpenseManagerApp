package com.example.expensemanager.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.expensemanager.R
import com.example.expensemanager.ui.main.MainActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Animate elements
        val scaleIn = AnimationUtils.loadAnimation(this, R.anim.scale_in)
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)
        
        findViewById<ImageView>(R.id.ivLogo).startAnimation(scaleIn)
        findViewById<TextView>(R.id.tvAppName).startAnimation(slideUp)
        findViewById<TextView>(R.id.tvAppSubtitle).startAnimation(slideUp)

        // Delay and navigate to MainActivity
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        }, 2000)
    }
}
