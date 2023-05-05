package com.kuwait.showroomz.view.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kuwait.showroomz.databinding.ApplyForFinanceBankItemBinding
import com.kuwait.showroomz.model.data.Bank
import com.kuwait.showroomz.model.simplifier.BankSimplifier
import com.kuwait.showroomz.viewModel.ApplyForFinanceStepThreeVM

class ApplyForFinanceBanksSelectAdapter(private var banks: List<Bank>, var context: Context?, val viewModel: ApplyForFinanceStepThreeVM) :
    RecyclerView.Adapter<ApplyForFinanceBanksSelectAdapter.BankViewHolder>() {
    private lateinit var listener: OnItemClickListener
    var selectedItem = -1
    var lastSelected = -1

    inner class BankViewHolder(var binding: ApplyForFinanceBankItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(bank: Bank?) {
            var isSelected = false

            viewModel.selectedBanks.forEach {
                if (bank?.id==it.id){
                    isSelected = true
                }
            }
            binding.bank = bank?.let { BankSimplifier(it) }

            binding.isSelected = isSelected
            binding.root.setOnClickListener {
                isSelected = !isSelected
                binding.isSelected = isSelected
                if (isSelected) {

                    if (bank != null) {
                        viewModel.selectedBanks.add(bank)
                    }
                } else{
                    var iterator =viewModel.selectedBanks.iterator()
                    iterator.forEach {
                        if (bank?.id==it.id){
                            iterator.remove()
                        }
                    }
                    var list = iterator.asSequence().toCollection(ArrayList())

                    viewModel.selectedBanks=list

                }
            }


        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BankViewHolder {
        return BankViewHolder(
            ApplyForFinanceBankItemBinding.inflate(
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