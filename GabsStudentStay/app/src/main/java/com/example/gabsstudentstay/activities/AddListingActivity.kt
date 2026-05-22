package com.example.gabsstudentstay.activities

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.gabsstudentstay.R
import com.example.gabsstudentstay.firebase.FirestoreHelper
import com.example.gabsstudentstay.firebase.StorageHelper
import com.example.gabsstudentstay.models.Listing
import com.example.gabsstudentstay.utils.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import java.util.*

/**
 * Activity for landlords/providers to add a new property listing.
 * Handles image selection, data validation, and uploading to Firebase Storage and Firestore.
 */
class AddListingActivity : AppCompatActivity() {

    // UI Elements
    private lateinit var tvBack: TextView
    private lateinit var layoutImageUpload: FrameLayout
    private lateinit var ivListingPreview: ImageView
    private lateinit var layoutPlaceholder: LinearLayout
    private lateinit var etTitle: EditText
    private lateinit var etDescription: EditText
    private lateinit var etPrice: EditText
    private lateinit var etDeposit: EditText
    private lateinit var spinnerHouseType: Spinner
    private lateinit var spinnerLocation: Spinner
    private lateinit var etAvailabilityDate: EditText
    private lateinit var btnSubmitListing: Button

    // Firebase and Utility Helpers
    private var selectedImageUri: Uri? = null
    private val firestoreHelper = FirestoreHelper()
    private val storageHelper = StorageHelper()
    private val auth = FirebaseAuth.getInstance()

    // Launcher for selecting an image from the gallery
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            ivListingPreview.setImageURI(uri)
            ivListingPreview.visibility = View.VISIBLE
            layoutPlaceholder.visibility = View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_listing)

        // Initialize UI components
        tvBack = findViewById(R.id.tvBack)
        layoutImageUpload = findViewById(R.id.layoutImageUpload)
        ivListingPreview = findViewById(R.id.ivListingPreview)
        layoutPlaceholder = findViewById(R.id.layoutPlaceholder)
        etTitle = findViewById(R.id.etTitle)
        etDescription = findViewById(R.id.etDescription)
        etPrice = findViewById(R.id.etPrice)
        etDeposit = findViewById(R.id.etDeposit)
        spinnerHouseType = findViewById(R.id.spinnerHouseType)
        spinnerLocation = findViewById(R.id.spinnerLocation)
        etAvailabilityDate = findViewById(R.id.etAvailabilityDate)
        btnSubmitListing = findViewById(R.id.btnSubmitListing)

        // Populate dropdown menus
        setupSpinners()

        // Set click listeners
        tvBack.setOnClickListener { finish() }
        layoutImageUpload.setOnClickListener { pickImageLauncher.launch("image/*") }
        etAvailabilityDate.setOnClickListener { showDatePicker() }

        btnSubmitListing.setOnClickListener {
            if (validateForm()) {
                uploadImageAndSaveListing()
            }
        }
    }

    /**
     * Sets up the adapters for the House Type and Location Spinners using string arrays from resources.
     */
    private fun setupSpinners() {
        val houseTypes = resources.getStringArray(R.array.house_types)
        val locations = resources.getStringArray(R.array.gaborone_locations)

        val houseTypeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, houseTypes)
        spinnerHouseType.adapter = houseTypeAdapter

        val locationAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, locations)
        spinnerLocation.adapter = locationAdapter
    }

    /**
     * Displays a DatePickerDialog to allow the user to select the property's availability date.
     */
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = "$dayOfMonth/${month + 1}/$year"
                etAvailabilityDate.setText(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    /**
     * Initiates the listing saving process. First uploads the selected image to Firebase Storage,
     * then saves the listing data (including the image URL) to Firestore.
     */
    private fun uploadImageAndSaveListing() {
        val title = etTitle.text.toString().trim()
        val amenities = etDescription.text.toString().trim()
        val price = etPrice.text.toString().toDoubleOrNull() ?: 0.0
        val deposit = etDeposit.text.toString().toDoubleOrNull() ?: 0.0
        val houseType = spinnerHouseType.selectedItem.toString()
        val location = spinnerLocation.selectedItem.toString()
        val availabilityDate = etAvailabilityDate.text.toString().trim()
        val landlordId = auth.currentUser?.uid ?: ""

        btnSubmitListing.isEnabled = false
        Toast.makeText(this, "Publishing Listing...", Toast.LENGTH_SHORT).show()

        if (selectedImageUri != null) {
            // Upload selected image to Firebase Storage
            storageHelper.uploadListingImage(selectedImageUri!!) { imageUrl ->
                if (imageUrl != null) {
                    saveListing(Listing(
                        title = title,
                        price = price,
                        depositAmount = deposit,
                        location = location,
                        houseType = houseType,
                        amenities = amenities,
                        availabilityDate = availabilityDate,
                        imageUrl = imageUrl,
                        landlordId = landlordId,
                        status = "Available"
                    ))
                } else {
                    btnSubmitListing.isEnabled = true
                    Toast.makeText(this, "Permission Error in Storage. Check Rules.", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            // Save listing without an image if none selected
            saveListing(Listing(title = title, price = price, depositAmount = deposit, location = location, houseType = houseType, amenities = amenities, availabilityDate = availabilityDate, imageUrl = "", landlordId = landlordId, status = "Available"))
        }
    }

    /**
     * Saves the final Listing object to the Firestore database.
     * Also triggers a local notification for the user to simulate a 'Smart Alert'.
     */
    private fun saveListing(listing: Listing) {
        firestoreHelper.saveListing(listing) { success, error ->
            if (success) {
                // Trigger a local alert for the user (Simulated Preference Match)
                NotificationHelper(this).showListingAlert(
                    "Smart Alert Match!",
                    "A new ${listing.houseType} is available in ${listing.location} for P${listing.price}"
                )
                Toast.makeText(this, "Listing posted successfully!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                btnSubmitListing.isEnabled = true
                Toast.makeText(this, "Error: $error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Validates that all required fields are filled before allowing submission.
     * @return true if valid, false otherwise.
     */
    private fun validateForm(): Boolean {
        return when {
            etTitle.text.isEmpty() -> { etTitle.error = "Required"; false }
            etPrice.text.isEmpty() -> { etPrice.error = "Required"; false }
            etAvailabilityDate.text.isEmpty() -> { etAvailabilityDate.error = "Required"; false }
            else -> true
        }
    }
}
