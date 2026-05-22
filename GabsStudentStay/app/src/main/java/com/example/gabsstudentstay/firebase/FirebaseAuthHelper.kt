package com.example.gabsstudentstay.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class FirebaseAuthHelper {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun logout() {
        auth.signOut()
    }

    // Auth logic is handled directly in Activities for easier callback management
    // but we can add helper methods here if needed later.
}
