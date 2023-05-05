package com.kuwait.showroomz.view.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kuwait.showroomz.databinding.ChooseModelItemBinding
import com.kuwait.showroomz.databinding.FilterBrandItemBinding
import com.kuwait.showroomz.model.data.Brand
import com.kuwait.showroomz.model.data.Model
import com.kuwait.showroomz.model.simplifier.BrandSimplifier
import com.kuwait.showroomz.model.simplifier.ModelSimplifier

class ChooseModelAdapter(private var models: List<Model>) :
    RecyclerView.Adapter<ChooseModelAdapter.ModelViewHolder>() {
    var index = -1
    var selectedItem = -1
    var lastSelected = -1
    private lateinit var listener: OnItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModelViewHolder {
        return ModelViewHolder(
            ChooseModelItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        )
    }

    override fun getItemCount(): Int {
        return models.size
    }

    override fun onBindViewHolder(holder: ModelViewHolder, position: Int) {
        holder.binding.isSelected = selectedItem == position
        holder.itemView.setOnClickListener {
            if (listener != null) {
                listener.onItemClick(models[position])
            }
            lastSelected = selectedItem

            selectedItem = position


            notifyItemChanged(lastSelected)


            notifyItemChanged(selectedItem)

        }
        holder.bind(models[position])
    }
    fun setOnItemCLickListener(listener: OnItemClickListener) {
        this.listener = listener

    }
    fun refreshData(models: List<Model>?) {
        if (models != null) {
            this.models = models
            notifyDataSetChanged()
        }
    }

    inner class ModelViewHolder(var binding: ChooseModelItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(model: Model) {
            binding.model = ModelSimplifier(model)


        }
    }
    interface OnItemClickListener {
        fun onItemClick(model: Model)
    }
}