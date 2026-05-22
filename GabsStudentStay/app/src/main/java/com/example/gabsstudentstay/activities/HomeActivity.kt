package com.example.gabsstudentstay.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.gabsstudentstay.R
import com.example.gabsstudentstay.fragments.ExploreFragment
import com.example.gabsstudentstay.fragments.MessagesFragment
import com.example.gabsstudentstay.fragments.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * Main container Activity that hosts the app's primary navigation.
 * Uses a BottomNavigationView to switch between Explore, Messages, and Profile fragments.
 */
class HomeActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        bottomNav = findViewById(R.id.bottom_navigation)
        
        // --- DEFAULT VIEW ---
        // Load the Explore screen by default when the app opens
        loadFragment(ExploreFragment())

        // Bottom Navigation selection logic
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> loadFragment(ExploreFragment())
                R.id.nav_chat -> loadFragment(MessagesFragment())
                R.id.nav_profile -> loadFragment(ProfileFragment())
            }
            true
        }
    }

    /**
     * Replaces the current fragment in the container with the selected one.
     * @param fragment The new fragment to display.
     */
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
