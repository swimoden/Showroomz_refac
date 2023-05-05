package com.kuwait.showroomz.view.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kuwait.showroomz.databinding.CompareItemSpecBinding
import com.kuwait.showroomz.model.data.Action
import com.kuwait.showroomz.model.simplifier.CompareResult

class CompareResultAdapter( var list: List<CompareResult>) :
    RecyclerView.Adapter<CompareResultAdapter.CompareResulViewHolder>() {
    private lateinit var listener: CompareResultAdapter.OnItemClickListener
    class CompareResulViewHolder(var view: CompareItemSpecBinding) : RecyclerView.ViewHolder(view.root) {
        fun bind(result: CompareResult?) {
            view.model = result
        }
    }
    fun setOnItemCLickListener(listener: CompareResultAdapter.OnItemClickListener){
        this.listener=listener

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompareResulViewHolder {
        return CompareResulViewHolder(
            CompareItemSpecBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: CompareResulViewHolder, position: Int) {
        holder.bind(list[position])
    }

    fun refresh(list: List<CompareResult>) {

     this.list=list
        notifyDataSetChanged()
    }

    interface OnItemClickListener{
        fun onItemClick(action: Action?)
    }
}