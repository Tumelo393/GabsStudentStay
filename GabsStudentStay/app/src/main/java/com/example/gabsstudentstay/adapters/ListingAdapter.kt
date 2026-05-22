package com.example.gabsstudentstay.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.gabsstudentstay.R
import com.example.gabsstudentstay.models.Listing

class ListingAdapter(
    private val listings: List<Listing>,
    private val onItemClick: (Listing) -> Unit
) : RecyclerView.Adapter<ListingAdapter.ListingViewHolder>() {

    class ListingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivImage: ImageView = view.findViewById(R.id.ivListingImage)
        val tvTitle: TextView = view.findViewById(R.id.tvListingTitle)
        val tvLocation: TextView = view.findViewById(R.id.tvListingLocation)
        val tvPrice: TextView = view.findViewById(R.id.tvListingPrice)
        val tvType: TextView = view.findViewById(R.id.tvListingRooms) // Using existing ID for House Type
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_listing, parent, false)
        return ListingViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListingViewHolder, position: Int) {
        val listing = listings[position]
        
        holder.tvTitle.text = listing.title
        holder.tvLocation.text = "📍 ${listing.location}"
        holder.tvPrice.text = "P ${listing.price}"
        holder.tvType.text = listing.houseType
        
        if (listing.imageUrl.isNotEmpty()) {
            holder.ivImage.load(listing.imageUrl) {
                crossfade(true)
                placeholder(android.R.drawable.ic_menu_gallery)
                error(android.R.drawable.ic_menu_report_image)
            }
        }

        holder.itemView.setOnClickListener { onItemClick(listing) }
    }

    override fun getItemCount() = listings.size
}
