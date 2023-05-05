package com.kuwait.showroomz.view.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kuwait.showroomz.databinding.TrimColorItemBinding
import com.kuwait.showroomz.model.data.Color
import com.kuwait.showroomz.model.simplifier.ColorSimplifier
import io.realm.RealmList

class TrimColorsAdapter(var list: List<Color>) :RecyclerView.Adapter<TrimColorsAdapter.ColorViewHolder>(){
    class ColorViewHolder(var view: TrimColorItemBinding) : RecyclerView.ViewHolder(view.root) {
        fun bind(color: Color) {
            view.color=ColorSimplifier(color)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {

        return ColorViewHolder(
            TrimColorItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        )

    }

    override fun getItemCount(): Int {
       return list.size
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        holder.bind(list.get(position))
    }

    fun refreshData(colors: RealmList<Color>?) {
        if (colors != null) {
            this.list=colors
            notifyDataSetChanged()
        }
    }
}