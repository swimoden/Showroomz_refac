package com.kuwait.showroomz.view.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.BankItemBinding
import com.kuwait.showroomz.model.data.Bank
import com.kuwait.showroomz.model.simplifier.BankSimplifier

class BanksAdapter(private var banks: List<Bank>, var context: Context?) :

    RecyclerView.Adapter<BanksAdapter.BankViewHolder>() {
    var index = -1
     var selectedItem = -1
     var lastSelected = -1
      var clickable:Boolean =true
    private lateinit var listener: BanksAdapter.OnItemClickListener

    class BankViewHolder(var view: BankItemBinding) : RecyclerView.ViewHolder(view.root) {
        fun bind(bank: Bank?) {
            view.bank = bank?.let { BankSimplifier(it) }
        }
    }

    fun setOnItemCLickListener(listener: BanksAdapter.OnItemClickListener) {
        this.listener = listener

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BankViewHolder {
        return BankViewHolder(
            BankItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return banks.size
    }

    override fun onBindViewHolder(holder: BankViewHolder, position: Int) {
        holder.bind(banks[position])
        if (selectedItem ==position){
            holder.view.bankImgContainer.background= ContextCompat.getDrawable(context!!,R.drawable.red_stroke_circle_bg)
        }else holder.view.bankImgContainer.background= ContextCompat.getDrawable(context!!,R.drawable.gray_circle_bg)
        holder.itemView.setOnClickListener {
            if (clickable)
           {
               if (listener != null) {
                   listener.onItemClick(banks[position])
               }
           }
            lastSelected = selectedItem

            selectedItem = position


            notifyItemChanged(lastSelected)


            notifyItemChanged(selectedItem)
        }




    }

    fun refreshActions(banks: List<Bank>) {
        this.banks = banks
        notifyDataSetChanged()
    }

    interface OnItemClickListener {
        fun onItemClick(bank: Bank)
    }
}