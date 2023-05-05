package com.kuwait.showroomz.view.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.kuwait.showroomz.R
import com.kuwait.showroomz.model.data.Callback
import com.kuwait.showroomz.model.simplifier.CallbackSimplifier
import com.kuwait.showroomz.view.fragment.FinanceRequestFragmentDirections

class FinanceRequestAdapter(private var list: List<Callback>, var context: Context?) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    class CallbackViewHolder(var view: com.kuwait.showroomz.databinding.FinanceRequestItemBinding) :
        RecyclerView.ViewHolder(view.root)


    fun refresh(callbacks: List<Callback>) {
        this.list = callbacks
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        val view =
            DataBindingUtil.inflate<com.kuwait.showroomz.databinding.FinanceRequestItemBinding>(
                inflater,
                R.layout.finance_request_item,
                parent,
                false
            )
        return CallbackViewHolder(view)


    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val callback = list[position]
        if (holder is CallbackViewHolder) {
            holder.view.callback = CallbackSimplifier(callback)
            holder.view.modelCategory.paint.isUnderlineText=true
            when (callback.processStatus) {
                10 -> {
                    holder.view.status.text = context?.getString(R.string.pending)
                    holder.view.status.background =
                        ContextCompat.getDrawable(context!!, R.drawable.filled_yellow)

                }
                30 -> {
                    holder.view.status.text = context?.getString(R.string.rejected)
                    holder.view.status.background =
                        ContextCompat.getDrawable(context!!, R.drawable.filled_red)

                }
                20-> {
                    holder.view.status.text = context?.getString(R.string.approved)
                    holder.view.status.background =
                        ContextCompat.getDrawable(context!!, R.drawable.filled_green)

                }
            }

        }


        holder.itemView.setOnClickListener {

            Navigation.findNavController(it)
                .navigate(FinanceRequestFragmentDirections.showFinanceRequestItemDetail(null, callback))
        }

    }


}