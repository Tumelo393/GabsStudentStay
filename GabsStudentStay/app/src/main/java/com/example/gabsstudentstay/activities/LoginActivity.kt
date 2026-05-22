package com.example.gabsstudentstay.activities

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gabsstudentstay.R
import com.google.firebase.auth.FirebaseAuth

/**
 * Handles user authentication for the application.
 * Features include persistent login check, password visibility toggle,
 * and email/password authentication using Firebase.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var ivPasswordToggle: ImageView
    private lateinit var btnLogin: Button
    private lateinit var tvRegisterLink: TextView
    
    private val auth = FirebaseAuth.getInstance()
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // --- PERSISTENT SESSION CHECK ---
        // If a user is already authenticated, skip the login screen
        if (auth.currentUser != null) {
            navigateToHome()
        }
        
        setContentView(R.layout.activity_login)

        // Initialize UI components
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        ivPasswordToggle = findViewById(R.id.ivPasswordToggle)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegisterLink = findViewById(R.id.tvRegisterLink)

        // Password visibility toggle logic
        ivPasswordToggle.setOnClickListener {
            togglePasswordVisibility()
        }

        // Login action
        btnLogin.setOnClickListener { performLogin() }
        
        // Navigation to registration screen
        tvRegisterLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    /**
     * Toggles the input type of the password field between hidden and visible text.
     * Updates the toggle icon to reflect the current state.
     */
    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Hide password
            etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            ivPasswordToggle.setImageResource(android.R.drawable.ic_menu_view)
        } else {
            // Show password
            etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            ivPasswordToggle.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
        }
        isPasswordVisible = !isPasswordVisible
        
        // Maintain cursor position at the end of the text
        etPassword.setSelection(etPassword.text.length)
    }

    /**
     * Authenticates the user with Firebase using the provided email and password.
     * Displays a toast message on failure.
     */
    private fun performLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        // Basic validation for empty fields
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Firebase Sign In
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    navigateToHome()
                } else {
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    /**
     * Navigates to the HomeActivity and clears the activity stack.
     * Prevents the user from returning to the Login screen via the back button.
     */
    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
