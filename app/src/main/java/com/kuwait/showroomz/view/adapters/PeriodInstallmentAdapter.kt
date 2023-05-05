package com.kuwait.showroomz.view.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.InstallementItemBinding

class PeriodInstallmentAdapter(private var ratios: List<Double>, var context: Context?) :
    RecyclerView.Adapter<PeriodInstallmentAdapter.RatioVIewHolder>() {
    var index = 0
    private lateinit var listener: PeriodInstallmentAdapter.OnItemClickListener

    class RatioVIewHolder(var view: InstallementItemBinding) : RecyclerView.ViewHolder(view.root)

    fun setOnItemCLickListener(listener: PeriodInstallmentAdapter.OnItemClickListener) {
        this.listener = listener

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RatioVIewHolder {
        return RatioVIewHolder(
            InstallementItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return ratios.size
    }

    override fun onBindViewHolder(holder: RatioVIewHolder, position: Int) {
        if (position == 0) holder.view.yearText.text =
            "" + (position + 1) + " " + context?.resources?.getString(R.string.year)
        else holder.view.yearText.text =
            "" + (position + 1) + " " + context?.resources?.getString(R.string.years)
        holder.itemView.setOnClickListener {
            if (listener != null) {
                listener.onItemClick(ratios[position])
            }
            index = position
            notifyDataSetChanged()
        }



        if (index == position) {
            holder.view.container.background =
                ContextCompat.getDrawable(context!!, R.drawable.round_background_black)
            holder.view.yearText.setTextColor(ContextCompat.getColor(context!!, R.color.colorWhite))
        } else {
            holder.view.container.background =
                ContextCompat.getDrawable(context!!, R.drawable.round_background_light_gray)
            holder.view.yearText.setTextColor(ContextCompat.getColor(context!!, R.color.colorBlack))

        }
    }

    fun refreshActions(ratios: List<Double>) {
        /*val auxRatio = arrayListOf<Double>()
        auxRatio.addAll(ratios)
        if (ratios.size < 5){
            for (i in auxRatio.size ..4){
                auxRatio.add(ratios.last())
            }
        }*/
        this.ratios = ratios
        notifyDataSetChanged()
    }

    interface OnItemClickListener {
        fun onItemClick(double: Double)
    }
}