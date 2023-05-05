package com.kuwait.showroomz.view.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kuwait.showroomz.databinding.CarDetailsImageItemBinding
import com.kuwait.showroomz.extras.MyApplication.Key.context
import com.kuwait.showroomz.model.data.Image
import io.realm.RealmList


class CarImageAdapter(
    private var images: ArrayList<Uri>, var action:(position:Int) -> Unit

) :

    RecyclerView.Adapter<CarImageAdapter.ImageItemViewHolder>() {



    class ImageItemViewHolder(var view: CarDetailsImageItemBinding) : RecyclerView.ViewHolder(view.root) {
        fun bind(image: Uri?,position:Int,  action:(position:Int) -> Unit) {
            view.image.setImageURI(image)
            Glide.with(context).load(image).override(100, 100).into(view.image)

            view.deleteImage.setOnClickListener {
                action.invoke(position)
            }
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
        holder.bind(images[position],position, action)



    }
        fun addImage(uri:Uri){
            images.add(0,uri)
            notifyItemInserted(0)
        }

    fun removeimage() {
        if (images.size > 0)
            images.removeAt(0)
        notifyDataSetChanged()
    }
    fun removeimage(atIndex:Int){
        images.removeAt(atIndex)
        notifyDataSetChanged()
    }
    fun refreshActions(images: ArrayList<Uri>) {
        this.images = images
        notifyDataSetChanged()
    }


}