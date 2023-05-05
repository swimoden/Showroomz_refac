package com.kuwait.showroomz.view.adapters

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.kuwait.showroomz.databinding.VideoItemBinding
import com.kuwait.showroomz.extras.MyApplication.Key.context
import com.kuwait.showroomz.model.data.DataObject
import com.kuwait.showroomz.model.data.Image
import com.kuwait.showroomz.model.simplifier.VideoSimplifier
import com.kuwait.showroomz.view.fragment.ModelDetailFragmentDirections


class VideoAdapter(var datas: List<DataObject>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var listener: OnItemClickListener


    class VideoViewHolder(var view: VideoItemBinding) : RecyclerView.ViewHolder(view.root) {
        fun bind(video: Image) {
            var simplifier=VideoSimplifier(video)
            view.video=simplifier
//            view.thumbnailImg.setImageBitmap(simplifier.thumbnail)


        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
         return VideoViewHolder(
             VideoItemBinding.inflate(
                 LayoutInflater.from(parent.context),
                 parent,
                 false
             )
         )
    }

    override fun getItemCount(): Int {
        return datas.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (datas[position] is Image) {
            (holder as VideoViewHolder).bind(datas[position] as Image)
            holder.view.linearLayout.setOnClickListener{
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(Uri.parse(""), "video/*")
                val shareIntent = Intent.createChooser(intent, null)
                startActivity(context, shareIntent, null)
               // ContextCompat.startActivity(Intent.createChooser(intent, "Complete action using"))
                //Navigation.findNavController(it).navigate(ModelDetailFragmentDirections.showVideoFragment(datas[position] as Image))
            }

        }



    }

    fun refresh(videos: List<DataObject>) {
        this.datas = videos

        notifyDataSetChanged()
    }
    interface OnItemClickListener{
        fun onItemClick()
    }

}