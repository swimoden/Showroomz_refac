package com.kuwait.showroomz.view.adapters

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kuwait.showroomz.databinding.ActionsGridItemBinding
import com.kuwait.showroomz.databinding.TrimCompareItemBinding
import com.kuwait.showroomz.extras.DesignUtils
import com.kuwait.showroomz.model.data.Action
import com.kuwait.showroomz.model.data.Trim
import com.kuwait.showroomz.model.simplifier.ActionSimplifier
import com.kuwait.showroomz.model.simplifier.TrimSimplifier

class CompareTrimsAdapter( var trims: List<Trim>) :
    RecyclerView.Adapter<CompareTrimsAdapter.TrimViewHolder>() {
    var selectedItem = 0
    var lastSelected = 0
    private lateinit var listener: CompareTrimsAdapter.OnItemClickListener
    class TrimViewHolder(var view: TrimCompareItemBinding) : RecyclerView.ViewHolder(view.root) {
        fun bind(trim: Trim?) {
            view.trim = trim?.let { TrimSimplifier(it) }
        }
    }
    fun setOnItemCLickListener(listener: CompareTrimsAdapter.OnItemClickListener){
        this.listener=listener

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrimViewHolder {
        return TrimViewHolder(
            TrimCompareItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return trims.size
    }

    override fun onBindViewHolder(holder: TrimViewHolder, position: Int) {
        holder.bind(trims[position])
        holder.view.checkbox.isChecked=selectedItem==position
        holder.itemView.setOnClickListener {
            listener.onItemClick(trims[position])
            lastSelected = selectedItem
            selectedItem = position
            notifyItemChanged(lastSelected)
            notifyItemChanged(selectedItem)
        }

    }

    fun refreshActions(trims: List<Trim>) {

     this.trims=trims
        notifyDataSetChanged()
    }

    interface OnItemClickListener{
        fun onItemClick(trim: Trim?)
    }
}