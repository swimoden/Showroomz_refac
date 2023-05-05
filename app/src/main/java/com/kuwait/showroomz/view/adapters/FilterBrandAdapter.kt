package com.kuwait.showroomz.view.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kuwait.showroomz.databinding.FilterBrandItemBinding
import com.kuwait.showroomz.model.data.Brand
import com.kuwait.showroomz.model.simplifier.BrandSimplifier
import com.kuwait.showroomz.viewModel.FilterVM

class FilterBrandAdapter(private var brands: List<Brand>, private val viewModel: FilterVM) :
    RecyclerView.Adapter<FilterBrandAdapter.FilterBrandViewHolder>() {
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
        holder.bind(brands[position])
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
            var isSelected = false

            viewModel.selectedBrands.forEach {
                if (brand.id==it.id){
                    isSelected = true
                }
            }
            binding.brand = BrandSimplifier(brand)

            binding.isSelected = isSelected
            binding.root.setOnClickListener {
                isSelected = !isSelected
                binding.isSelected = isSelected
                if (isSelected) {
                    viewModel.selectedBrands.add(brand)
                } else{
                    val index = viewModel.selectedBrands.indexOfFirst{
                        brand.id == it.id
                    }
                    if (index > -1 && index < viewModel.selectedBrands.size){
                        viewModel.selectedBrands.removeAt(index)
                    }

                }
            }
        }
    }
}