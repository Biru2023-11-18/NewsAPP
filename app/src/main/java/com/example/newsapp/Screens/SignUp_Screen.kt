package com.example.newsapp.Screens

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.newsapp.UsersDataBase.UserDatabase
import com.example.newsapp.UsersDataBase.Users.UserRepository
import com.example.newsapp.R
import com.example.newsapp.UsersDataBase.UserViewModel.UserViewModel
import com.example.newsapp.databinding.ActivitySignUpBinding
import kotlinx.coroutines.launch

// SignUp_Screen.kt
class SignUp_Screen : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var authViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val database = UserDatabase.getDatabase(this)
        authViewModel = UserViewModel(UserRepository(database.userDao()))

        binding.btnSignUp.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            authViewModel.registerUser(email, password, confirmPassword) { success, message ->
                if (success) {
                    Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, Login_Screen::class.java))
                    finish()
                } else {
                    Toast.makeText(this, message ?: "Error occurred", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.loginNavigation.setOnClickListener {
            startActivity(Intent(this, Login_Screen::class.java))
        }
    }
}