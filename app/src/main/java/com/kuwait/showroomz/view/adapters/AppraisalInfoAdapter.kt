package com.kuwait.showroomz.view.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kuwait.showroomz.databinding.FilterBrandItemBinding
import com.kuwait.showroomz.databinding.InfoItemBinding
import com.kuwait.showroomz.extras.isEnglish
import com.kuwait.showroomz.model.data.AppraisalInfo
import com.kuwait.showroomz.model.data.Brand
import com.kuwait.showroomz.model.simplifier.BrandSimplifier

class AppraisalInfoAdapter(private var infos: List<AppraisalInfo>) :
RecyclerView.Adapter<AppraisalInfoAdapter.AppraisalInfoViewHolder>() {
    private lateinit var listener: OnItemClickListener
    inner class AppraisalInfoViewHolder(var binding: InfoItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(name: String) {
            binding.name = name
        }
    }
    interface OnItemClickListener {
        fun onItemClick(info: AppraisalInfo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppraisalInfoViewHolder {
        return AppraisalInfoViewHolder(
            InfoItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        )
    }
    fun setOnItemCLickListener(listener: OnItemClickListener) {
        this.listener = listener

    }
    override fun onBindViewHolder(holder: AppraisalInfoViewHolder, position: Int) {
        holder.itemView.setOnClickListener {
            if (::listener.isInitialized) {
                listener.onItemClick(infos[position])
            }
        }
val item = infos[position]
        val lang = if (isEnglish) item.translations?.en else item.translations?.ar
        val name = lang?.let {
            it.name
        } ?: run {
            ""
        }
        holder.bind(name)
    }

    override fun getItemCount(): Int = infos.size
    fun refreshData(brands: List<AppraisalInfo>?) {
        if (brands != null) {
            this.infos = brands
            notifyDataSetChanged()
        }
    }
}