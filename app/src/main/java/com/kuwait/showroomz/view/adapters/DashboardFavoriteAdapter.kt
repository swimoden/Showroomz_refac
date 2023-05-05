package com.kuwait.showroomz.view.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.DashboardFavoriteItemBinding
import com.kuwait.showroomz.model.data.Favorite
import com.kuwait.showroomz.model.simplifier.FavoriteSimplifier
import com.kuwait.showroomz.view.fragment.DashboardFragmentDirections

class DashboardFavoriteAdapter(private var list: List<Favorite>) :
    RecyclerView.Adapter<DashboardFavoriteAdapter.FavoriteViewHolder>() {
    class FavoriteViewHolder(var view: DashboardFavoriteItemBinding) :
        RecyclerView.ViewHolder(view.root)


    fun refresh(favorites: List<Favorite>) {
        this.list = favorites
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        val view =
            DataBindingUtil.inflate<com.kuwait.showroomz.databinding.DashboardFavoriteItemBinding>(
                inflater,
                R.layout.dashboard_favorite_item,
                parent,
                false
            )
        return FavoriteViewHolder(view)


    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val favorite = list[position]

        holder.view.favorite = FavoriteSimplifier(favorite)
        holder.view.modelCategory.apply {
            paint?.isUnderlineText = true
        }
        holder.itemView
            .setOnClickListener {
                val simp = FavoriteSimplifier(favorite)
                simp.model.model?.let { it1 ->
                    DashboardFragmentDirections.aactionShowModelDetail(
                        it1,
                        null, null
                    )
                }.let { it2 -> Navigation.findNavController(it).navigate(it2) }
            }
    }


}