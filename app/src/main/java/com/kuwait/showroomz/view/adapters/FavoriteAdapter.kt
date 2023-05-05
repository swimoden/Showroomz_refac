package com.kuwait.showroomz.view.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.kuwait.showroomz.R
import com.kuwait.showroomz.model.data.Favorite
import com.kuwait.showroomz.model.simplifier.FavoriteSimplifier
import com.kuwait.showroomz.view.fragment.FavoriteFragmentDirections
import com.kuwait.showroomz.viewModel.FavoriteVM

class FavoriteAdapter(private var list: MutableList<Favorite>,val viewModel: FavoriteVM) :
    RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder>() {


    class FavoriteViewHolder(var view: com.kuwait.showroomz.databinding.FavoriteItemBinding) :
        RecyclerView.ViewHolder(view.root)

    fun refresh(list: List<Favorite>) {
        this.list = list.toMutableList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val inflater = LayoutInflater.from(parent.context)

            val view =
                DataBindingUtil.inflate<com.kuwait.showroomz.databinding.FavoriteItemBinding>(
                    inflater,
                    R.layout.favorite_item,
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
        holder.view.favoriteImg.setOnClickListener {
            viewModel.removeFromFavoriteList(list[position])
            //list.remove(list[position])
            //notifyDataSetChanged()
           // if (list.isEmpty()) viewModel.empty.value=true
        }
        holder.itemView.setOnClickListener {
            val simp = FavoriteSimplifier(favorite)
            simp.model.model?.let { it1 ->
                FavoriteFragmentDirections.showModelDetail(
                    it1,
                    null, null
                )
            }.let { it2 -> Navigation.findNavController(it).navigate(it2) }
        }

    }

    override fun getItemViewType(position: Int): Int {
        return if (list[position].modelData?.id != null) 0 else 1
    }
}