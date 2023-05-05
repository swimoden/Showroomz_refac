package com.kuwait.showroomz.view.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kuwait.showroomz.databinding.AppraisalItemDetailsListBinding
import com.kuwait.showroomz.model.data.AppraisalRequest
import com.kuwait.showroomz.model.simplifier.RequestSimplifier
import com.kuwait.showroomz.viewModel.AppraisalItemDetailsVM


class AppraisalRequestsAdapter(
    private var requests: List<AppraisalRequest>,

    var viewModel: AppraisalItemDetailsVM
) :

    RecyclerView.Adapter<AppraisalRequestsAdapter.AddonViewHolder>() {
    private lateinit var listener: OnItemClickListener
    var index = -1

    var clickable: Boolean = true

    fun setOnItemCLickListener(listener: OnItemClickListener){
        this.listener=listener

    }
    class AddonViewHolder(var view: AppraisalItemDetailsListBinding) :
        RecyclerView.ViewHolder(view.root) {
        fun bind(request: AppraisalRequest) {
            view.request = RequestSimplifier(request)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddonViewHolder {
        return AddonViewHolder(
            AppraisalItemDetailsListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return requests.size
    }

    override fun onBindViewHolder(holder: AddonViewHolder, position: Int) {
        val item = requests[position]
        holder.bind(item)

        holder.view.checkbox.isChecked = item.isSelected
        /*if (selectedItem == position) {
            holder.view.checkbox.isChecked = true
            viewModel.request = requests[position]
        } else holder.view.checkbox.isChecked = true*/

        holder.itemView.setOnClickListener {

            if (listener!=null){
                listener.onItemClick(requests[position])
            }

            /*lastSelected = selectedItem;
            selectedItem = position;
            notifyItemChanged(lastSelected);
            notifyItemChanged(selectedItem);*/
        }

    }

    fun refreshActions(requests: List<AppraisalRequest>) {
        this.requests = requests
        notifyDataSetChanged()
    }

    interface OnItemClickListener{
        fun onItemClick(request: AppraisalRequest?)
    }


}