package com.kuwait.showroomz.view.fragment

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.FragmentModelImageBinding
import com.kuwait.showroomz.extras.DesignUtils
import com.kuwait.showroomz.model.data.Model
import com.kuwait.showroomz.model.simplifier.ModelSimplifier
import com.kuwait.showroomz.viewModel.ModelDetailVM
import com.kuwait.showroomz.viewModel.ModelVM


class ModelImageFragment : Fragment() {

    private lateinit var model: Model
    private lateinit var simplifier: ModelSimplifier
    private lateinit var viewModel: ModelDetailVM
    private lateinit var binding: FragmentModelImageBinding
    private var isInterior: Boolean = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        container?.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_model_image, container, false)
        binding.root.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let {
            DesignUtils.instance.changeScreenOrientation(
                it,
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            )
        }
        viewModel = ViewModelProviders.of(this).get(ModelDetailVM::class.java)

        arguments?.let {
            model = ModelImageFragmentArgs.fromBundle(it).model
            simplifier = ModelSimplifier(model)
            isInterior = ModelImageFragmentArgs.fromBundle(it).isInterior
            binding.isInterior = isInterior
//            binding.model = simplifier
        }

        binding.webView.settings.javaScriptEnabled = true
        if (isInterior)
            simplifier.internalImage?.let { binding.webView.loadUrl(it) }
        else simplifier.externalImage?.let { binding.webView.loadUrl(it) }
        onClickListener()
        viewModel.noConectionError.observe(viewLifecycleOwner,  androidx.lifecycle.Observer {
            if (it){
                DesignUtils.instance.showErrorDialog(requireContext(), null,null){

                }
            }
        })

    }

    private fun onClickListener() {
        binding.button.setOnClickListener { Navigation.findNavController(it).navigateUp() }
        binding.imageButton.setOnClickListener {
            if (isInterior) {
                simplifier.externalImage?.let { it1 -> binding.webView.loadUrl(it1) }

            } else simplifier.internalImage?.let { it1 -> binding.webView.loadUrl(it1) }
            isInterior = !isInterior
            binding.isInterior = isInterior
        }
    }

}