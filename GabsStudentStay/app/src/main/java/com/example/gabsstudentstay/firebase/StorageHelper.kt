package com.example.gabsstudentstay.firebase

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class StorageHelper {
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    fun uploadListingImage(imageUri: Uri, onComplete: (String?) -> Unit) {
        val fileName = "listings/${UUID.randomUUID()}.jpg"
        val ref = storageRef.child(fileName)

        ref.putFile(imageUri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { url ->
                    onComplete(url.toString())
                }
            }
            .addOnFailureListener {
                onComplete(null)
            }
    }
}
