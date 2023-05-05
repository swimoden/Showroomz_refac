package com.kuwait.showroomz.view.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.*
import com.kuwait.showroomz.model.data.Offer
import com.kuwait.showroomz.model.simplifier.ModelSimplifier
import com.kuwait.showroomz.model.simplifier.OfferSimplifier
import com.kuwait.showroomz.view.MainActivity
import com.kuwait.showroomz.viewModel.ModelOffersVM

class ModelOfferAdapter(
    val context: Context?,
    private var offers: List<Offer>,
    var viewModel: ModelOffersVM
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TYPE_EXCLUSIVE = 1
    private val TYPE_NORMAL = 2
    var selectedItem = -1
    var lastSelected = -1
    private lateinit var listener: OnItemClickListener
    private var model:ModelSimplifier? = null
    class OfferViewHolder(var view: OfferDetailsItemBinding) : RecyclerView.ViewHolder(view.root) {
        fun bind(offer: Offer) {
            var simplifier = OfferSimplifier(offer)
            view.offer = simplifier
        }
    }


    class ExclusiveOfferViewHolder(var view: ExclusiveOfferDetailsItemBinding) :
        RecyclerView.ViewHolder(view.root) {

        fun bind(context: Context, offer: Offer, viewModel: ModelOffersVM) {
            val dialog: BottomSheetDialog by lazy {  BottomSheetDialog(context, R.style.BottomSheetDialog) }

            val simplifier = OfferSimplifier(offer)
            view.offer = simplifier
            view.callbackBtn.setOnClickListener {

                showCallbackDialog(dialog,context , viewModel)

            }
            viewModel.successCallback.observe(context as MainActivity, Observer {

                if (it) {
                    showSuccessDialog(viewModel, context)
                }
            })
            viewModel.civilIdError.observe(context, Observer {
                if (it) {
                    showErrorDialog(context,context.getString(R.string.invalid_civil_id))
                }
            })
            viewModel.phoneError.observe(context, Observer {
                if (it) {
                    showErrorDialog(context ,context.getString(R.string.invalid_phone))
                }
            })
            viewModel.nameError.observe(context, Observer {
                if (it) {
                    showErrorDialog(context,context.getString(R.string.empty_name))
                }
            })
            viewModel.callbackLoading.observe(context, Observer {
                dialog.dismiss()
            })
        }
        private fun showErrorDialog(context: Context,message: String) {
            val errorDialog =
                BottomSheetDialog(context, R.style.BottomSheetDialog)
            val bindingError: ErrorBottomSheetBinding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.error_bottom_sheet,
                null,
                false
            )
            errorDialog.setContentView(bindingError.root)
            errorDialog.setCancelable(false)
            bindingError.titleText.text =context.getString(R.string.error)
            bindingError.messageText.text =
                message


            errorDialog.show()

            bindingError.exitBtn.setOnClickListener {
                errorDialog.dismiss()


            }

        }
        private fun showSuccessDialog(
            viewModel: ModelOffersVM,
            context: Context
        ) {
            viewModel.successCallback.value = false
            var successDialog = BottomSheetDialog(context, R.style.BottomSheetDialog)
            var bindingSuccess: CallbackSuccessBottomSheetBinding =
                DataBindingUtil.inflate(
                    LayoutInflater.from(context),
                    R.layout.callback_success_bottom_sheet,
                    null,
                    false
                )
            successDialog.setContentView(bindingSuccess.root)
            bindingSuccess.titleText.text =
                context.getString(R.string.done_successfully)
            bindingSuccess.messageText.text =
                context.getString(R.string.representative_contact)
            //                            binding.messageText.text=getString(R.string.representative_contact_delay)

            successDialog.show()

            bindingSuccess.exitBtn.setOnClickListener {
                successDialog.dismiss()


            }
        }

        private fun showCallbackDialog(dialog: BottomSheetDialog, context: Context, viewModel: ModelOffersVM  ) {
            var binding: OffreCallbackBottomSheetBinding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.offre_callback_bottom_sheet,
                null,
                false
            )
            dialog.setContentView(binding.root)
            binding.viewModel = viewModel
            dialog.show()
            binding.exitBtn.setOnClickListener {
                dialog.dismiss()

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_NORMAL) {
            OfferViewHolder(
                OfferDetailsItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        } else ExclusiveOfferViewHolder(
            ExclusiveOfferDetailsItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        )
    }

    override fun getItemCount(): Int {
        return offers.size
    }

    fun setOnItemCLickListener(listener: OnItemClickListener) {
        this.listener = listener

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == TYPE_NORMAL) {
                val offer = offers[position]
            (holder as OfferViewHolder).bind(offer)
            if (model?.hasAllOffer == true){
                holder.view.dividerContainer.visibility = View.GONE
            }else if (position == offers.size - 1) {
                holder.view.dividerContainer.visibility = View.GONE
            }
            if (position == 1) holder.view.root.updatePadding(top = 80)
            if (position == offers.size - 1) holder.view.root.updatePadding(bottom = 80)
            holder.view.checkbox.isClickable = false
            holder.view.checkbox.isVisible = viewModel.fromBuyNow
            holder.view.checkbox.isChecked = viewModel.offers.contains(offers[position])
            if (viewModel.fromBuyNow) {
                holder.itemView.setOnClickListener{
                    val offer = offers[position]
                    viewModel.offers.clear()
                    viewModel.offers.add(offer)
                   /* if ( offers.filter { s -> s.type == 2 }.isNotEmpty()){
                        if (viewModel.offers.contains(offer)){
                            viewModel.offers.remove(offer)
                        }else{
                            viewModel.offers.add(offer)
                        }
                    }else{
                        viewModel.offers.clear()
                        viewModel.offers.add(offer)
                    }*/


                    notifyDataSetChanged()
                }
            }

        } else {
            (holder as ExclusiveOfferViewHolder).bind(context!!, offers[position], viewModel)
            val offer = offers[position]
            holder.view.checkbox.isClickable = false
            holder.view.checkbox.isVisible = viewModel.fromBuyNow
            holder.view.checkbox.isChecked = viewModel.offers.contains(offer)
            holder.view.callbackBtn.isVisible = !viewModel.fromBuyNow
            //viewModel.offers.add(offers[position])
            if (viewModel.fromBuyNow) {
                holder.itemView.setOnClickListener{
                    viewModel.offers.clear()
                    viewModel.offers.add(offer)
                   /* if (offers.filter { s -> s.type == 2 }.isNotEmpty()){
                        if (viewModel.offers.contains(offer)){
                            viewModel.offers.remove(offer)
                        }else{
                            viewModel.offers.add(offer)
                        }
                    }else{

                    }*/
                    notifyDataSetChanged()
                }
            }
        }


    }

    fun refresh(offers: List<Offer>) {
        this.offers = offers.sortedBy { it.type }
        notifyDataSetChanged()
    }

    fun refresh(offers: List<Offer>, model: ModelSimplifier) {
        this.offers = offers.sortedBy { it.type }
        this.model = model
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        val simplifier = OfferSimplifier(offers[position])
        return if (simplifier.isExclusive)
            TYPE_EXCLUSIVE
        else TYPE_NORMAL

    }

    interface OnItemClickListener {
        fun onItemClick(offer: Offer?)
    }
}