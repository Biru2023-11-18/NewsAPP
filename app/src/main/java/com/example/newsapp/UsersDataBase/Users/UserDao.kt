package com.example.newsapp.UsersDataBase.Users

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserDao {
    @Insert
    suspend fun insertUser(user: User): Long

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE email = :email)")

    suspend fun isEmailExists(email: String): Boolean

    @Query("SELECT email FROM users ORDER BY id DESC LIMIT 1")
    suspend fun getLastLoggedInEmail(): String?
    @Query("UPDATE users SET email = :newEmail WHERE email = :oldEmail")
    suspend fun updateUserEmail(oldEmail: String, newEmail: String): Int

    @Query("UPDATE users SET password = :newPassword, salt = :newSalt WHERE email = :email")
    suspend fun updateUserPasswordAndSalt(email: String, newPassword: String, newSalt: String): Int
}