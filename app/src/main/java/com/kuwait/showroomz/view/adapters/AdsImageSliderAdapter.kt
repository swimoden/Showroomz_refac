package com.kuwait.showroomz.view.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.kuwait.showroomz.R
import com.kuwait.showroomz.model.data.Image
import com.kuwait.showroomz.model.simplifier.ImageSimplifier
import kotlinx.android.synthetic.main.image_slider_item.view.*


class AdsImageSliderAdapter(private var images: List<Image>, var context: Context) :
    PagerAdapter() {
    private lateinit var listener: OnItemClickListener
    private var inflater: LayoutInflater? = null

    fun setOnItemCLickListener(listener:OnItemClickListener) {
        this.listener = listener

    }
    override fun isViewFromObject(
        @NonNull view: View,
        @NonNull ob: Any
    ): Boolean {
        return view === ob
    }

    override fun getCount(): Int {
        return images.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater!!.inflate(R.layout.image_slider_item, null)
        val requestOptions = RequestOptions()
        requestOptions.placeholder(R.drawable.place_holder)


        Glide.with(context)
            .setDefaultRequestOptions(requestOptions)

            .load(ImageSimplifier(images[position]).imageUrl)
            .into(view.image_item)


        val vp = container as ViewPager
        vp.addView(view, 0)
        view.setOnClickListener {
           listener.onItemClick()
        }
        return view

    }

    fun refreshData(imageGallery: List<Image>?) {
        if (imageGallery != null) {
            this.images = imageGallery
        }
        notifyDataSetChanged()

    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
    interface OnItemClickListener {
        fun onItemClick()
    }

}