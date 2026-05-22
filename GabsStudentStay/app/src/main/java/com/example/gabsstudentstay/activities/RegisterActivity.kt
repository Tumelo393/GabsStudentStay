package com.example.gabsstudentstay.activities

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.gabsstudentstay.R
import com.example.gabsstudentstay.firebase.FirestoreHelper
import com.example.gabsstudentstay.models.User
import com.google.firebase.auth.FirebaseAuth

/**
 * Activity that handles new user registration.
 * Allows users to create an account as either a Student or a Landlord/Provider.
 * Account data is stored in Firebase Authentication and a profile is created in Firestore.
 */
class RegisterActivity : AppCompatActivity() {

    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var rgRole: RadioGroup
    private lateinit var btnRegister: Button
    private lateinit var tvLoginLink: TextView
    private lateinit var tvBack: TextView

    private val auth = FirebaseAuth.getInstance()
    private val firestoreHelper = FirestoreHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize UI components and link to layout IDs
        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        rgRole = findViewById(R.id.rgRole)
        btnRegister = findViewById(R.id.btnRegister)
        tvLoginLink = findViewById(R.id.tvLoginLink)
        tvBack = findViewById(R.id.tvBack)

        // Set up click listeners for navigation and registration action
        tvBack.setOnClickListener { finish() }
        tvLoginLink.setOnClickListener { finish() }

        btnRegister.setOnClickListener {
            performRegistration()
        }
    }

    /**
     * Validates user input and creates a new account in Firebase Authentication.
     */
    private fun performRegistration() {
        val fullName = etFullName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()
        
        // Determine selected role from RadioGroup
        val selectedRoleId = rgRole.checkedRadioButtonId
        val role = if (selectedRoleId == R.id.rbLandlord) "landlord" else "student"

        // Basic input validation
        if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        // Firebase Auth: Create account with email and password
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = task.result?.user
                    if (firebaseUser != null) {
                        // Create a User object to store additional profile data in Firestore
                        val newUser = User(
                            uid = firebaseUser.uid,
                            fullName = fullName,
                            email = email,
                            phone = phone,
                            role = role
                        )
                        saveUserToFirestore(newUser)
                    }
                } else {
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    /**
     * Saves the user profile data to Firestore after successful authentication.
     */
    private fun saveUserToFirestore(user: User) {
        firestoreHelper.saveUserProfile(user) { success ->
            if (success) {
                Toast.makeText(this, "Welcome, ${user.fullName}!", Toast.LENGTH_SHORT).show()
                navigateToHome()
            } else {
                Toast.makeText(this, "Failed to save profile. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Navigates to the main HomeActivity and clears the back stack.
     */
    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
