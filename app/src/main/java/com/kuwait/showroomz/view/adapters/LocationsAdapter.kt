package com.kuwait.showroomz.view.adapters

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.LocationItemBinding
import com.kuwait.showroomz.model.data.ClickType
import com.kuwait.showroomz.model.data.Location
import com.kuwait.showroomz.model.simplifier.LocationSimplifier

/**
*
*
clickType = .ITEM  when we click on item
clickType = .CALLBACK when we click on request Appointment
*
*
* */

class LocationsAdapter(private var locations:List<Location>, private val action: (location: LocationSimplifier, position:Int, clickType: ClickType) -> (Unit)):RecyclerView.Adapter<LocationsAdapter.LocationViewHolder>() {

    class LocationViewHolder(var view: LocationItemBinding) : RecyclerView.ViewHolder(view.root)

    fun refresh(locations:List<Location>){
        this.locations = locations
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<LocationItemBinding>(inflater,  R.layout.location_item, parent, false )
        return LocationViewHolder(binding)
    }

    override fun getItemCount() = locations.count()

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
       val location = LocationSimplifier(locations[position])
        holder.view.location = location
        holder.view.root.setOnClickListener{
            action.invoke(location,position, ClickType.ITEM)
        }

        holder.view.navigate.setOnClickListener {
            val gmmIntentUri = Uri.parse(
                "google.navigation:q=${location.latitude},${location.longitude}&mode=d"
            )
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(holder.itemView.context,mapIntent,null)
        }

        holder.view.callback.setOnClickListener{
            action.invoke(location,position, ClickType.CALLBACK)
        }
    }
}