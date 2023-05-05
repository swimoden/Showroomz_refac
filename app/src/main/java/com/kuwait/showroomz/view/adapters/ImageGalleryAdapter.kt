package com.kuwait.showroomz.view.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.GalleryItemBinding
import com.kuwait.showroomz.extras.MyApplication.Key.context
import com.kuwait.showroomz.model.data.Image
import com.kuwait.showroomz.model.simplifier.ImageSimplifier

class ImageGalleryAdapter(private var images: List<Image>) :
    RecyclerView.Adapter<ImageGalleryAdapter.ImageGalleryViewHolder>() {
    var selectedItem = 0
    var lastSelected = 0
    private lateinit var listener: OnItemClickListener
    fun setOnItemCLickListener(listener: OnItemClickListener) {
        this.listener = listener

    }

    inner class ImageGalleryViewHolder(var binding: GalleryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(image: Image) {
            binding.image = ImageSimplifier(image)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageGalleryViewHolder {
        return ImageGalleryViewHolder(
            GalleryItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return images.size
    }

    override fun onBindViewHolder(holder: ImageGalleryViewHolder, position: Int) {
        holder.bind(images[position])
        if (selectedItem == position) {
            holder.binding.imageItem.background = ContextCompat.getDrawable(context, R.drawable.filled_red)
        } else holder.binding.imageItem.background = null

        holder.itemView.setOnClickListener {
            if (listener != null ) {
                listener.onItemClick(images.get(position))
            }
            selectedItem = position
            notifyDataSetChanged()
        }
    }

    fun refreshData(imageGallery: List<Image>?) {
        if (imageGallery != null) {
            this.images = imageGallery
        }
        notifyDataSetChanged()

    }

    interface OnItemClickListener {
        fun onItemClick(image: Image)
    }
}