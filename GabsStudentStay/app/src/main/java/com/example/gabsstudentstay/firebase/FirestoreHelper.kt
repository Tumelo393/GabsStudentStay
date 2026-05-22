package com.example.gabsstudentstay.firebase

import com.example.gabsstudentstay.models.User
import com.example.gabsstudentstay.models.Listing
import com.example.gabsstudentstay.models.Message
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class FirestoreHelper {
    private val db = FirebaseFirestore.getInstance()

    // --- USER PROFILE ---
    fun saveUserProfile(user: User, onComplete: (Boolean) -> Unit) {
        db.collection("users").document(user.uid).set(user)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun getUserProfile(uid: String, onResult: (User?) -> Unit) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { onResult(it.toObject(User::class.java)) }
            .addOnFailureListener { onResult(null) }
    }

    fun updateUserPreferences(uid: String, prefs: Map<String, Any>, onComplete: (Boolean) -> Unit) {
        db.collection("users").document(uid).update("preferences", prefs)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    // --- LISTINGS ---
    fun getAllListings(onResult: (List<Listing>) -> Unit) {
        db.collection("listings").get()
            .addOnSuccessListener { onResult(it.toObjects(Listing::class.java)) }
            .addOnFailureListener { onResult(emptyList()) }
    }

    fun saveListing(listing: Listing, onComplete: (Boolean, String?) -> Unit) {
        val ref = db.collection("listings").document()
        val newListing = listing.copy(id = ref.id)
        ref.set(newListing)
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { onComplete(false, it.message) }
    }

    fun updateListingStatus(listingId: String, status: String, onComplete: (Boolean) -> Unit) {
        db.collection("listings").document(listingId).update("status", status)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    // --- CHAT ---
    fun sendMessage(chatId: String, message: Message, participants: List<String>, onComplete: (Boolean) -> Unit) {
        db.collection("chats").document(chatId)
            .set(mapOf("participants" to participants, "lastMessage" to message.text, "timestamp" to System.currentTimeMillis()))
        
        db.collection("chats").document(chatId).collection("messages")
            .add(message)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun listenForMessages(chatId: String, onResult: (List<Message>) -> Unit) {
        db.collection("chats").document(chatId).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) onResult(snapshot.toObjects(Message::class.java))
            }
    }

    fun getMyChats(userId: String, onResult: (List<Map<String, Any>>) -> Unit) {
        db.collection("chats")
            .whereArrayContains("participants", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val chats = snapshot.documents.map { doc ->
                        val data = doc.data?.toMutableMap() ?: mutableMapOf()
                        data["id"] = doc.id
                        data
                    }
                    onResult(chats)
                }
            }
    }
}
