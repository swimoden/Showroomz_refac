package com.kuwait.showroomz.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.kuwait.showroomz.databinding.TrimServiceItemBinding
import com.kuwait.showroomz.model.data.Service
import com.kuwait.showroomz.model.simplifier.ServiceSimplifier
import io.realm.RealmList

class TrimServiceAdapter(private val services: RealmList<Service>) :
    RecyclerView.Adapter<TrimServiceAdapter.ServiceVH>() {
    val filteredService =services.filter { it.isEnabled==true }
    private var index: Int = -1

    class ServiceVH(var view: TrimServiceItemBinding) : RecyclerView.ViewHolder(view.root) {
        fun bind(service: Service?) {
            view.service = service?.let { ServiceSimplifier(it) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceVH {
        return ServiceVH(
            TrimServiceItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        )

    }

    override fun getItemCount(): Int {
        return filteredService.size
    }

    override fun onBindViewHolder(holder: ServiceVH, position: Int) {
        if (index == position) {
//            holder.view.serviceNameContainer.visibility = View.VISIBLE
        } else {
//            holder.view.serviceNameContainer.visibility = View.INVISIBLE
        }

        holder.itemView.setOnClickListener {
            index = position
            notifyDataSetChanged()

        }
        holder.bind(filteredService[position])




    }
}