package com.kuwait.showroomz.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.FragmentNetworkSettingBinding
import com.kuwait.showroomz.extras.DesignUtils
import com.kuwait.showroomz.extras.MyApplication
import com.kuwait.showroomz.extras.Shared
import com.kuwait.showroomz.view.MainActivity


class NetworkSettingFragment : Fragment() {
    private val prefs = Shared()
    private lateinit var binding: FragmentNetworkSettingBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_network_setting, container, false)
        binding.root.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        binding.offlineModeContainer.isVisible = prefs.bool(MyApplication.LANGUAGE_SELECTED)
        DesignUtils.instance.setStatusBar(requireActivity() as MainActivity,false,
            ContextCompat.getColor(requireContext(),R.color.colorPrimaryDark))
        binding.tryAgain.setOnClickListener {
            (activity as MainActivity).restartActivity()
        }
        binding.offlineMode.setOnClickListener {
            val action = NetworkSettingFragmentDirections.actionNetworkFragmentToDashboardFragment()
            view?.let { it1 -> Navigation.findNavController(it1).navigate(action) }
        }
        return binding.root
    }


}