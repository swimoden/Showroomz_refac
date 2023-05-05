package com.kuwait.showroomz.view.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kuwait.showroomz.databinding.ChooseYearItemBinding
import com.kuwait.showroomz.model.data.Industry
import com.kuwait.showroomz.model.simplifier.IndustrySimplifier

class IndustriesAdapter(private var  industries: List<Industry>?) :
    RecyclerView.Adapter<IndustriesAdapter.YearViewHolder>() {
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
        return industries?.size!!
    }

    override fun onBindViewHolder(holder: YearViewHolder, position: Int) {
        holder.binding.isSelected = selectedItem == position
        holder.itemView.setOnClickListener {
            if (listener != null) {
                industries?.get(position)?.let { it1 -> listener.onItemClick(it1) }
            }
            lastSelected = selectedItem

            selectedItem = position


            notifyItemChanged(lastSelected)


            notifyItemChanged(selectedItem)

        }
        industries?.get(position)?.let { holder.bind(it) }
    }
    fun setOnItemCLickListener(listener: OnItemClickListener) {
        this.listener = listener

    }
    fun refreshData( industries: List<Industry>?) {
        if (industries != null) {
            this.industries = industries
            notifyDataSetChanged()
        }
    }

    inner class YearViewHolder(var binding: ChooseYearItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(industry: Industry) {
            binding.yearTxt.text=IndustrySimplifier(industry).name


        }
    }
    interface OnItemClickListener {
        fun onItemClick(industry: Industry)
    }
}