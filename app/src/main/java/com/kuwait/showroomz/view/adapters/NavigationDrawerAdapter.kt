package com.kuwait.showroomz.view.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.NavigationViewItemLayoutBinding
import com.kuwait.showroomz.extras.MyApplication
import com.kuwait.showroomz.extras.Shared


import com.kuwait.showroomz.extras.isEnglish
import com.kuwait.showroomz.view.MainActivity
import com.kuwait.showroomz.view.fragment.DashboardFragment

class NavigationDrawerAdapter(
    private val context: Context?,
    val fragment: DashboardFragment,
    private val itemsList: List<DrawerItem>
) :
    RecyclerView.Adapter<NavigationDrawerAdapter.NavigationDrawerViewHolder>() {
    var selectedItem = -1

    private var lastSelected = -1
    private lateinit var listener: NavigationDrawerAdapter.OnItemClickListener

    class NavigationDrawerViewHolder(val view: NavigationViewItemLayoutBinding) :
        RecyclerView.ViewHolder(view.root) {

        fun bind(
            context: Context,
            fragment: DashboardFragment,
            item: DrawerItem
        ) {
            if (item.id == R.id.languageFragment) {
                view.arabic.isVisible = true
                view.english.isVisible = true
                if (isEnglish) view.english.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.colorPrimary
                    )
                ) else view.arabic.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.colorPrimary
                    )
                )
                view.english.setOnClickListener {
                    if (!isEnglish) {
                        val  shared =Shared(context)
                        shared.setBool(MyApplication.LANGUAGE_SELECTED,true)
                        isEnglish=true
                        shared.setString(MyApplication.LANG,"EN")
                        (context as MainActivity).setLocale("en")
                        view.english.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.colorPrimary
                            ))
                        view.arabic.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.colorWhite
                            ))
                        fragment.reloadFragment()
                    }

                }
                view.arabic.setOnClickListener {
                    if (isEnglish) {
                        val  shared =Shared(context)
                        shared.setBool(MyApplication.LANGUAGE_SELECTED,true)
                        isEnglish=false
                        shared.setString(MyApplication.LANG,"AR")
                        (context as MainActivity).setLocale("ar")
                        view.arabic.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.colorPrimary
                            ))
                        view.english.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.colorWhite
                            ))
                        fragment.reloadFragment()
                    }


                }
            } else {
                view.arabic.isVisible = false
                view.english.isVisible = false
            }
            view.itemTitle.text = item.title
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NavigationDrawerViewHolder {
        return NavigationDrawerViewHolder(
            NavigationViewItemLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    fun setOnItemCLickListener(listener: OnItemClickListener) {
        this.listener = listener

    }

    override fun getItemCount(): Int = itemsList.size

    override fun onBindViewHolder(holder: NavigationDrawerViewHolder, position: Int) {
        holder.bind(context!!,fragment,itemsList[position])
        holder.view.indicator.visibility = if (position == selectedItem) View.VISIBLE else View.GONE
        holder.itemView.setOnClickListener {
            if (listener != null) {
                listener.onItemClick(itemsList[position])
            }
            lastSelected = selectedItem

            selectedItem = position


            notifyItemChanged(lastSelected)


            notifyItemChanged(selectedItem)

        }
    }

    interface OnItemClickListener {
        fun onItemClick(item: DrawerItem)
    }
}

data class DrawerItem(
    @IdRes var id: Int = -1,
    var title: String = ""
)