package com.example.newsapp.Screens

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.newsapp.MainActivity
import com.example.newsapp.UsersDataBase.UserDatabase
import com.example.newsapp.UsersDataBase.UserViewModel.UserViewModel
import com.example.newsapp.UsersDataBase.Users.UserRepository
import com.example.newsapp.databinding.ActivityLoginScreenBinding

class Login_Screen : AppCompatActivity() {
    private lateinit var binding: ActivityLoginScreenBinding
    private lateinit var authViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val database = UserDatabase.getDatabase(this)
        authViewModel = UserViewModel(UserRepository(database.userDao()))

        binding.btnLogin.setOnClickListener {
            val email = binding.loginEmail.text.toString().trim()
            val password = binding.loginPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "All fields required!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            authViewModel.loginUser(email, password) { success ->
                if (success) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.signupNavigation.setOnClickListener {
            startActivity(Intent(this, SignUp_Screen::class.java))
        }
    }
}