package com.kuwait.showroomz.view.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kuwait.showroomz.databinding.UserAddressItemBinding
import com.kuwait.showroomz.model.data.UserAddress

class UserAddressAdapter(private var list: List<UserAddress>) :
    RecyclerView.Adapter<UserAddressAdapter.AddressViewHolder>() {
    private lateinit var listener: OnItemClickListener

    class AddressViewHolder(var view: UserAddressItemBinding) : RecyclerView.ViewHolder(view.root) {
        fun bind(address: UserAddress?) {
            view.address = address
        }

    }

    fun setOnItemCLickListener(listener: OnItemClickListener) {
        this.listener = listener

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        return AddressViewHolder(
            UserAddressItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {

        holder.bind(list[position])
        holder.view.temBtn.setOnClickListener {
            if (listener != null) {
                listener.onItemClick(list[position])
            }

        }
    }

    fun refresh(address: List<UserAddress>) {
        this.list = address
        notifyDataSetChanged()
    }

    interface OnItemClickListener {
        fun onItemClick(address: UserAddress?)
    }
}