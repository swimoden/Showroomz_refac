package com.kuwait.showroomz.view.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.kuwait.showroomz.databinding.CarDetailsImageItemBinding
import com.kuwait.showroomz.databinding.OffersIconItemBinding
import com.kuwait.showroomz.extras.load
import com.kuwait.showroomz.model.data.Image
import com.kuwait.showroomz.model.simplifier.ImageSimplifier
import io.realm.RealmList


class CarDetailsImageAdapter(
    private var images: RealmList<Image>

) :

    RecyclerView.Adapter<CarDetailsImageAdapter.ImageItemViewHolder>() {

    private var showDelete = true

    class ImageItemViewHolder(var view: CarDetailsImageItemBinding) : RecyclerView.ViewHolder(view.root) {
        fun bind(image: Image?, showDelete: Boolean) {
            view.image.load(image?.let { ImageSimplifier(it).imageUrl })
            view.deleteImage.isVisible = showDelete
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageItemViewHolder {
        return ImageItemViewHolder(
            CarDetailsImageItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return images.size
    }

    override fun onBindViewHolder(holder: ImageItemViewHolder, position: Int) {
        holder.bind(images[position], showDelete)

    }
    fun refreshActions(images: RealmList<Image>) {
        this.images = images
        notifyDataSetChanged()
    }
    fun refreshImages(images: RealmList<Image>, showDelete:Boolean) {
        this.images = images
        this.showDelete = showDelete
        notifyDataSetChanged()
    }


}

class OffersIconsImageAdapter( private var images: ArrayList<String>) : RecyclerView.Adapter<OffersIconsImageAdapter.ImageItemViewHolder>() {
    class ImageItemViewHolder(var view: OffersIconItemBinding) : RecyclerView.ViewHolder(view.root) {
        fun bind(image: String) {
            view.image.load(image)

        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageItemViewHolder {
        return ImageItemViewHolder(
            OffersIconItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return images.size
    }

    override fun onBindViewHolder(holder: ImageItemViewHolder, position: Int) {
        holder.bind(images[position])

    }

    fun refreshImages(images: ArrayList<String>) {
        this.images = images
        notifyDataSetChanged()
    }


}