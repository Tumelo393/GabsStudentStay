package com.example.gabsstudentstay.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gabsstudentstay.R
import com.example.gabsstudentstay.activities.AddListingActivity
import com.example.gabsstudentstay.activities.ListingDetailsActivity
import com.example.gabsstudentstay.adapters.ListingAdapter
import com.example.gabsstudentstay.firebase.FirestoreHelper
import com.example.gabsstudentstay.models.Listing
import com.google.android.material.slider.RangeSlider
import com.google.firebase.auth.FirebaseAuth

class ExploreFragment : Fragment() {

    private lateinit var rvListings: RecyclerView
    private lateinit var etSearch: EditText
    private lateinit var btnFilter: Button
    private lateinit var tvUserName: TextView
    private lateinit var btnAddListing: Button
    
    private val auth = FirebaseAuth.getInstance()
    private val firestoreHelper = FirestoreHelper()
    private var allListings = listOf<Listing>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_explore, container, false)
        
        rvListings = view.findViewById(R.id.rvExploreListings)
        etSearch = view.findViewById(R.id.etSearch)
        btnFilter = view.findViewById(R.id.btnFilter)
        tvUserName = view.findViewById(R.id.tvUserName)
        btnAddListing = view.findViewById(R.id.btnAddListing)
        
        rvListings.layoutManager = LinearLayoutManager(context)
        
        loadUserSession()
        loadListings()
        
        setupSearch()
        
        btnFilter.setOnClickListener { showFilterDialog() }
        btnAddListing.setOnClickListener {
            startActivity(Intent(activity, AddListingActivity::class.java))
        }
        
        return view
    }

    private fun loadUserSession() {
        val uid = auth.currentUser?.uid ?: return
        firestoreHelper.getUserProfile(uid) { user ->
            user?.let {
                tvUserName.text = it.fullName
                if (it.role.lowercase() == "provider" || it.role.lowercase() == "landlord") {
                    btnAddListing.visibility = View.VISIBLE
                } else {
                    btnAddListing.visibility = View.GONE
                }
            }
        }
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().lowercase().trim()
                filterBySearch(query)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterBySearch(query: String) {
        val filtered = allListings.filter { 
            it.title.lowercase().contains(query) || 
            it.location.lowercase().contains(query)
        }
        updateUI(filtered)
    }

    private fun loadListings() {
        firestoreHelper.getAllListings { listings ->
            allListings = listings
            updateUI(allListings)
        }
    }

    private fun updateUI(listings: List<Listing>) {
        val adapter = ListingAdapter(listings) { listing ->
            val intent = Intent(context, ListingDetailsActivity::class.java).apply {
                putExtra("LISTING_ID", listing.id)
                putExtra("LISTING_TITLE", listing.title)
                putExtra("LISTING_LOCATION", listing.location)
                putExtra("LISTING_PRICE", listing.price)
                putExtra("LISTING_DEPOSIT", listing.depositAmount)
                putExtra("LISTING_TYPE", listing.houseType)
                putExtra("LISTING_AMENITIES", listing.amenities)
                putExtra("LISTING_DATE", listing.availabilityDate)
                putExtra("LISTING_DESCRIPTION", listing.description)
                putExtra("LISTING_IMAGE", listing.imageUrl)
                putExtra("LANDLORD_ID", listing.landlordId)
                putExtra("LISTING_STATUS", listing.status)
            }
            startActivity(intent)
        }
        rvListings.adapter = adapter
    }

    private fun showFilterDialog() {
        val context = context ?: return
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_filter, null)
        val etFilterLocation = dialogView.findViewById<EditText>(R.id.etFilterLocation)
        val priceSlider = dialogView.findViewById<RangeSlider>(R.id.priceRangeSlider)
        val tvRangeValue = dialogView.findViewById<TextView>(R.id.tvPriceRangeValue)

        priceSlider.addOnChangeListener { slider, _, _ ->
            val values = slider.values
            tvRangeValue.text = "P ${values[0].toInt()} - P ${values[1].toInt()}"
        }

        AlertDialog.Builder(context)
            .setTitle("Smart Filter")
            .setView(dialogView)
            .setPositiveButton("Apply") { _, _ ->
                val min = priceSlider.values[0].toDouble()
                val max = priceSlider.values[1].toDouble()
                val loc = etFilterLocation.text.toString().lowercase().trim()
                
                val filtered = allListings.filter { 
                    it.location.lowercase().contains(loc) && 
                    it.price >= min && it.price <= max
                }
                updateUI(filtered)
            }
            .setNegativeButton("Reset") { _, _ -> updateUI(allListings) }
            .show()
    }
}
