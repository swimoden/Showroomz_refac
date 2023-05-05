package com.kuwait.showroomz.view.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kuwait.showroomz.databinding.ChooseModelItemBinding
import com.kuwait.showroomz.databinding.ChooseYearItemBinding
import com.kuwait.showroomz.model.data.Model
import com.kuwait.showroomz.model.simplifier.ModelSimplifier

class ChooseGenderAdapter(private var years: Array<String>) :
    RecyclerView.Adapter<ChooseGenderAdapter.YearViewHolder>() {
    var index = -1
    var selectedItem = -1
    var lastSelected = -1
    private lateinit var listener: OnItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YearViewHolder {
        return YearViewHolder(
            ChooseYearItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        )
    }

    override fun getItemCount(): Int {
        return years.size
    }

    override fun onBindViewHolder(holder: YearViewHolder, position: Int) {
        holder.binding.isSelected = selectedItem == position
        holder.itemView.setOnClickListener {
            if (listener != null) {
                listener.onItemClick(years[position])
            }
            lastSelected = selectedItem

            selectedItem = position


            notifyItemChanged(lastSelected)


            notifyItemChanged(selectedItem)

        }
        holder.bind(years[position])
    }
    fun setOnItemCLickListener(listener: OnItemClickListener) {
        this.listener = listener

    }
    fun refreshData(years: Array<String>?) {
        if (years != null) {
            this.years = years
            notifyDataSetChanged()
        }
    }

    inner class YearViewHolder(var binding: ChooseYearItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(year: String) {
            binding.yearTxt.text=year


        }
    }
    interface OnItemClickListener {
        fun onItemClick(year: String)
    }
}