package com.kuwait.showroomz.view.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kuwait.showroomz.databinding.AddonItemBinding
import com.kuwait.showroomz.databinding.OptionItemBinding
import com.kuwait.showroomz.model.data.Addon
import com.kuwait.showroomz.model.data.CarOption
import com.kuwait.showroomz.model.simplifier.OptionSimplifier
import com.kuwait.showroomz.viewModel.CarDetailsVM
import io.realm.RealmList


class OptionsAdapter(
    private var options: List<CarOption>,
    var context: Context?,
    var viewModel: CarDetailsVM
) :

    RecyclerView.Adapter<OptionsAdapter.OptionViewHolder>() {
    var index = -1
    var selectedItem = -1
    var lastSelected = -1
    var clickable: Boolean = true


    class OptionViewHolder(var view: OptionItemBinding) : RecyclerView.ViewHolder(view.root) {
        fun bind(option: CarOption?) {
            view.option = option?.let { OptionSimplifier(it) }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionViewHolder {
        return OptionViewHolder(
            OptionItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return options.size
    }

    override fun onBindViewHolder(holder: OptionViewHolder, position: Int) {
        holder.bind(options[position])
        if (viewModel.selectedOptions?.contains(options[position]) == true) holder.view.addonSwitch.isChecked=true
        holder.view.addonSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                viewModel.selectedOptions?.add(options[position])

            } else {
                viewModel.selectedOptions?.remove(options[position])

            }
        }


    }

    fun refreshActions(options: List<CarOption>) {
        this.options = options
        notifyDataSetChanged()
    }




}