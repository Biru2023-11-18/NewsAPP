package com.example.newsapp.UsersDataBase.Users

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class UserRepository(private val userDao: UserDao) {
    private val TAG = "UserRepository"
    private val ITERATIONS = 10000
    private val KEY_LENGTH = 256
    private val SALT_LENGTH = 32


    suspend fun addUser(email: String, password: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val cleanEmail = email.trim().lowercase()
                if (userDao.isEmailExists(cleanEmail)) {
                    Log.d(TAG, "Email already exists")
                    return@withContext false
                }

                val salt = generateSalt()
                val hashedPassword = hashPassword(password, salt)

                val user = User(
                    email = cleanEmail,
                    password = hashedPassword,
                    salt = salt
                )

                val userId = userDao.insertUser(user)
                Log.d(TAG, "User created with ID: $userId")
                userId > 0
            } catch (e: Exception) {
                Log.e(TAG, "Error creating user", e)
                false
            }
        }
    }

    suspend fun verifyUser(email: String, password: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val cleanEmail = email.trim().lowercase()
                val user = userDao.getUserByEmail(cleanEmail) ?: run {
                    Log.d(TAG, "User not found")
                    return@withContext false
                }

                val isValid = verifyPassword(password, user.salt, user.password)
                Log.d(TAG, "Password verification ${if (isValid) "successful" else "failed"}")
                isValid
            } catch (e: Exception) {
                Log.e(TAG, "Error verifying user", e)
                false
            }
        }
    }
    suspend fun updateUserEmail(oldEmail: String, newEmail: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val cleanOldEmail = oldEmail.trim().lowercase()
                val cleanNewEmail = newEmail.trim().lowercase()

                // Check if new email already exists
                if (userDao.isEmailExists(cleanNewEmail)) {
                    Log.d(TAG, "New email already exists")
                    return@withContext false
                }

                // Update email in database
                val rowsAffected = userDao.updateUserEmail(cleanOldEmail, cleanNewEmail)
                rowsAffected > 0
            } catch (e: Exception) {
                Log.e(TAG, "Error updating email", e)
                false
            }
        }
    }

    suspend fun updateUserPassword(email: String, newPassword: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val cleanEmail = email.trim().lowercase()

                // Generate new salt and hash for the new password
                val newSalt = generateSalt()
                val newHashedPassword = hashPassword(newPassword, newSalt)

                // Update password and salt in database
                val rowsAffected = userDao.updateUserPasswordAndSalt(
                    cleanEmail,
                    newHashedPassword,
                    newSalt
                )
                rowsAffected > 0
            } catch (e: Exception) {
                Log.e(TAG, "Error updating password", e)
                false
            }
        }
    }

    suspend fun isEmailExists(email: String): Boolean {
        return withContext(Dispatchers.IO) {
            userDao.isEmailExists(email.trim().lowercase())
        }
    }

    private fun generateSalt(): String {
        val salt = ByteArray(SALT_LENGTH)
        SecureRandom().nextBytes(salt)
        return salt.toHex()
    }

    private fun hashPassword(password: String, salt: String): String {
        val saltBytes = salt.hexToBytes()
        val spec = PBEKeySpec(
            password.toCharArray(),
            saltBytes,
            ITERATIONS,
            KEY_LENGTH
        )
        val skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = skf.generateSecret(spec).encoded
        return hash.toHex()
    }

    private fun verifyPassword(password: String, salt: String, storedHash: String): Boolean {
        val testHash = hashPassword(password, salt)
        return secureCompare(testHash, storedHash)
    }

    private fun secureCompare(a: String, b: String): Boolean {
        if (a.length != b.length) return false
        var result = 0
        for (i in a.indices) {
            result = result or (a[i].code xor b[i].code)
        }
        return result == 0
    }


    private fun ByteArray.toHex(): String {
        return joinToString("") { "%02x".format(it) }
    }

    private fun String.hexToBytes(): ByteArray {
        return chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }

    suspend fun getCurrentUserEmail(): String? {
        return withContext(Dispatchers.IO) {
            userDao.getLastLoggedInEmail()
        }
    }





}