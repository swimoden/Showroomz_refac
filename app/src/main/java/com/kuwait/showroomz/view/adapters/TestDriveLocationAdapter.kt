package com.kuwait.showroomz.view.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.TestDriveShowroomItemBinding
import com.kuwait.showroomz.databinding.TrimProgramItemBinding
import com.kuwait.showroomz.model.data.Action
import com.kuwait.showroomz.model.data.Location
import com.kuwait.showroomz.model.data.Program
import com.kuwait.showroomz.model.simplifier.LocationSimplifier
import com.kuwait.showroomz.model.simplifier.TrimProgramSimplifier
import io.realm.RealmList

class TestDriveLocationAdapter(
     var locations: List<Location>
) :
    RecyclerView.Adapter<TestDriveLocationAdapter.ViewHolder>() {
    private lateinit var listener:OnItemClickListener
    private var index: Int = -1

     class ViewHolder(var view: TestDriveShowroomItemBinding) :
        RecyclerView.ViewHolder(view.root) {
        fun bind(location: Location?) {
            var simplifier = location?.let { LocationSimplifier(it) }

            view.location = simplifier


        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            TestDriveShowroomItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        )

    }

    override fun getItemCount(): Int {
        return locations.size
    }
    fun setOnItemCLickListener(listener: OnItemClickListener){
        this.listener=listener

    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.bind(locations[position])

        holder.view.checkbox.isChecked = index == position
        holder.itemView.setOnClickListener {
            if (listener!=null){
                listener.onItemClick(locations[position])
            }
            index = position
                    notifyDataSetChanged()
        }


    }
    fun refresh(list: List<Location>){
        this.locations=list
        notifyDataSetChanged()
    }
    interface OnItemClickListener{
        fun onItemClick(localtion: Location?)
    }
}