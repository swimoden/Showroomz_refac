package com.kuwait.showroomz.view.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kuwait.showroomz.databinding.AddonItemBinding
import com.kuwait.showroomz.model.data.Addon
import com.kuwait.showroomz.model.simplifier.AddonSimplifier
import com.kuwait.showroomz.viewModel.BookRentVM


class AddonsAdapter(
    private var addons: List<Addon>,
    var context: Context?,
    var viewModel: BookRentVM
) :

    RecyclerView.Adapter<AddonsAdapter.AddonViewHolder>() {
    var index = -1
    var selectedItem = -1
    var lastSelected = -1
    var clickable: Boolean = true


    class AddonViewHolder(var view: AddonItemBinding) : RecyclerView.ViewHolder(view.root) {
        fun bind(addon: Addon?) {
            view.addon = addon?.let { AddonSimplifier(it) }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddonViewHolder {
        return AddonViewHolder(
            AddonItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return addons.size
    }

    override fun onBindViewHolder(holder: AddonViewHolder, position: Int) {
        holder.bind(addons[position])
        holder.view.addonSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                viewModel.listAddons.add(addons[position])
                viewModel.calculateAmount()
            } else {
                viewModel.listAddons.remove(addons[position])
                viewModel.calculateAmount()
            }
        }


    }

    fun refreshActions(addons: List<Addon>) {
        this.addons = addons
        notifyDataSetChanged()
    }


}