package com.example.newsapp.UsersDataBase.UserViewModel

import com.example.newsapp.UsersDataBase.Users.UserRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch


class UserViewModel(private val userRepository: UserRepository) : ViewModel() {

    fun loginUser(email: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = userRepository.verifyUser(email, password)
            onResult(success)
        }
    }

    fun registerUser(
        email: String,
        password: String,
        confirmPassword: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            if (password != confirmPassword) {
                onResult(false, "Passwords don't match")
                return@launch
            }

            if (userRepository.isEmailExists(email)) {
                onResult(false, "Email already exists")
                return@launch
            }

            val success = userRepository.addUser(email, password)
            onResult(success, if (success) null else "Registration failed")
        }
    }
    fun getCurrentUserEmail(onResult: (String?) -> Unit) {
        viewModelScope.launch {
            val email = userRepository.getCurrentUserEmail()
            onResult(email)
        }
    }

    fun updateUserEmail(
        oldEmail: String,
        newEmail: String,
        currentPassword: String,
        callback: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val user = userRepository.isEmailExists(oldEmail)
                if (user == null) {
                    callback(false, "User not found")
                    return@launch
                }

                val isPasswordCorrect = userRepository.verifyUser(oldEmail, currentPassword)
                if (!isPasswordCorrect) {
                    callback(false, "Current password is incorrect")
                    return@launch
                }

                // Check if new email is valid
                if (newEmail.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                    callback(false, "Invalid email format")
                    return@launch
                }

                // Check if email already exists
                if (userRepository.isEmailExists(newEmail)) {
                    callback(false, "Email already in use")
                    return@launch
                }

                // Update email
                val success = userRepository.updateUserEmail(oldEmail, newEmail)
                callback(success, if (success) null else "Failed to update email")
            } catch (e: Exception) {
                callback(false, "Error: ${e.message}")
            }
        }
    }

    fun updateUserPassword(
        email: String,
        currentPassword: String,
        newPassword: String,
        confirmPassword: String,
        callback: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Verify current password first using repository's secure verification
                val isPasswordCorrect = userRepository.verifyUser(email, currentPassword)
                if (!isPasswordCorrect) {
                    callback(false, "Current password is incorrect")
                    return@launch
                }

                // Validate new password
                if (newPassword != confirmPassword) {
                    callback(false, "New passwords don't match")
                    return@launch
                }

                if (newPassword.length < 6) {
                    callback(false, "Password must be at least 6 characters")
                    return@launch
                }
                val success = userRepository.updateUserPassword(email, newPassword)
                callback(success, if (success) null else "Failed to update password")
            } catch (e: Exception) {
                callback(false, "Error: ${e.message}")
            }
        }
    }
}