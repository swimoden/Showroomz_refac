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
import com.kuwait.showroomz.databinding.FragmentListAppraisalRequestsBinding
import com.kuwait.showroomz.extras.DesignUtils
import com.kuwait.showroomz.model.repository.LogProgressRepository
import com.kuwait.showroomz.view.MainActivity
import com.kuwait.showroomz.view.adapters.AppraisalRequestsListAdapter
import com.kuwait.showroomz.viewModel.AppraisalCallbackListVM


class AppraisalRequestsListFragment : Fragment() {


    private lateinit var binding: FragmentListAppraisalRequestsBinding
    private lateinit var viewModel: AppraisalCallbackListVM
    private val adapter: AppraisalRequestsListAdapter by lazy {
        AppraisalRequestsListAdapter(listOf())

    }
    override fun onResume() {
        super.onResume()

        LogProgressRepository.logProgress("Appraisal_list_screen")
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_list_appraisal_requests, container, false)
        binding.root.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(AppraisalCallbackListVM::class.java)
        viewModel.fetchAppraisalList()
        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@AppraisalRequestsListFragment.adapter
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
        viewModel.noConectionError.observe(viewLifecycleOwner, Observer {
            if (it){
                DesignUtils.instance.showErrorDialog(requireContext(), null,null){

                }
            }
        })
        viewModel.callbackAppraisal.observe(viewLifecycleOwner, Observer {
            adapter.refresh(it)
        })
        viewModel.loading.observe(viewLifecycleOwner, Observer { isloading ->
            binding.progressCircular.isVisible = isloading
            binding.recycler.isVisible = !isloading
            if (isloading) {
                binding.emptyDataTxt.container.isVisible = false

            }
        })
        viewModel.empty.observe(viewLifecycleOwner, Observer {
            binding.emptyDataTxt.container.isVisible = it
            binding.recycler.isVisible=!it
        })
    }
}