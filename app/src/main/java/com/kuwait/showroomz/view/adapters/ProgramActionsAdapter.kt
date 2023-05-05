package com.kuwait.showroomz.view.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kuwait.showroomz.databinding.ActionProgramItemBinding
import com.kuwait.showroomz.model.data.Action
import com.kuwait.showroomz.model.simplifier.ActionSimplifier

class ProgramActionsAdapter(private var actions: List<Action>) :
    RecyclerView.Adapter<ProgramActionsAdapter.ActionViewHolder>() {
    private lateinit var listener: OnItemClickListener

    class ActionViewHolder(var view: ActionProgramItemBinding) :
        RecyclerView.ViewHolder(view.root) {
        fun bind(action: Action?) {
            view.action = action?.let { ActionSimplifier(it) }
        }
    }

    fun setOnItemCLickListener(listener: OnItemClickListener) {
        this.listener = listener

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActionViewHolder {
        return ActionViewHolder(
            ActionProgramItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return actions.size
    }

    override fun onBindViewHolder(holder: ActionViewHolder, position: Int) {


        holder.bind(actions[position])

        holder.view.button.setOnClickListener {
            listener.onItemClick(actions[position])

        }
    }

    fun refreshActions(actions: List<Action>) {

        this.actions = actions
        notifyDataSetChanged()
    }

    interface OnItemClickListener {
        fun onItemClick(action: Action?)
    }
}