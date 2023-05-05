package com.kuwait.showroomz.view.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.BrandsCategoryItemBinding
import com.kuwait.showroomz.model.data.Category
import com.kuwait.showroomz.model.simplifier.CategorySimplifier

class BrandsCategoryAdapter(var categories: List<Category>, private val action: (category:Category) -> (Unit)) : RecyclerView.Adapter<BrandsCategoryAdapter.CategoryViewHolder>(){


    fun refresh(categories: List<Category>){
        this.categories = categories.filter { it.isEnabled==true }
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = DataBindingUtil.inflate<BrandsCategoryItemBinding>(inflater, R.layout.brands_category_item, parent, false)
        return CategoryViewHolder(view)
    }

    override fun getItemCount() = categories.size

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.view.contair.isSelected = category.selected
        holder.view.category = CategorySimplifier(category)
        holder.view.root.setOnClickListener{
            action.invoke(category)
           // val action = DashboardFragmentDirections.actionShowBrandsByCategory(category)
            //Navigation.findNavController(it).navigate(action)
        }
    }

    class CategoryViewHolder(var view: BrandsCategoryItemBinding) : RecyclerView.ViewHolder(view.root)

}