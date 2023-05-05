package com.kuwait.showroomz.view.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.kuwait.showroomz.databinding.FilterBrandItemBinding
import com.kuwait.showroomz.model.data.Brand
import com.kuwait.showroomz.model.simplifier.BrandSimplifier

class ChooseBrandAdapter(private var brands: List<Brand>) :
    RecyclerView.Adapter<ChooseBrandAdapter.FilterBrandViewHolder>() {
    var index = -1
    var selectedItem = -1
    var lastSelected = -1
    private lateinit var listener: OnItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterBrandViewHolder {
        return FilterBrandViewHolder(
            FilterBrandItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        )
    }

    override fun getItemCount(): Int {
        return brands.size
    }

    override fun onBindViewHolder(holder: FilterBrandViewHolder, position: Int) {
        holder.binding.isSelected = selectedItem == position
        holder.itemView.setOnClickListener {
            if (listener != null) {
                listener.onItemClick(brands[position])
            }
            lastSelected = selectedItem

            selectedItem = position


            notifyItemChanged(lastSelected)


            notifyItemChanged(selectedItem)

        }
        holder.bind(brands[position])
    }
    fun setOnItemCLickListener(listener: OnItemClickListener) {
        this.listener = listener

    }
    fun refreshData(brands: List<Brand>?) {
        if (brands != null) {
            this.brands = brands
            notifyDataSetChanged()
        }
    }

    inner class FilterBrandViewHolder(var binding: FilterBrandItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(brand: Brand) {
            binding.brand = BrandSimplifier(brand)
        }
    }
    interface OnItemClickListener {
        fun onItemClick(brand: Brand)
    }
}