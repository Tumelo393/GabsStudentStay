package com.example.gabsstudentstay.activities

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gabsstudentstay.R
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class PaymentActivity : AppCompatActivity() {

    private lateinit var tvSummaryTitle: TextView
    private lateinit var tvSummaryPrice: TextView
    private lateinit var tvSummaryDeposit: TextView
    private lateinit var etCardName: EditText
    private lateinit var etCardNumber: EditText
    private lateinit var btnConfirmPayment: Button

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        tvSummaryTitle = findViewById(R.id.tvSummaryTitle)
        tvSummaryPrice = findViewById(R.id.tvSummaryPrice)
        tvSummaryDeposit = findViewById(R.id.tvSummaryDeposit)
        etCardName = findViewById(R.id.etCardName)
        etCardNumber = findViewById(R.id.etCardNumber)
        btnConfirmPayment = findViewById(R.id.btnConfirmPayment)

        val listingId = intent.getStringExtra("PAYMENT_ID") ?: ""
        val title = intent.getStringExtra("PAYMENT_TITLE") ?: ""
        val price = intent.getDoubleExtra("PAYMENT_PRICE", 0.0)

        tvSummaryTitle.text = title
        tvSummaryPrice.text = "P $price"
        tvSummaryDeposit.text = "P $price" // Deposit usually equals 1 month rent

        findViewById<TextView>(R.id.tvBack).setOnClickListener { finish() }

        btnConfirmPayment.setOnClickListener {
            if (validatePayment()) {
                processPayment(listingId)
            }
        }
    }

    private fun validatePayment(): Boolean {
        if (etCardName.text.isEmpty() || etCardNumber.text.length < 16) {
            Toast.makeText(this, "Invalid card details", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun processPayment(listingId: String) {
        btnConfirmPayment.isEnabled = false
        
        // 1. Update Firestore status to 'Reserved'
        db.collection("listings").document(listingId)
            .update("status", "Reserved")
            .addOnSuccessListener {
                showReceipt()
            }
            .addOnFailureListener {
                btnConfirmPayment.isEnabled = true
                Toast.makeText(this, "Reservation failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showReceipt() {
        val refNumber = "REF-${UUID.randomUUID().toString().substring(0, 8).uppercase()}"
        
        AlertDialog.Builder(this)
            .setTitle("Payment Successful")
            .setMessage("Your reservation is confirmed!\n\nReference: $refNumber\nStatus: Reserved")
            .setCancelable(false)
            .setPositiveButton("Done") { _, _ ->
                finish() // Returns to details which will refresh
            }
            .show()
    }
}
