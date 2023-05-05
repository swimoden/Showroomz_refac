package com.kuwait.showroomz.view.adapters

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.TestDriveTimeItemBinding
import com.kuwait.showroomz.extras.DesignUtils
import com.kuwait.showroomz.extras.MyApplication.Key.context
import com.kuwait.showroomz.extras.Utils
import com.kuwait.showroomz.extras.isEnglish
import com.kuwait.showroomz.model.data.PreferredTimeTestDrive
import com.kuwait.showroomz.model.simplifier.PreferredTimeSimplifier

class TestDriveTimeAdapter(@ColorInt private  var  color: Int,var times: ArrayList<String>, var list: ArrayList<PreferredTimeTestDrive>) :
    RecyclerView.Adapter<TestDriveTimeAdapter.TestDriveTimeViewHolder>() {
    var selectedItem = -1
    var lastSelected = -1
    private lateinit var listener: TestDriveTimeAdapter.OnItemClickListener

    class TestDriveTimeViewHolder(var view: TestDriveTimeItemBinding) :
        RecyclerView.ViewHolder(view.root) {
        fun bind(time: String) {
            view.time = if (!isEnglish) Utils.instance.convertToArabic(time) else time
        }
    }

    fun setOnItemCLickListener(listener: TestDriveTimeAdapter.OnItemClickListener) {
        this.listener = listener

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestDriveTimeViewHolder {
        return TestDriveTimeViewHolder(
            TestDriveTimeItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return times.size
    }

    override fun onBindViewHolder(holder: TestDriveTimeViewHolder, position: Int) {
        holder.bind(times[position])

            list.forEach {
                var x = times[position].replace(" p.m", "").replace(" a.m", "")
                    .replace(" صباحا","").replace(" عصرا","")

                if (x.contains(":")) {
                    val container = x.split(":")
                    val x1 = container[0].toInt()
                    val x2 = container[1]
                    if (x1 > 12) {
                        val f = x1 - 12
                        x = "$f:$x2"
                    }
                }


                if (x == PreferredTimeSimplifier(it).time){
                    holder.itemView.background = ContextCompat.getDrawable(
                        context,
                        R.drawable.filled_dark_gray
                    )
                    holder.view.timeTxt.setTextColor(ContextCompat.getColor(context, R.color.colorWhite))
                    holder.itemView.isClickable=false
                    return
                }



        }
        holder.itemView.isClickable=true
        if (selectedItem == position) {
            holder.itemView.background = DesignUtils.instance.createDrawable(color,GradientDrawable.RECTANGLE)

            holder.view.timeTxt.setTextColor(ContextCompat.getColor(context, R.color.colorWhite))

        } else {
            holder.itemView.background = ContextCompat.getDrawable(
                context,
                R.drawable.round_background_light_gray
            )
            holder.view.timeTxt.setTextColor(ContextCompat.getColor(context, R.color.colorBlack))

        }

        holder.itemView.setOnClickListener {

            if (listener != null) {
                listener.onItemClick(times[position])
            }
            lastSelected = selectedItem
            selectedItem = position



            notifyItemChanged(lastSelected)


            notifyItemChanged(selectedItem)

        }
    }
   fun refreshPreferredTimeTestDrive(list:ArrayList<PreferredTimeTestDrive>){
       this.list.clear()
       this.list=list
       notifyDataSetChanged()
   }
    fun refreshDates(color: Int,dates: ArrayList<String>) {
        this.color=color
        selectedItem=-1
        lastSelected=-1
        this.times.clear()
        this.times = dates
        notifyDataSetChanged()
    }

    interface OnItemClickListener {
        fun onItemClick(time: String?)
    }
}