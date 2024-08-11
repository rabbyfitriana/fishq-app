package com.android.fishq

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    // Inisialisasi variabel BottomNavigationView
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        firebaseAuth = FirebaseAuth.getInstance()

        // Periksa status login
        if (firebaseAuth.currentUser == null) {
            // Jika pengguna belum login, arahkan ke LoginActivity
            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }

        // Mengatur aplikasi untuk selalu menggunakan mode cahaya
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // Mengatur pendengar untuk BottomNavigationView untuk menangani klik item
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener{menuItem->
            when(menuItem.itemId){
                R.id.beranda -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.pindai -> {
                    replaceFragment(ScanFragment())
                    true
                }
                R.id.panduan -> {
                    replaceFragment(GuideFragment())
                    true
                }
                else -> false
            }

        }

        // Ganti fragmen utama dengan HomeFragment
        replaceFragment(HomeFragment())

        // Mengatur bantalan untuk tata letak utama agar sesuai dengan sisipan jendela sistem
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // Menetapkan fungsi untuk mengganti fragmen saat ini dengan fragmen yang baru
    private fun replaceFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction().replace(R.id.flFragment, fragment).commit()
    }
}
