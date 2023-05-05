package com.kuwait.showroomz.view.adapters

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.AddonAmountItemBinding
import com.kuwait.showroomz.databinding.OfferDetailItemBinding
import com.kuwait.showroomz.databinding.OfferDetailsItemBinding
import com.kuwait.showroomz.model.data.Addon
import com.kuwait.showroomz.model.data.Color
import com.kuwait.showroomz.model.data.OfferDetail
import com.kuwait.showroomz.model.data.UserCarDetails
import com.kuwait.showroomz.model.simplifier.AddonSimplifier
import com.kuwait.showroomz.model.simplifier.AddonsResult

class AddonsAmountAdapter(
    private var addonsResult: List<AddonsResult>,
    var context: Context?

) :

    RecyclerView.Adapter<AddonsAmountAdapter.AddonViewHolder>() {


    class AddonViewHolder(var view: AddonAmountItemBinding) : RecyclerView.ViewHolder(view.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddonViewHolder {
        return AddonViewHolder(
            AddonAmountItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return addonsResult.size
    }

    override fun onBindViewHolder(holder: AddonViewHolder, position: Int) {
        holder.view.value.text = addonsResult[position].result
        holder.view.name.text = addonsResult[position].name


    }

    fun refreshActions(addonsResult: List<AddonsResult>) {
        this.addonsResult = addonsResult

        notifyDataSetChanged()
    }


}

class USerCarDetailsAdapter(
    private var userCarsDetails: ArrayList<UserCarDetails>,
    var context: Context?

) :

    RecyclerView.Adapter<USerCarDetailsAdapter.AddonViewHolder>() {


    class AddonViewHolder(var view: AddonAmountItemBinding) : RecyclerView.ViewHolder(view.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddonViewHolder {
        return AddonViewHolder(
            AddonAmountItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return userCarsDetails.size
    }

    override fun onBindViewHolder(holder: AddonViewHolder, position: Int) {
        holder.view.value.text = userCarsDetails.get(position).value
        holder.view.name.text = userCarsDetails.get(position).key


    }

    fun refresh(userCarsDetails: ArrayList<UserCarDetails>) {
        this.userCarsDetails = userCarsDetails
        notifyDataSetChanged()
    }


}

class OfferDetailsAdapter(private var offersDetails: ArrayList<OfferDetail>) :
    RecyclerView.Adapter<OfferDetailsAdapter.OfferDetailsHolder>() {

    class OfferDetailsHolder(var view: OfferDetailItemBinding) : RecyclerView.ViewHolder(view.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfferDetailsHolder {
        return OfferDetailsHolder(
            OfferDetailItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return offersDetails.size
    }

    override fun onBindViewHolder(holder: OfferDetailsHolder, position: Int) {
val offer = offersDetails[position]
        holder.view.data = offer
        when (offer.type) {
            0 -> {
                holder.view.txtText.setTextColor(ContextCompat.getColor(holder.view.container.context, R.color.colorPrimary))
                holder.view.amountText.setTextColor(ContextCompat.getColor(holder.view.container.context, R.color.colorPrimary))
                holder.view.txtText.textSize = 17f
                holder.view.txtText.setTypeface(holder.view.txtText.typeface, Typeface.BOLD)
                holder.view.amountText.textSize = 17f
                holder.view.amountText.setTypeface(holder.view.txtText.typeface, Typeface.BOLD)
            }
            2 -> {
                holder.view.txtText.setTextColor(ContextCompat.getColor(holder.view.container.context, R.color.com_facebook_messenger_blue))
                holder.view.txtText.textSize = 17f
                holder.view.txtText.setTypeface(holder.view.txtText.typeface, Typeface.BOLD)
            }
        }


    }

    fun refresh(offersDetails: ArrayList<OfferDetail>) {
        this.offersDetails = offersDetails
        notifyDataSetChanged()
    }


}