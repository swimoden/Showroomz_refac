package com.kuwait.showroomz.view.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.TrimProgramItemBinding
import com.kuwait.showroomz.model.data.Program
import com.kuwait.showroomz.model.simplifier.TrimProgramSimplifier
import io.realm.RealmList

class TrimProgramAdapter(
    private val programs: List<Program>,
    private val context: Context
) :
    RecyclerView.Adapter<TrimProgramAdapter.ProgramViewHolder>() {
    private lateinit var listener: OnItemClickListener
    private var index: Int = -1
    fun setOnItemCLickListener(listener: OnItemClickListener) {
        this.listener = listener

    }
    inner class ProgramViewHolder(var view: TrimProgramItemBinding) :
        RecyclerView.ViewHolder(view.root) {
        fun bind(program: Program?) {
            var simplifier = program?.let { TrimProgramSimplifier(it) }

            view.program = simplifier
            when (simplifier?.contractPeriod) {
                0 -> {
                    view.type.text = ""
                }
                1 -> {
                    view.type.text = context.resources.getString(R.string.daily)
                    view.textContract.text = context.resources.getString(R.string.daily)
                }
                7 -> {
                    view.type.text = context.resources.getString(R.string.weekly)
                    view.textContract.text = context.resources.getString(R.string.weekly)
                }
                30,31 -> {
                    view.type.text = context.resources.getString(R.string.monthly)
                    view.textContract.text = context.resources.getString(R.string.monthly)
                }
                365 -> {
                    view.type.text = context.resources.getString(R.string.yearly)
                    view.textContract.text = context.resources.getString(R.string.yearly)
                }
                else -> {
                    run { view.type.text = context.resources.getString(R.string.monthly)
                        view.textContract.text = simplifier?.contractPeriod.toString()+" "+ context.resources.getString(R.string.month_contract)}
                }

            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgramViewHolder {
        return ProgramViewHolder(
            TrimProgramItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        )

    }

    override fun getItemCount(): Int {
        return programs.size
    }

    override fun onBindViewHolder(holder: ProgramViewHolder, position: Int) {

        holder.bind(programs[position])
        holder.itemView.setOnClickListener {
            index = position
            notifyDataSetChanged()
            if (listener != null) {
                programs[position].let { it1 -> listener.onItemClick(it1) }
            }
        }
        if (index == position) {
            holder.view.checkbox.isChecked = true
            holder.view.priceContainer.background =
                ContextCompat.getDrawable(context, R.drawable.rectengle_red)
            holder.view.price.setTextColor(ContextCompat.getColor(context, R.color.colorWhite))
            holder.view.type.setTextColor(ContextCompat.getColor(context, R.color.colorWhite))

        } else {
            holder.view.checkbox.isChecked = false
            holder.view.priceContainer.background =
                ContextCompat.getDrawable(context, R.drawable.rectangle_stroke_black)
            holder.view.price.setTextColor(ContextCompat.getColor(context, R.color.colorBlack))
            holder.view.type.setTextColor(ContextCompat.getColor(context, R.color.colorBlack))

        }

    }
    interface OnItemClickListener {
        fun onItemClick(program: Program)
    }
}