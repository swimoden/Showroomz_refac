package com.kuwait.showroomz.view.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.AppraisalListItemBinding
import com.kuwait.showroomz.model.data.CallbackAppraisalClientVehicle
import com.kuwait.showroomz.model.data.TestDrive
import com.kuwait.showroomz.model.simplifier.CallbackAppraisalClientVehicleSimplifier
import com.kuwait.showroomz.model.simplifier.TestDriveSimplifier
import com.kuwait.showroomz.view.fragment.AppraisalRequestsListFragmentDirections
import com.kuwait.showroomz.view.fragment.TestDriveListFragmentDirections

class AppraisalRequestsListAdapter(private var list: List<CallbackAppraisalClientVehicle>) :
    RecyclerView.Adapter<AppraisalRequestsListAdapter.ViewHolder>() {
    class ViewHolder(var view: AppraisalListItemBinding) :
        RecyclerView.ViewHolder(view.root) {
        fun bind(callback: CallbackAppraisalClientVehicle) {
            view.callback = CallbackAppraisalClientVehicleSimplifier(callback)
        }
    }


    fun refresh(callbacks: List<CallbackAppraisalClientVehicle>) {
        this.list = callbacks
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        val view =
            DataBindingUtil.inflate<AppraisalListItemBinding>(
                inflater,
                R.layout.appraisal_list_item,
                parent,
                false
            )
        return ViewHolder(view)


    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
        holder.itemView.setOnClickListener {
            Navigation.findNavController(it).navigate(AppraisalRequestsListFragmentDirections.
            showItem(CallbackAppraisalClientVehicleSimplifier(list[position]), null))
        }
    }


}