package com.kuwait.showroomz.view.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.kuwait.showroomz.R
import com.kuwait.showroomz.model.data.Model
import com.kuwait.showroomz.model.simplifier.ModelSimplifier

class TrendingModelAdapter(private var list:List<Model>, private val action: (model: Model) -> (Unit))
    : RecyclerView.Adapter<TrendingModelAdapter.ModelViewExclusiveHolder>()  {
    class ModelViewExclusiveHolder(var view: com.kuwait.showroomz.databinding.TrendingModelItemBinding) : RecyclerView.ViewHolder(view.root)

    fun refresh(models: List<Model>) {
        this.list = models
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModelViewExclusiveHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = DataBindingUtil.inflate<com.kuwait.showroomz.databinding.TrendingModelItemBinding>(inflater, R.layout.trending_model_item,parent,false)
        return ModelViewExclusiveHolder(view)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ModelViewExclusiveHolder, position: Int) {
       val model = list[position]
        holder.view.model = ModelSimplifier(model)

        holder.view.root.setOnClickListener {
            action.invoke(model)
        }
    }
}