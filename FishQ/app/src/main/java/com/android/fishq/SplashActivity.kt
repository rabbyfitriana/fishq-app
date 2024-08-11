package com.android.fishq

import android.content.Intent
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {
    private lateinit var logoAnim: Animation
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        firebaseAuth = FirebaseAuth.getInstance()

        // Memuat gambar logo
        val logoImageView = findViewById<ImageView>(R.id.logo_splash)

        // Membuat animasi
        logoAnim = AnimationUtils.loadAnimation(this, R.anim.anim_splash)
        logoImageView.startAnimation(logoAnim)

        // Memulai aktivitas utama setelah animasi selesai
        logoAnim.setAnimationListener(animationListener)

    }

    private val animationListener = object : Animation.AnimationListener {
        override fun onAnimationStart(animation: Animation) {}

        override fun onAnimationEnd(animation: Animation) {
            // Periksa status login setelah animasi selesai
            val intent = if (firebaseAuth.currentUser != null) {
                Intent(this@SplashActivity, MainActivity::class.java)
            } else {
                Intent(this@SplashActivity, LoginActivity::class.java)
            }
            startActivity(intent)
            finish()
        }

        override fun onAnimationRepeat(animation: Animation) {}
    }
}