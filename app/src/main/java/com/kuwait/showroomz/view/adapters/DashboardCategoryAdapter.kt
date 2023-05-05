package com.kuwait.showroomz.view.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.DashbordCategoryItemBinding
import com.kuwait.showroomz.extras.Shared
import com.kuwait.showroomz.extras.Shared.Companion.oldIndex
import com.kuwait.showroomz.extras.Shared.Companion.selectedIndex
import com.kuwait.showroomz.extras.blockingClickListener
import com.kuwait.showroomz.model.data.Category
import com.kuwait.showroomz.model.simplifier.CategorySimplifier
import com.kuwait.showroomz.view.MainActivity
import com.kuwait.showroomz.view.fragment.DashboardFragmentDirections
import kotlinx.android.synthetic.main.fragment_test_drive_address.view.*

class DashboardCategoryAdapter(
    var categories: List<Category>,
   var activity: Context?
) : RecyclerView.Adapter<DashboardCategoryAdapter.CategoryViewHolder>(){


    fun refresh(categories: List<Category>){
        this.categories = categories
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = DataBindingUtil.inflate<DashbordCategoryItemBinding>(inflater, R.layout.dashbord_category_item, parent, false)
        return CategoryViewHolder(view)
    }

    override fun getItemCount() = categories.size

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.view.category = CategorySimplifier(category)
        holder.view.root.blockingClickListener {
            selectedIndex = 0
            oldIndex = 0
            (activity as MainActivity).selectedCategory=categories[position]
            val action = DashboardFragmentDirections.actionShowBrandsByCategory(category,null, null)
            Navigation.findNavController(holder.view.root).navigate(action)
        }

        /*holder.view.root.setOnClickListener{
            selectedIndex = 0
            oldIndex = 0
            (activity as MainActivity).selectedCategory=categories[position]
            val action = DashboardFragmentDirections.actionShowBrandsByCategory(category,null, null)
            Navigation.findNavController(it).navigate(action)
        }*/
    }

    class CategoryViewHolder(var view: DashbordCategoryItemBinding) : RecyclerView.ViewHolder(view.root)

}