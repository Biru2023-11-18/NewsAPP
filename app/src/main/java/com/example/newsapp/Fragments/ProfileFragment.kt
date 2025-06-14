package com.example.newsapp.Fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.newsapp.MainActivity
import com.example.newsapp.MainActivity.MyApplication
import com.example.newsapp.R
import com.example.newsapp.Screens.Welcome_Screen
import com.example.newsapp.UsersDataBase.UserViewModel.UserViewModel
import com.example.newsapp.UsersDataBase.Users.UserRepository
import com.example.newsapp.databinding.DialogAboutBinding
import com.example.newsapp.databinding.FragmentProfileBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val PREFS_NAME = "profile_prefs"
private const val KEY_PROFILE_URI = "profile_uri"

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var ivProfile: ImageView
    private lateinit var tvNavEmail: TextView
    private lateinit var userViewModel: UserViewModel
    private lateinit var sharedPreferences: SharedPreferences

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            sharedPreferences.edit().putString(KEY_PROFILE_URI, it.toString()).apply()
            Glide.with(this)
                .load(it)
                .circleCrop()
                .into(ivProfile)
            Toast.makeText(requireContext(), "Profile picture updated", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val headerView = binding.profilenavigation.getHeaderView(0)
        ivProfile = headerView.findViewById(R.id.ivProfile)
        tvNavEmail = headerView.findViewById(R.id.useremail)

        ivProfile.setOnClickListener {
            showGalleryAccessDialog()
        }

        val application = requireContext().applicationContext as MyApplication
        val userDao = application.db.userDao()
        userViewModel = UserViewModel(UserRepository(userDao))

        // Load saved profile image
        val savedUri = sharedPreferences.getString(KEY_PROFILE_URI, null)
        savedUri?.let {
            Glide.with(this)
                .load(Uri.parse(it))
                .circleCrop()
                .into(ivProfile)
        }

        getCurrentUserEmail()
        setupNavigation()

        return binding.root
    }

    private fun getCurrentUserEmail() {
        userViewModel.getCurrentUserEmail { email ->
            CoroutineScope(Dispatchers.Main).launch {
                tvNavEmail.text = email ?: "Not logged in"
            }
        }
    }

    private fun setupNavigation() {
        binding.profilenavigation.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.aboutus -> {
                    showAboutDialog()
                    true
                }
                R.id.logoutmenu -> {
                    showLogoutConfirmation()
                    true
                }
                R.id.Homepage -> {
                    navigateToMainActivity()
                    true
                }
                R.id.myfav -> {
                    navigateToFragment(BookMarks_Fragment())
                    true
                }
                R.id.useremail -> {
                    showUserEmailDialog()
                    true
                }
                R.id.ivProfile -> {
                    showGalleryAccessDialog()
                    true
                }
                R.id.setting -> {
                    showSettingDialog()
                    true
                }
                else -> false
            }
        }
    }

    private fun showSettingDialog() {
        val options = arrayOf("Change Email", "Change Password")
        AlertDialog.Builder(requireContext())
            .setTitle("Account Settings")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showChangeEmailDialog()
                    1 -> showChangePasswordDialog()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showChangeEmailDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_change_email, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Change Email")
            .setView(dialogView)
            .setPositiveButton("Change") { _, _ ->
                val newEmail = dialogView.findViewById<EditText>(R.id.etNewEmail).text.toString()
                val password = dialogView.findViewById<EditText>(R.id.etPassword).text.toString()
                if (newEmail.isBlank() || password.isBlank()) {
                    Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                } else {
                    changeEmail(newEmail, password)
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }

    private fun showChangePasswordDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_change_password, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Change Password")
            .setView(dialogView)
            .setPositiveButton("Change") { _, _ ->
                val currentPassword = dialogView.findViewById<EditText>(R.id.etCurrentPassword).text.toString()
                val newPassword = dialogView.findViewById<EditText>(R.id.etNewPassword).text.toString()
                val confirmPassword = dialogView.findViewById<EditText>(R.id.etConfirmPassword).text.toString()
                if (currentPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) {
                    Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                } else {
                    changePassword(currentPassword, newPassword, confirmPassword)
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }

    private fun changeEmail(newEmail: String, password: String) {
        val sharedPref = requireContext().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val currentEmail = sharedPref.getString("email", "") ?: ""

        if (currentEmail.isEmpty()) {
            Toast.makeText(requireContext(), "No user logged in", Toast.LENGTH_SHORT).show()
            return
        }

        userViewModel.updateUserEmail(
            oldEmail = currentEmail,
            newEmail = newEmail,
            currentPassword = password
        ) { success, message ->
            activity?.runOnUiThread {
                if (success) {
                    sharedPref.edit().putString("email", newEmail).apply()
                    tvNavEmail.text = newEmail
                    Toast.makeText(requireContext(), "Email updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), message ?: "Failed to update email", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun changePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        if (newPassword != confirmPassword) {
            Toast.makeText(requireContext(), "Passwords don't match", Toast.LENGTH_SHORT).show()
            return
        }

        val sharedPref = requireContext().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val email = sharedPref.getString("email", "") ?: ""

        if (email.isEmpty()) {
            Toast.makeText(requireContext(), "No user logged in", Toast.LENGTH_SHORT).show()
            return
        }

        userViewModel.updateUserPassword(
            email = email,
            currentPassword = currentPassword,
            newPassword = newPassword,
            confirmPassword = confirmPassword
        ) { success, message ->
            activity?.runOnUiThread {
                if (success) {
                    Toast.makeText(requireContext(), "Password updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), message ?: "Failed to update password", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showAboutDialog() {
        val dialogBinding = DialogAboutBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation

        dialogBinding.aboutTitle.text = getString(R.string.about_title)
        dialogBinding.AboutContent.text = getString(R.string.aboutus)
        dialogBinding.btnClose.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun navigateToMainActivity() {
        val intent = Intent(requireContext(), MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        requireActivity().finish()
    }

    private fun navigateToFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.search_frame_container, fragment)
            .addToBackStack(fragment.javaClass.simpleName)
            .commit()
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { _, _ ->
                performLogout()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun performLogout() {
        val sharedPref = requireContext().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        sharedPref.edit().remove("email").apply()

        val intent = Intent(requireContext(), Welcome_Screen::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        requireActivity().finish()
    }

    private fun showGalleryAccessDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Access Gallery")
            .setMessage("Allow this app to access your photos to set a profile picture?")
            .setPositiveButton("Yes") { dialog, _ ->
                pickImageLauncher.launch("image/*")
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun showUserEmailDialog() {
        val sharedPref = requireContext().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val userEmail = sharedPref.getString("email", "Not logged in")

        AlertDialog.Builder(requireContext())
            .setTitle("User Email")
            .setMessage("Logged in as:\n$userEmail")
            .setPositiveButton("OK", null)
            .show()
    }
}