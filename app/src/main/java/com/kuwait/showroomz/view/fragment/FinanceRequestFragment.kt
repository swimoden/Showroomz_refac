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
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.FragmentFinanceRequestBinding
import com.kuwait.showroomz.extras.DesignUtils
import com.kuwait.showroomz.model.repository.LogProgressRepository
import com.kuwait.showroomz.view.MainActivity
import com.kuwait.showroomz.view.adapters.FinanceRequestAdapter
import com.kuwait.showroomz.viewModel.FinanceRequestVM


class FinanceRequestFragment : Fragment() {
    private lateinit var binding: FragmentFinanceRequestBinding
    private lateinit var viewModel: FinanceRequestVM
    private val callbacksAdapter: FinanceRequestAdapter by lazy {
        FinanceRequestAdapter(listOf(),context)

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_finance_request, container, false)
            binding.root.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
            return binding.root

    }
    override fun onResume() {
        super.onResume()

        LogProgressRepository.logProgress("Request_finance_list_screen")
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(FinanceRequestVM::class.java)
        viewModel.fetchCallbacks()
        binding.callbackRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = callbacksAdapter
        }
        DesignUtils.instance.setStatusBar(requireActivity() as MainActivity,false,
            ContextCompat.getColor(requireContext(),R.color.colorPrimaryDark))
        onClickListener()
        observeData()
    }

    private fun onClickListener() {
        binding.backBtn.setOnClickListener {
            Navigation.findNavController(it).navigateUp()
        }
    }

    private fun observeData() {
        viewModel.noConnectionError.observe(viewLifecycleOwner,  androidx.lifecycle.Observer {
            if (it){
                DesignUtils.instance.showErrorDialog(requireContext(), null,null){

                }
            }
        })
        viewModel.callbacks.observe(viewLifecycleOwner, Observer {
            callbacksAdapter.refresh(it)
        })
        viewModel.loading.observe(viewLifecycleOwner, Observer { isloading ->
            binding.progressCircular.isVisible = isloading
            binding.callbackRecycler.isVisible = !isloading
            if (isloading) {
                binding.emptyDataTxt.container.isVisible = false

            }
        })
        viewModel.empty.observe(viewLifecycleOwner, Observer {
            binding.emptyDataTxt.container.isVisible = it
            binding.callbackRecycler.isVisible=!it
        })
    }
}