package com.example.gabsstudentstay.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.example.gabsstudentstay.R

/**
 * Activity that displays detailed information about a selected accommodation.
 * Shows mandatory fields: price, location, type, amenities, availability, and deposit.
 * Handles state-specific UI changes like disabling 'Reserve' for already booked rooms.
 */
class ListingDetailsActivity : AppCompatActivity() {

    private lateinit var ivDetailImage: ImageView
    private lateinit var tvBack: TextView
    private lateinit var tvDetailTitle: TextView
    private lateinit var tvDetailLocation: TextView
    private lateinit var tvDetailPrice: TextView
    private lateinit var tvDetailDeposit: TextView
    private lateinit var tvDetailType: TextView
    private lateinit var tvDetailDate: TextView
    private lateinit var tvDetailAmenities: TextView
    private lateinit var tvDetailStatus: TextView
    private lateinit var btnContactLandlord: Button
    private lateinit var btnMakePayment: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listing_details)

        // Link UI components to the activity
        ivDetailImage       = findViewById(R.id.ivDetailImage)
        tvBack              = findViewById(R.id.tvBack)
        tvDetailTitle       = findViewById(R.id.tvDetailTitle)
        tvDetailLocation    = findViewById(R.id.tvDetailLocation)
        tvDetailPrice       = findViewById(R.id.tvDetailPrice)
        tvDetailDeposit     = findViewById(R.id.tvDetailDeposit)
        tvDetailType        = findViewById(R.id.tvDetailType)
        tvDetailDate        = findViewById(R.id.tvDetailDate)
        tvDetailAmenities   = findViewById(R.id.tvDetailAmenities)
        tvDetailStatus      = findViewById(R.id.tvDetailStatus)
        btnContactLandlord  = findViewById(R.id.btnContactLandlord)
        btnMakePayment      = findViewById(R.id.btnMakePayment)

        // --- READ INCOMING DATA ---
        // Data is passed from the ExploreFragment when a listing is clicked
        val listingId       = intent.getStringExtra("LISTING_ID")          ?: ""
        val title           = intent.getStringExtra("LISTING_TITLE")       ?: "Unknown"
        val location        = intent.getStringExtra("LISTING_LOCATION")    ?: "Unknown"
        val price           = intent.getDoubleExtra("LISTING_PRICE", 0.0)
        val deposit         = intent.getDoubleExtra("LISTING_DEPOSIT", 0.0)
        val type            = intent.getStringExtra("LISTING_TYPE")        ?: "Standard"
        val amenities       = intent.getStringExtra("LISTING_AMENITIES")   ?: "None"
        val date            = intent.getStringExtra("LISTING_DATE")        ?: "Available Now"
        val imageUrl        = intent.getStringExtra("LISTING_IMAGE")       ?: ""
        val landlordId      = intent.getStringExtra("LANDLORD_ID")          ?: ""
        val status          = intent.getStringExtra("LISTING_STATUS")      ?: "Available"

        // Update the screen with the retrieved data
        tvDetailTitle.text      = title
        tvDetailLocation.text   = "📍 $location"
        tvDetailPrice.text      = "P ${String.format("%,.0f", price)}"
        tvDetailDeposit.text    = "P ${String.format("%,.0f", deposit)}"
        tvDetailType.text       = type
        tvDetailDate.text       = date
        tvDetailAmenities.text  = amenities
        tvDetailStatus.text     = status

        // Load property image using Coil
        if (imageUrl.isNotEmpty()) {
            ivDetailImage.load(imageUrl) {
                placeholder(android.R.drawable.ic_menu_gallery)
                error(android.R.drawable.ic_menu_report_image)
            }
        }

        // --- RESERVATION LOGIC ---
        // Prevent booking if the status is 'Reserved'
        if (status == "Reserved") {
            btnMakePayment.isEnabled = false
            btnMakePayment.text = "Currently Reserved"
            btnMakePayment.alpha = 0.5f // Visual indicator of disabled state
            tvDetailStatus.setTextColor(getColor(R.color.error))
        } else {
            tvDetailStatus.setTextColor(getColor(R.color.success))
        }

        tvBack.setOnClickListener { finish() }

        // Start chat session with the specific landlord
        btnContactLandlord.setOnClickListener {
            val chatIntent = Intent(this, ChatActivity::class.java).apply {
                putExtra("CHAT_LANDLORD_NAME", "Landlord of $title")
                putExtra("RECIPIENT_ID", landlordId)
            }
            startActivity(chatIntent)
        }

        // Navigate to payment/reservation workflow
        btnMakePayment.setOnClickListener {
            val paymentIntent = Intent(this, PaymentActivity::class.java).apply {
                putExtra("PAYMENT_ID", listingId)
                putExtra("PAYMENT_TITLE", title)
                putExtra("PAYMENT_PRICE", price)
            }
            startActivity(paymentIntent)
        }
    }
}
