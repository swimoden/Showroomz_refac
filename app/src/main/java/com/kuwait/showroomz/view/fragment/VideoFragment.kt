package com.kuwait.showroomz.view.fragment

import android.media.MediaPlayer.OnPreparedListener
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.FragmentVideoBinding
import com.kuwait.showroomz.model.simplifier.VideoSimplifier


class VideoFragment : Fragment() {
    lateinit var binding: FragmentVideoBinding
    private lateinit var simplifier: VideoSimplifier
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_video, container, false)
        binding.root.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments.let {
            val video = it?.let { it1 -> VideoFragmentArgs.fromBundle(it1).video }
            simplifier = video?.let { it1 -> VideoSimplifier(it1) }!!
        }

        binding.videoView.setVideoPath(simplifier.url)
        val mediaController = MediaController(context)
        mediaController.setAnchorView(binding.videoView)
        binding.videoView.setMediaController(mediaController)
        binding.videoView.requestFocus()
        binding.videoView.start()
        binding.progress.isVisible=true
        binding.videoView.setOnPreparedListener(OnPreparedListener {
            binding.progress.isVisible=false
        })






        binding.closeButton.setOnClickListener {
            Navigation.findNavController(it).navigateUp()
        }
    }

}