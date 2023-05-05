package com.kuwait.showroomz.view.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.BankItemBinding
import com.kuwait.showroomz.databinding.OfferBankItemBinding
import com.kuwait.showroomz.model.data.Bank
import com.kuwait.showroomz.model.simplifier.BankSimplifier

class OffersBanksAdapter(private var banks: List<Bank>, var context: Context?) :
    RecyclerView.Adapter<OffersBanksAdapter.BankViewHolder>() {
    var index=-1

    class BankViewHolder(var view: OfferBankItemBinding) : RecyclerView.ViewHolder(view.root) {
        fun bind(bank: Bank?) {
            view.bank = bank?.let { BankSimplifier(it) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BankViewHolder {
        return BankViewHolder(
            OfferBankItemBinding.inflate(
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
    }
    fun refreshActions(banks: List<Bank>) {
     this.banks=banks
        notifyDataSetChanged()
    }


}