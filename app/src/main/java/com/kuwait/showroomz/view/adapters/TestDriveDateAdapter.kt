package com.kuwait.showroomz.view.adapters

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.TestDriveDateItemBinding
import com.kuwait.showroomz.extras.MyApplication.Key.context
import com.kuwait.showroomz.extras.Utils
import java.util.*

class TestDriveDateAdapter(var dates: List<Date>,@ColorInt private  var  color: Int) :
    RecyclerView.Adapter<TestDriveDateAdapter.TestDriveDatViewHolder>() {
    var selectedItem = 0
    var lastSelected = 0
    private lateinit var listener: TestDriveDateAdapter.OnItemClickListener
    class TestDriveDatViewHolder(var view: TestDriveDateItemBinding) : RecyclerView.ViewHolder(view.root) {
        fun bind(date: Date) {
            view.dayOfWeek.text= DateFormat.format("EEE", date) as String
            view.dayTxt.text= DateFormat.format("dd", date) as String
            view.monthTxt.text= DateFormat.format("MMM", date) as String
        }
    }
    fun setOnItemCLickListener(listener: TestDriveDateAdapter.OnItemClickListener){
        this.listener=listener

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestDriveDatViewHolder {
        return TestDriveDatViewHolder(
            TestDriveDateItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return dates.size
    }

    override fun onBindViewHolder(holder: TestDriveDatViewHolder, position: Int) {
        holder.bind(dates[position])
        if (selectedItem ==position){
            holder.itemView.setBackgroundColor(
               color
            )
            holder.view.monthTxt.setTextColor(ContextCompat.getColor(context, R.color.colorWhite))
            holder.view.dayOfWeek.setTextColor(ContextCompat.getColor(context, R.color.colorWhite))
            holder.view.dayTxt.setTextColor(ContextCompat.getColor(context, R.color.colorWhite))
        }else {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorWhite))
            holder.view.monthTxt.setTextColor(ContextCompat.getColor(context, R.color.colorBlack))
            holder.view.dayOfWeek.setTextColor(ContextCompat.getColor(context, R.color.colorBlack))
            holder.view.dayTxt.setTextColor(ContextCompat.getColor(context, R.color.colorBlack))
        }


        holder.itemView.setOnClickListener {

            if (listener!=null){
                listener.onItemClick(Utils.instance.addDaysToDate(dates[position], 1))
            }
            lastSelected = selectedItem
            selectedItem = position



            notifyItemChanged(lastSelected)


            notifyItemChanged(selectedItem)

        }
    }



    fun refreshDates(@ColorInt color: Int, dates: List<Date>) {
        this.color=color
     this.dates=dates
        notifyDataSetChanged()
    }

    interface OnItemClickListener{
        fun onItemClick(date: Date?)
    }
}