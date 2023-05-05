package com.kuwait.showroomz.view.adapters

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.ActionsGridItemBinding
import com.kuwait.showroomz.extras.DesignUtils
import com.kuwait.showroomz.model.data.Action
import com.kuwait.showroomz.model.simplifier.ActionSimplifier

class ActionsAdapter(private var actions: List<Action>,val source:Int = 0, @ColorInt private var  color:Int) :
    RecyclerView.Adapter<ActionsAdapter.ActionViewHolder>() {
    private lateinit var listener: ActionsAdapter.OnItemClickListener
    class ActionViewHolder(var view: ActionsGridItemBinding) : RecyclerView.ViewHolder(view.root) {
        fun bind(action: Action?) {
            view.action = action?.let { ActionSimplifier(it) }
        }
    }
    fun setOnItemCLickListener(listener: ActionsAdapter.OnItemClickListener){
        this.listener=listener

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActionViewHolder {
        return ActionViewHolder(
            ActionsGridItemBinding.inflate(
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
        holder.view.hide  =  (position + 1).rem(3) == 0||position+1==actions.size

        holder.bind(actions[position])
        holder.view.actionIconContainer.background = DesignUtils.instance.createDrawableWithStrock(Color.BLACK,color,
            GradientDrawable.OVAL)
        holder.itemView.setOnClickListener {
            if (listener!=null){
                listener.onItemClick(actions[position])
            }
        }
        if (source > 0)
            holder.view.actionTxt.setTextColor(holder.itemView.context.resources.getColor(R.color.colorBlack))
    }

    fun refreshActions(actions: List<Action>, bgColor: Int) {
        this.color=bgColor
     this.actions=actions
        notifyDataSetChanged()
    }

    interface OnItemClickListener{
        fun onItemClick(action: Action?)
    }
}