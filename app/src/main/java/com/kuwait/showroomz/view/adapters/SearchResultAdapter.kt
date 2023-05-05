package com.kuwait.showroomz.view.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.kuwait.showroomz.databinding.BrandItemBinding
import com.kuwait.showroomz.databinding.ModelItemBinding
import com.kuwait.showroomz.model.data.Brand
import com.kuwait.showroomz.model.data.Model
import com.kuwait.showroomz.model.simplifier.BrandSimplifier
import com.kuwait.showroomz.model.simplifier.ModelSimplifier
import com.kuwait.showroomz.view.fragment.SearchFragmentDirections
import io.realm.RealmObject


class SearchResultAdapter(
    var list: List<RealmObject>

) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    class ModelViewHolder(var view: ModelItemBinding) :
        RecyclerView.ViewHolder(view.root)

    class BrandViewHolder(var view: BrandItemBinding) :
        RecyclerView.ViewHolder(view.root)


    fun refresh(list: List<RealmObject>?) {
        if (list != null) {
            this.list = list
        }

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 0)
            BrandViewHolder(
                BrandItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        else ModelViewHolder(
            ModelItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount() = list.size
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is BrandViewHolder) {
            val brand = BrandSimplifier(list[position] as Brand)
            holder.view.brand = brand
            holder.itemView.setOnClickListener {
                if (brand.hasModels == true)
                Navigation.findNavController(it)
                    .navigate(SearchFragmentDirections.actionShowModelsByBrandId(list[position] as Brand, null))
                else
                    Navigation.findNavController(it)
                        .navigate(SearchFragmentDirections.actionSearchShowLocationsByBrandId(brand.id, brand.brand, emptyArray()))
            }
        }
        if (holder is ModelViewHolder) {
            holder.view.modelSimplifier = ModelSimplifier(list[position] as Model)
            holder.itemView.setOnClickListener {
                Navigation.findNavController(it)
                    .navigate(SearchFragmentDirections.aactionShowModelDetail(list[position] as Model,
                        null, null))
            }
        }

    }

    override fun getItemViewType(position: Int): Int {
        return if (list[position] is Brand) 0 else 1
    }


}




