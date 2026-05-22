package com.example.gabsstudentstay.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.gabsstudentstay.R
import com.example.gabsstudentstay.activities.LoginActivity
import com.example.gabsstudentstay.firebase.FirestoreHelper
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvRole: TextView
    private lateinit var etPrefLocation: EditText
    private lateinit var etPrefMaxPrice: EditText
    private lateinit var btnSavePrefs: Button
    private lateinit var btnLogout: Button

    private val auth = FirebaseAuth.getInstance()
    private val firestoreHelper = FirestoreHelper()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        tvName = view.findViewById(R.id.tvProfileName)
        tvEmail = view.findViewById(R.id.tvProfileEmail)
        tvRole = view.findViewById(R.id.tvProfileRole)
        etPrefLocation = view.findViewById(R.id.etPrefLocation)
        etPrefMaxPrice = view.findViewById(R.id.etPrefMaxPrice)
        btnSavePrefs = view.findViewById(R.id.btnSavePrefs)
        btnLogout = view.findViewById(R.id.btnLogout)

        loadUserData()

        btnSavePrefs.setOnClickListener { savePreferences() }
        btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(activity, LoginActivity::class.java))
            activity?.finish()
        }

        return view
    }

    private fun loadUserData() {
        val uid = auth.currentUser?.uid ?: return
        firestoreHelper.getUserProfile(uid) { user ->
            user?.let {
                tvName.text = it.fullName
                tvEmail.text = it.email
                tvRole.text = "Role: ${it.role.replaceFirstChar { c -> c.uppercase() }}"
                
                // Load existing preferences
                it.preferences?.let { prefs ->
                    etPrefLocation.setText(prefs["location"] as? String ?: "")
                    etPrefMaxPrice.setText((prefs["maxPrice"] as? Double)?.toInt()?.toString() ?: "")
                }
            }
        }
    }

    private fun savePreferences() {
        val uid = auth.currentUser?.uid ?: return
        val location = etPrefLocation.text.toString().trim()
        val maxPrice = etPrefMaxPrice.text.toString().toDoubleOrNull() ?: 0.0

        val prefs = mapOf(
            "location" to location,
            "maxPrice" to maxPrice
        )

        firestoreHelper.updateUserPreferences(uid, prefs) { success ->
            if (success) {
                Toast.makeText(context, "Preferences saved for Smart Alerts!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
