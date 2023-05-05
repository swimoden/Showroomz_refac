package com.kuwait.showroomz.view.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kuwait.showroomz.databinding.ApplyForFinanceBankBottomSheetBankItemBinding
import com.kuwait.showroomz.model.data.Bank
import com.kuwait.showroomz.model.simplifier.BankSimplifier

class ApplyForFinanceBanksAdapter(private var banks: List<Bank>, var context: Context?) :
    RecyclerView.Adapter<ApplyForFinanceBanksAdapter.BankViewHolder>() {
    private lateinit var listener: ApplyForFinanceBanksAdapter.OnItemClickListener
    var selectedItem = -1
    var lastSelected = -1

    class BankViewHolder(var view: ApplyForFinanceBankBottomSheetBankItemBinding) :
        RecyclerView.ViewHolder(view.root) {
        fun bind(bank: Bank?) {
            view.bank = bank?.let { BankSimplifier(it) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BankViewHolder {
        return BankViewHolder(
            ApplyForFinanceBankBottomSheetBankItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }
    fun setOnItemCLickListener(listener: OnItemClickListener) {
        this.listener = listener

    }
    override fun onBindViewHolder(holder: BankViewHolder, position: Int) {
        holder.bind(banks[position])
        holder.view.isSelected = selectedItem == position
        holder.itemView.setOnClickListener {
            if (listener != null) {
                listener.onItemClick(banks[position])
            }
            lastSelected = selectedItem

            selectedItem = position


            notifyItemChanged(lastSelected)


            notifyItemChanged(selectedItem)

        }

    }

    override fun getItemCount(): Int {
        return banks.size
    }
    fun refreshActions(banks: List<Bank>) {
        this.banks = banks
        notifyDataSetChanged()
    }

    interface OnItemClickListener {
        fun onItemClick(bank: Bank)
    }
}