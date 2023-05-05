package com.kuwait.showroomz.view.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.kuwait.showroomz.R
import com.kuwait.showroomz.model.data.Advertisement
import com.kuwait.showroomz.model.data.Brand
import com.kuwait.showroomz.model.simplifier.BrandSimplifier
import com.kuwait.showroomz.model.simplifier.ImageSimplifier
import com.kuwait.showroomz.view.MainActivity

class ExclusiveBrandAdapter(private var list:List<Brand>, private val action: (brand: Brand) -> (Unit))
    : RecyclerView.Adapter<ExclusiveBrandAdapter.BrandViewExclusiveHolder>()  {
    class BrandViewExclusiveHolder(var view: com.kuwait.showroomz.databinding.BrandExclusiveDashbordItemBinding) : RecyclerView.ViewHolder(view.root)

    fun refresh(brands: List<Brand>) {
        this.list = brands
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrandViewExclusiveHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = DataBindingUtil.inflate<com.kuwait.showroomz.databinding.BrandExclusiveDashbordItemBinding>(inflater, R.layout.brand_exclusive_dashbord_item,parent,false)
        return BrandViewExclusiveHolder(view)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: BrandViewExclusiveHolder, position: Int) {
       val brand = list[position]
        holder.view.brand = BrandSimplifier(brand)

        holder.itemView.setOnClickListener {
            action.invoke(brand)
        }
    }
}


class TopAdsAdapter(private var list:List<Advertisement>, private val action: (ads: Advertisement) -> (Unit))
    : RecyclerView.Adapter<TopAdsAdapter.TopAdsHolder>()  {
    class TopAdsHolder(var view: com.kuwait.showroomz.databinding.TopAdsImagesBinding) : RecyclerView.ViewHolder(view.root)

    fun refresh(brands: List<Advertisement>) {
        this.list = brands
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopAdsHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = DataBindingUtil.inflate<com.kuwait.showroomz.databinding.TopAdsImagesBinding>(inflater, R.layout.top_ads_images,parent,false)
        return TopAdsHolder(view)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: TopAdsHolder, position: Int) {
        val ads = list[position]
        holder.view.image = ads.imageGallery[0]?.let { ImageSimplifier(it) }
        holder.view.root.setOnClickListener {
            action.invoke(ads)
        }
    }
}