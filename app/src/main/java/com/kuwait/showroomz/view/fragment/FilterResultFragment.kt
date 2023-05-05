package com.kuwait.showroomz.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.FragmentFilterResultBinding
import com.kuwait.showroomz.extras.DesignUtils
import com.kuwait.showroomz.extras.WebViewLocaleHelper
import com.kuwait.showroomz.model.data.FilterAttributes
import com.kuwait.showroomz.model.simplifier.BrandSimplifier
import com.kuwait.showroomz.view.MainActivity
import com.kuwait.showroomz.view.adapters.ModelAdapter
import com.kuwait.showroomz.viewModel.FilterResultViewModel
import kotlinx.android.synthetic.main.fragment_filter_result.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class FilterResultFragment : Fragment() {

    private lateinit var binding: FragmentFilterResultBinding
    private lateinit var result: FilterAttributes
    private lateinit var viewModel: FilterResultViewModel
    private var size = 0
    private val modelAdapter: ModelAdapter by lazy {
        ModelAdapter(emptyList(), null, size, 0, {
            val action = FilterResultFragmentDirections.aactionShowModelDetail(
                it,
                null, null
            )
            view?.let { it1 ->
                Navigation.findNavController(it1).navigate(action)
            }
        }, {

        },{ ads, action ->
           // (activity as MainActivity).callAction(ads,action)
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val helper = context?.let { WebViewLocaleHelper(it) }
        helper?.implementWorkaround()
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_filter_result, container, false)
        binding.root.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(FilterResultViewModel::class.java)
        arguments.let { bundle ->

            lifecycleScope.launch {
               //delay(225)
                bundle?.let {
                    val attr = FilterResultFragmentArgs.fromBundle(bundle).modelsResult
                    attr.category?.let { it1 -> viewModel.refreshCate(it1) }
                    viewModel.filterFromLocal(attr)
                    if (attr.ids?.size!! > 1 || attr.ids?.size == 0)
                        binding.result = resources.getString(R.string.results)
                    else binding.result = BrandSimplifier(attr.ids!![0]).name
                }


                model_recycler.apply {
                    layoutManager = LinearLayoutManager(context)
                    adapter = modelAdapter
                }
            }

        }
        DesignUtils.instance.setStatusBar(
            requireActivity() as MainActivity, true,
            ContextCompat.getColor(requireContext(), R.color.off_white)
        )
        onClickListener()
        observeResult()
    }

    private fun onClickListener() {
        back.setOnClickListener {

            Navigation.findNavController(it).navigateUp()

        }
    }

    private fun observeResult() {
        viewModel.noConnectionError.observe(viewLifecycleOwner, Observer {
            if (it) {
                DesignUtils.instance.showErrorDialog(requireContext(), null, null) {

                }
            }
        })
        viewModel.models.observe(viewLifecycleOwner, Observer {
            modelAdapter.refresh(null, it, it.size, -1)
        })
        viewModel.loading.observe(viewLifecycleOwner, Observer { isloading ->
            if (isloading) print("Loading") else print("not loading")
            if (isloading) {
                binding.progressCircular.visibility = View.VISIBLE
            } else binding.progressCircular.visibility = View.GONE

        })

        viewModel.error.observe(viewLifecycleOwner, Observer { error ->
            print(error)
        })
        viewModel.empty.observe(viewLifecycleOwner, Observer {
            binding.emptyDataTxt.root.isVisible = it
        })
    }


}