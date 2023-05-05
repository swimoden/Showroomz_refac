package com.kuwait.showroomz.view.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.kuwait.showroomz.R
import com.kuwait.showroomz.model.data.Callback
import com.kuwait.showroomz.model.simplifier.CallbackSimplifier
import com.kuwait.showroomz.view.fragment.DashboardFragmentDirections

class DashboardFinanceCallbackAdapter(private var list: List<Callback>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    class CallbackViewHolder(var view: com.kuwait.showroomz.databinding.DashboardCallbackItemBinding) :
        RecyclerView.ViewHolder(view.root)

    class CallbackEmptyModelViewHolder(var view: com.kuwait.showroomz.databinding.DashboaedCallbackItemEmptyBinding) :
        RecyclerView.ViewHolder(view.root)

    fun refresh(callbacks: List<Callback>) {
        this.list = callbacks
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        if (viewType == 0) {
            val view =
                DataBindingUtil.inflate<com.kuwait.showroomz.databinding.DashboardCallbackItemBinding>(
                    inflater,
                    R.layout.dashboard_callback_item,
                    parent,
                    false
                )
            return CallbackViewHolder(view)
        } else {
            val view =
                DataBindingUtil.inflate<com.kuwait.showroomz.databinding.DashboaedCallbackItemEmptyBinding>(
                    inflater,
                    R.layout.dashboaed_callback_item_empty,
                    parent,
                    false
                )
            return CallbackEmptyModelViewHolder(view)
        }

    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val callback = list[position]
        if (holder is CallbackViewHolder)
            holder.view.callback = CallbackSimplifier(callback)
        if (holder is CallbackEmptyModelViewHolder)
            holder.view.callback = CallbackSimplifier(callback)
        holder.itemView.setOnClickListener {
            Navigation.findNavController(it).navigate(DashboardFragmentDirections.showFinanceCallbackItem(callback))
        }

    }

    override fun getItemViewType(position: Int): Int {
        return if (list[position].modelData?.id != null) 0 else 1
    }
}