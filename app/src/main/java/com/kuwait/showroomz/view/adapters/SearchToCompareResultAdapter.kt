package com.kuwait.showroomz.view.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.BrandItemBinding
import com.kuwait.showroomz.databinding.ModelItemBinding
import com.kuwait.showroomz.model.data.Brand
import com.kuwait.showroomz.model.data.Model
import com.kuwait.showroomz.model.simplifier.ModelSimplifier


class SearchToCompareResultAdapter(
    var list: List<Model>

) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    class ModelViewHolder(var view: ModelItemBinding) :
        RecyclerView.ViewHolder(view.root)


    fun refresh(list: List<Model>?) {
        if (list != null) {
            this.list = list
        }

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ModelViewHolder(
            ModelItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )


    }

    override fun getItemCount() = list.size
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder is ModelViewHolder) {
            holder.view.modelSimplifier = ModelSimplifier(list[position])
            holder.itemView.setOnClickListener {
                Navigation.findNavController(it).previousBackStackEntry?.savedStateHandle?.set("MODEL", list[position])
                Navigation.findNavController(it).navigateUp()
//                Navigation.findNavController(it).popBackStack(R.id.searchToCompareFragment,true)
            }
        }

    }

    override fun getItemViewType(position: Int): Int {
        return if (list[position] is Brand) 0 else 1
    }


}




