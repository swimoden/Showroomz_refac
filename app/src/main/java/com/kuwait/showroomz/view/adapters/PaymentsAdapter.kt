package com.kuwait.showroomz.view.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.kuwait.showroomz.R
import com.kuwait.showroomz.model.data.Booking
import com.kuwait.showroomz.model.simplifier.BookingSimplifier
import com.kuwait.showroomz.view.fragment.PaymentListFragmentDirections

class PaymentsAdapter(private var list: List<Booking>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    class CallbackViewHolder(var view: com.kuwait.showroomz.databinding.PaymentListItemBinding) :
        RecyclerView.ViewHolder(view.root)


    fun refresh(bookings: List<Booking>) {
        this.list = bookings
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        val view =
            DataBindingUtil.inflate<com.kuwait.showroomz.databinding.PaymentListItemBinding>(
                inflater,
                R.layout.payment_list_item,
                parent,
                false
            )
        return CallbackViewHolder(view)


    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val booking = list[position]
        if (holder is CallbackViewHolder) {
            holder.view.booking = BookingSimplifier(booking)
            holder.itemView
                .setOnClickListener {
                    when (booking.discr) {
                        "Buy" -> {
                            Navigation.findNavController(it).navigate(
                                PaymentListFragmentDirections.showPaymentItemFragment(booking)
                            )
                        }
                        "Booking" -> {
                            Navigation.findNavController(it).navigate(
                                PaymentListFragmentDirections.showBookNowItemFragment(booking)
                            )
                        }
                        "Rent" -> {
                            Navigation.findNavController(it).navigate(
                                PaymentListFragmentDirections.showBookingRentItemFragment(booking)
                            )
                        }
                    }
                }
        }


    }


}