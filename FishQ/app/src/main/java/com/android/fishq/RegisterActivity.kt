package com.android.fishq

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.fishq.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var btnLogin: Button
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

//        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.buttonregis.setOnClickListener {
            val email = binding.textInputEmailRegis.editText?.text.toString()
            val pass = binding.textInputPasswordRegis.editText?.text.toString()
            val confirmPass = binding.textInputPasswordConfirm.editText?.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty() && confirmPass.isNotEmpty()) {
                if (pass == confirmPass) {
                    firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener {
                        if (it.isSuccessful) {
//                            val intent = Intent(this, LoginActivity::class.java)
                            finish()
                            Toast.makeText(this, "Signup successful", Toast.LENGTH_SHORT).show()

                        } else {
                            Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }

                } else {
                    Toast.makeText(this, "Password is not match", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Empty field is not allowed!", Toast.LENGTH_SHORT).show()
            }

        }

        ///kembali ke login
        btnLogin = findViewById(R.id.buttonlogin)
        btnLogin.setOnClickListener {
//            val intent = Intent(this, LoginActivity::class.java)
//            startActivity(intent)
            finish()
        }
    }
}