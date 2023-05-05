package com.kuwait.showroomz.view.adapters

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.AdsOnScrollItemBinding
import com.kuwait.showroomz.databinding.ModelDashbordItemBinding
import com.kuwait.showroomz.databinding.ModelItemBinding
import com.kuwait.showroomz.extras.DesignUtils
import com.kuwait.showroomz.extras.isEnglish
import com.kuwait.showroomz.extras.toDate
import com.kuwait.showroomz.model.data.*
import com.kuwait.showroomz.model.repository.LogProgressRepository
import com.kuwait.showroomz.model.simplifier.AdsSimplifier
import com.kuwait.showroomz.model.simplifier.ModelSimplifier
import io.realm.RealmObject
import kotlinx.android.synthetic.main.model_dashbord_item.view.*


class ModelAdapter(
    var models: List<RealmObject>,
    var item: ModelDashBoardItem?,
    private var size: Int,
    private var execIndex: Int,
    private val action: (model: Model) -> (Unit),
    private val actionAds: (ads: Advertisement) -> (Unit)?,
    private val actionIdentifier: (simplifier: AdsSimplifier, action: Action?) -> (Unit)?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class BrandModelViewHolder(var view: ModelItemBinding) : RecyclerView.ViewHolder(view.root)
    class ExclusiveBrandModelViewHolder(var view: ModelDashbordItemBinding) : RecyclerView.ViewHolder(view.root)
    class ItemAdsOnScroll(var view: AdsOnScrollItemBinding) : RecyclerView.ViewHolder(view.root)
    private var imageList: List<Image> = listOf()
    private val modelAdapter: ExclusiveModelAdapter by lazy {
        ExclusiveModelAdapter(listOf()) {
            action.invoke(it)
        }
    }

    private val topAdsAdapter: TopAdsAdapter by lazy {
        TopAdsAdapter(listOf()) {
            actionAds.invoke(it)
        }
    }

    fun refresh(item: ModelDashBoardItem?, models: List<RealmObject>?, size: Int, execIndex: Int) {
        this.models = models!!
        this.item = item
        this.size = size
        this.execIndex = execIndex
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            0 -> {
                return ExclusiveBrandModelViewHolder(
                    ModelDashbordItemBinding.inflate(
                        LayoutInflater.from(
                            parent.context
                        ), parent, false
                    )
                )
            }
            1 -> {
                return BrandModelViewHolder(
                    ModelItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            else -> {
                return ItemAdsOnScroll(
                    AdsOnScrollItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )

            }
        }
    }

    override fun getItemCount() = size
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == 0 && models.isNotEmpty()) {
            val holder = (holder as ExclusiveBrandModelViewHolder)
            val model = ModelSimplifier(models[position] as Model)
            val drawable: GradientDrawable? = model.brand?.category?.setBgColor()
                ?.let { DesignUtils.instance.createDrawable(it, GradientDrawable.RECTANGLE) }
            holder.view.viewSep.background = drawable
            holder.view.recyclerView.apply {
                layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

                adapter = if (item?.list != null)
                    modelAdapter
                else
                    topAdsAdapter
            }
            item?.let {
                holder.view.textView4.isVisible = it.title.isNotEmpty()
                holder.view.title = it.title
                it.list?.let { it1 ->
                    modelAdapter.refresh(it1)
                    if (it1.isNotEmpty()) {
                        holder.itemView.recyclerView.scrollToPosition(0)
                    }
                } ?: run {
                    it.ads?.let { it1 ->
                        topAdsAdapter.refresh(it1)
                        if (it1.isNotEmpty()) {
                            holder.itemView.recyclerView.scrollToPosition(0)
                        }
                    }
                }

            }
        } else if (getItemViewType(position) == 1) {
            if (models.isNotEmpty()) {
                val pos = position - (if (item != null) 1 else 0)
                val model = models[pos]
                val s = ModelSimplifier(model as Model)
                val holder = (holder as BrandModelViewHolder)
                holder.view.modelSimplifier = s
                if (s.isNew!!){
                    holder.view.isNewTxt.isVisible=true
                }
                if (s.hasAllOffer){
                    holder.view.hasOfferTxt.isVisible=true
                }
                /*if (execIndex > 0) {
                    if (position == 0)
                       // holder.view.title = holder.itemView.context.resources.getString(R.string.exclusive_offers)
                    else {
                        //holder.view.title = if (item != null && position == 1) holder.itemView.context.resources.getString(R.string.our_dealer) else ""
                    }
                    if (position == execIndex) {
                        // holder.view.title = holder.itemView.context.resources.getString(R.string.our_dealer)
                    }
                } else {
                    // holder.view.title = if (item != null && position == 1) holder.itemView.context.resources.getString(R.string.our_dealer) else ""
                }*/
                holder.itemView.setOnClickListener {
                    action.invoke(model)
                }
                holder.view.root.setOnClickListener {
                    action.invoke(model)
                }
                holder.view.cardView.setOnClickListener {
                    action.invoke(model)
                }

                holder.view.shapeableImageView2.setOnClickListener {
                    action.invoke(model)
                }
                holder.view.container.setOnClickListener {
                    action.invoke(model)
                }
                holder.view.layoutContainer.setOnClickListener {
                    action.invoke(model)
                }
            }
        }else {
            val holder = (holder as ItemAdsOnScroll)
            if (!isEnglish){
                holder.view.titleContainer.rotationY = 180f
                holder.view.title.rotationY = 180f
                val layoutParams = holder.view.title.layoutParams as ViewGroup.MarginLayoutParams
                layoutParams.marginStart = 16
                layoutParams.marginEnd = 100
                holder.view.title.requestLayout()
            }
            val pos = position - (if (item != null) 1 else 0)
            val ads = models[pos] as Advertisement
            LogProgressRepository.incrementNbView(ads)
            val simplifier = AdsSimplifier(ads)
            holder.view.programActions.isVisible = simplifier.actions.isNotEmpty()
            val programsActionAdapter = ProgramActionsAdapter(simplifier.actions)
            holder.view.title.text = simplifier.headline
            holder.view.programActions.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = programsActionAdapter
            }
            val imageSliderAdapter = AdsImageSliderAdapter(imageList, holder.itemView.context)
            holder.view.pager.adapter = imageSliderAdapter
            imageList = ads.imageGallery
            imageList.sortedByDescending { s -> s.createdAt?.toDate()  }
            imageSliderAdapter.refreshData(imageList)
            holder.view.tabLayout.isVisible = ads.imageGallery.size > 1
            holder.view.tabLayout.setupWithViewPager(holder.view.pager)
            imageSliderAdapter.setOnItemCLickListener(object :
                AdsImageSliderAdapter.OnItemClickListener {
                override fun onItemClick() {
                    actionAds.invoke(ads)
                }
            })
            programsActionAdapter.refreshActions(simplifier.actions.sortedBy { action -> action.position })
            programsActionAdapter.setOnItemCLickListener(object :
                ProgramActionsAdapter.OnItemClickListener {
                override fun onItemClick(action: Action?) {
                    ads.id?.let {
                        LogProgressRepository.logProgress(
                            "Actions_${action?.identifier}", "", "", "", "",
                            it, ""
                        )
                    }
                    actionIdentifier.invoke(simplifier,action)
                }
            })
        }
    }

    override fun getItemViewType(position: Int): Int {
        //return if (position == 0 && item != null && execIndex < 10) 0 else 1
        return if (position == 0 && item != null && execIndex < 10) 0 else {
            return if (item == null) {
                if (models[position] is Model) {
                    1
                } else {
                    2
                }
            } else {
                if (models[position - 1] is Model) {
                    1
                } else {
                    2
                }
            }
        }
    }
}