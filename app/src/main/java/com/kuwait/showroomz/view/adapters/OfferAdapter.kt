package com.kuwait.showroomz.view.adapters

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kuwait.showroomz.databinding.OfferItemBinding
import com.kuwait.showroomz.databinding.OffersCombinedItemBinding
import com.kuwait.showroomz.databinding.VideoItemBinding
import com.kuwait.showroomz.extras.MyApplication
import com.kuwait.showroomz.model.data.DataObject
import com.kuwait.showroomz.model.data.Image
import com.kuwait.showroomz.model.data.Model
import com.kuwait.showroomz.model.data.Offer
import com.kuwait.showroomz.model.simplifier.ImageSimplifier
import com.kuwait.showroomz.model.simplifier.OfferSimplifier
import com.kuwait.showroomz.model.simplifier.VideoSimplifier
import com.kuwait.showroomz.view.fragment.ModelDetailFragmentDirections
import io.realm.RealmList


class OfferAdapter( var datas: List<DataObject>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TYPE_OFFER = 1
    private val TYPE_VIDEO = 2
    private lateinit var listener: OnItemClickListener
    fun setOnItemCLickListener(listener: OnItemClickListener){
        this.listener=listener

    }
    class OfferViewHolder(var view: OfferItemBinding) : RecyclerView.ViewHolder(view.root) {
        fun bind(offer: Offer) {
            val simplifier=OfferSimplifier(offer)
            view.offer=simplifier
            if (simplifier.contents?.isNotEmpty()!!){
                var content =simplifier.contents?.get(0)
                if (simplifier.contents!!.size>1){
                    for (x in 1 until simplifier.contents!!.size){
                        content=content+" & "+ simplifier.contents!![x]
                    }
                }
                view.offerContent.text=content
            }
        }
    }
    class VideoViewHolder(var view: VideoItemBinding) : RecyclerView.ViewHolder(view.root) {
        fun bind(video: Image) {
            val simplifier=VideoSimplifier(video)
            view.video=simplifier
         }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TYPE_OFFER) {
            return OfferViewHolder(
                OfferItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
        else  return VideoViewHolder(
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
        if (datas[position] is Offer){
                val offer = datas[position] as Offer
            (holder as OfferViewHolder).bind(offer)
            holder.itemView.setOnClickListener { listener.onItemClick(OfferSimplifier(offer)) }
        }
        if (datas[position] is Image) {
            (holder as VideoViewHolder).bind(datas[position] as Image)
            holder.itemView.setOnClickListener{
                Navigation.findNavController(it).navigate(ModelDetailFragmentDirections.showVideoFragment(datas[position] as Image))
            }
        }



    }
    override fun getItemViewType(position: Int): Int {

        return if (datas[position] is Offer)
            TYPE_OFFER
        else TYPE_VIDEO

    }

    fun refresh(data: List<DataObject>) {
        this.datas = data
        notifyDataSetChanged()
    }
    fun add(data: List<DataObject>) {

        val array = arrayListOf<DataObject>()
        data.forEach {
            array.add(0,it)
        }
        array.addAll(datas)
        datas= listOf()
        datas=array
        notifyDataSetChanged()
    }

    /*fun addOffers(offers: @RawValue RealmList<Offer>) {
        var array = arrayListOf<DataObject>()
        if (datas.isNotEmpty()){
            if (datas[0]is Image){
                var video= datas.first { it is Image }
                array.add(0,video)
            }

        }
        array.addAll(offers)
        datas= listOf()
        datas=array
        notifyDataSetChanged()
    }*/

    interface OnItemClickListener{
        fun onItemClick(offer:OfferSimplifier)
    }

}

class OffersAdapter( var offers:ArrayList<List<Offer>>, var datas: List<DataObject>, private val action: (offers: List<Offer>) -> (Unit)): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TYPE_OFFER = 1
    private val TYPE_VIDEO = 2
    private lateinit var listener: OnItemClickListener
    fun setOnItemCLickListener(listener: OnItemClickListener){
        this.listener=listener
    }

    class OffersViewHolder(var view: OffersCombinedItemBinding) : RecyclerView.ViewHolder(view.root) {
        private val imageAdapter: OffersIconsImageAdapter by lazy {
            OffersIconsImageAdapter(arrayListOf())
        }
        fun bind(offers: List<Offer>) {

            view.iconsRecycler.apply {
                adapter = imageAdapter
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            }
            val images: ArrayList<String> = arrayListOf()
            var offerTitle = ""
            var offerContent = ""
            offers.forEach { offer ->
                offer.icon?.let { image ->
                    val simp = ImageSimplifier(image)
                    if (simp.imageUrl != "") {
                        images.add(simp.imageUrl)
                    }
                }
                val simplifier = OfferSimplifier(offer)
                offerTitle += simplifier.name

                offerContent += simplifier.contents?.first()
                if (offer.id != offers.last().id) {
                    offerTitle += ", "
                    offerContent += " & "
                }
            }
            view.offerName.text = offerTitle
            view.offerContent.text = offerContent
            imageAdapter.refreshImages(images)
        }
    }

    class VideoViewHolder(var view: VideoItemBinding) : RecyclerView.ViewHolder(view.root) {
        fun bind(video: Image) {
            val simplifier = VideoSimplifier(video)
            view.video = simplifier
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TYPE_OFFER) {
            return OffersViewHolder(
                OffersCombinedItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
        else  return VideoViewHolder(
            VideoItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return datas.size + offers.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position < datas.size){
            val vid = datas[position]
            if (vid is Image) {
                (holder as VideoViewHolder).bind(datas[position] as Image)
                holder.itemView.setOnClickListener{
                    val simplifier = VideoSimplifier(vid)
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setDataAndType(Uri.parse(simplifier.url), "video/*")
                    val shareIntent = Intent.createChooser(intent, null)
                    shareIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    ContextCompat.startActivity(MyApplication.context, shareIntent, null)
                   // Navigation.findNavController(it).navigate(ModelDetailFragmentDirections.showVideoFragment(datas[position] as Image))
                }
            }
        }else{
            (holder as OffersViewHolder).bind(offers[ position - datas.size])
            holder.itemView.setOnClickListener{
                action.invoke(offers[ position - datas.size])
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < datas.size) {
            TYPE_VIDEO
        } else {
            TYPE_OFFER
        }
    }

    fun refresh(data: List<DataObject>) {
        this.datas = data
        notifyDataSetChanged()
    }
    fun refreshOffers(offers: ArrayList<List<Offer>>) {
        this.offers = offers
        notifyDataSetChanged()
    }


    interface OnItemClickListener{
        fun onItemClick(offer:OfferSimplifier)
    }

}