package com.kuwait.showroomz.view.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.AppraisalVehicleItemBinding
import com.kuwait.showroomz.extras.MyApplication.Key.context
import com.kuwait.showroomz.model.data.Bank
import com.kuwait.showroomz.model.data.CallbackAppraisalClientVehicle
import com.kuwait.showroomz.model.data.ClientVehicle
import com.kuwait.showroomz.model.simplifier.CallbackAppraisalClientVehicleSimplifier
import com.kuwait.showroomz.model.simplifier.ClientVehicleSimplifier
import io.realm.RealmList

class CarAppraisalAdapter(private var list: List<ClientVehicle>) :   RecyclerView.Adapter<CarAppraisalAdapter.ViewHolder>() {
    private lateinit var listener: OnItemClickListener
    var index = -1
    var selectedItem = -1
    var lastSelected = -1
    fun setOnItemCLickListener(listener: OnItemClickListener) {
        this.listener = listener

    }
    interface OnItemClickListener {
        fun onItemClick(car: ClientVehicleSimplifier)
    }
  inner  class ViewHolder(var view: AppraisalVehicleItemBinding) :
        RecyclerView.ViewHolder(view.root) {


        fun bind(callback: ClientVehicle) {
            view.clientVehicle = ClientVehicleSimplifier(callback)


        }
    }


    fun refresh(callbacks: List<ClientVehicle>) {
        this.list = callbacks
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        val view =
            DataBindingUtil.inflate<AppraisalVehicleItemBinding>(
                inflater,
                R.layout.appraisal_vehicle_item,
                parent,
                false
            )
        return ViewHolder(view)


    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
        if (selectedItem ==position){
            holder.view.image.background= ContextCompat.getDrawable(context,R.drawable.red_stroke_rectangle_bg)
        }else holder.view.image.background= ContextCompat.getDrawable(context,R.drawable.gray_stroke_rectangle_bg)
        holder.itemView.setOnClickListener {
            ClientVehicleSimplifier(list[position]).let { it1 ->
                listener.onItemClick(
                    it1
                )
            }
            lastSelected = selectedItem

            selectedItem = position


            notifyItemChanged(lastSelected)


            notifyItemChanged(selectedItem)
        }
    }

}