package com.kuwait.showroomz.view.adapters

import android.R.attr.*
import android.graphics.Matrix
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.AdsOnScrollItemBinding
import com.kuwait.showroomz.extras.DesignUtils
import com.kuwait.showroomz.extras.isEnglish
import com.kuwait.showroomz.extras.toDate
import com.kuwait.showroomz.model.data.*
import com.kuwait.showroomz.model.repository.LogProgressRepository
import com.kuwait.showroomz.model.simplifier.AdsSimplifier
import com.kuwait.showroomz.model.simplifier.BrandSimplifier
import io.realm.RealmObject
import kotlinx.android.synthetic.main.dashbord_item.view.*


class BrandsAdapter(
    var brands: List<RealmObject>,
    var item: DashBoardItem?,
    private var size: Int,
    private var execIndex: Int,
    private val action: (brand: Brand) -> (Unit),
    private val actionAds: (ads: Advertisement) -> (Unit)?,
    private val actionIdentifier: (simplifier: AdsSimplifier, action: Action?) -> (Unit)?
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    class BrandViewHolder(var view: com.kuwait.showroomz.databinding.BrandItemBinding) :
        RecyclerView.ViewHolder(view.root)

    class BrandViewExclusiveHolder(var view: com.kuwait.showroomz.databinding.DashbordItemBinding) :
        RecyclerView.ViewHolder(view.root)

    class ItemAdsOnScroll(var view: AdsOnScrollItemBinding) : RecyclerView.ViewHolder(view.root)


    private val brandsAdapter: ExclusiveBrandAdapter by lazy {
        ExclusiveBrandAdapter(listOf()) {
            action.invoke(it)
        }
    }
    private val topAdsAdapter: TopAdsAdapter by lazy {
        TopAdsAdapter(listOf()) {
            actionAds.invoke(it)
        }
    }
    private var imageList: List<Image> = listOf()
    /*private val imageSliderAdapter: AdsImageSliderAdapter by lazy {
        AdsImageSliderAdapter(imageList, )
    }*/


    fun refresh(item: DashBoardItem?, brands: List<RealmObject>?, size: Int, execIndex: Int) {
        this.brands = brands!!
        this.item = item
        this.size = size
        this.execIndex = execIndex
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            0 -> {

                val view =
                    DataBindingUtil.inflate<com.kuwait.showroomz.databinding.DashbordItemBinding>(
                        inflater,
                        R.layout.dashbord_item,
                        parent,
                        false
                    )
                BrandViewExclusiveHolder(view)
            }
            1 -> {

                val view =
                    DataBindingUtil.inflate<com.kuwait.showroomz.databinding.BrandItemBinding>(
                        inflater,
                        R.layout.brand_item,
                        parent,
                        false
                    )
                BrandViewHolder(view)
            }
            else -> {
                val view = DataBindingUtil.inflate<AdsOnScrollItemBinding>(
                    inflater,
                    R.layout.ads_on_scroll_item,
                    parent,
                    false
                )
                ItemAdsOnScroll(view)
            }
        }

    }

    override fun getItemCount() = size

    override fun onBindViewHolder(holderView: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == 0 && brands.isNotEmpty()) {
            val holder = (holderView as BrandViewExclusiveHolder)
            val s = BrandSimplifier(brands[position] as Brand)
            // holder.view.brand = s
            val drawable: GradientDrawable? = s.parentSimpCategory?.setBgColor()
                ?.let { DesignUtils.instance.createDrawable(it, GradientDrawable.RECTANGLE) }
            holder.view.viewSep.background = drawable
            holder.view.recyclerView.apply {
                adapter = if (item?.list != null)
                    brandsAdapter
                else
                    topAdsAdapter
            }
            item?.let {
                holder.view.textView4.isVisible = it.title.isNotEmpty()
                holder.view.title = it.title
                it.list?.let { it1 ->
                    brandsAdapter.refresh(it1)
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
            val pos = position - (if (item != null) 1 else 0)
            val brand = brands[pos]
            val s = BrandSimplifier(brand as Brand)
            val holder = (holderView as BrandViewHolder)
            holder.view.brand = s
            holder.view.closedTxt.isVisible = s.isClosed
            if (execIndex > 0) {
                if (position == 0)
                    holder.view.title =
                        if (s.showExclusiveOnTop == true) holder.itemView.context.resources.getString(
                            R.string.exclusive_offers
                        ) else ""
                else {
                    holder.view.title =
                        if (item != null && position == 1) holder.itemView.context.resources.getString(
                            R.string.our_dealer
                        ) else ""
                }
                if (position == execIndex) {
                    holder.view.title =
                        holder.itemView.context.resources.getString(R.string.our_dealer)
                }
            } else {
                holder.view.title =
                    if (item != null && position == 1) holder.itemView.context.resources.getString(R.string.our_dealer) else ""
            }

            holder.itemView.setOnClickListener {
                action.invoke(brand)
            }

        } else {

            val holder = (holderView as ItemAdsOnScroll)
            if (!isEnglish){
                holder.view.titleContainer.rotationY = 180f
                holder.view.title.rotationY = 180f
                val layoutParams = holder.view.title.layoutParams as ViewGroup.MarginLayoutParams
                layoutParams.marginStart = 16
                layoutParams.marginEnd = 100
                holder.view.title.requestLayout()
            }
            val pos = position - (if (item != null) 1 else 0)
            val ads = brands[pos] as Advertisement
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
        /* item?.let {
             if (it.ads != null) {
                 return if (position == 0) 2 else 1
             }else if (it.list != null){
                 return if (position == 0) 0 else 1
             }
         }*/
        return if (position == 0 && item != null && execIndex < 10) 0 else {
            if (item == null) {
                if (brands.get(position) is Brand) {
                    return 1
                } else {
                    return 2
                }

            } else {
                if (brands.get(position - 1) is Brand) {
                    return 1
                } else {
                    return 2
                }
            }
        }
    }


}




