package com.kuwait.showroomz.view.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kuwait.showroomz.databinding.ModelTypeItemBinding
import com.kuwait.showroomz.model.data.Type
import com.kuwait.showroomz.model.simplifier.TypeSimplifier
import com.kuwait.showroomz.viewModel.FilterVM

class ModelTypesAdapter(private var types: List<Type>, private val viewModel: FilterVM) :
    RecyclerView.Adapter<ModelTypesAdapter.ModelTypeViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModelTypeViewHolder {
        return ModelTypeViewHolder(
            ModelTypeItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        )
    }

    override fun getItemCount(): Int {
        return types.size
    }

    override fun onBindViewHolder(holder: ModelTypeViewHolder, position: Int) {
        holder.bind(types[position])
    }

    fun refreshData(types: List<Type>?) {
        if (types != null) {
            this.types = types
            notifyDataSetChanged()
        }
    }

    inner class ModelTypeViewHolder(var binding: ModelTypeItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(type: Type) {
            var isSelected = false
            viewModel.selectedType.forEach {
                if (type.id==it.id){
                    isSelected = true
                }
            }

            Log.e("ModelTypesAdapter", "bind: $isSelected" )
            binding.isSelected=isSelected
            binding.type = TypeSimplifier(type)
            binding.root.setOnClickListener {

                isSelected = !isSelected
                binding.isSelected = isSelected
                if (isSelected) {
                    viewModel.selectedType.add(type)
                } else {
                    val index = viewModel.selectedType.indexOfFirst { s -> s.id == type.id }
                    if (index > -1) {
                        viewModel.selectedType.removeAt(index)
                    }
                    /*var iterator =viewModel.selectedType.iterator()
                    iterator.forEach {
                        if (type.id==it.id){
                            iterator.remove()
                        }
                    }
                    var list = iterator.asSequence().toCollection(ArrayList())

                    viewModel.selectedType=list*/

                }


            }

        }
    }
}