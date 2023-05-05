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
import com.kuwait.showroomz.databinding.FragmentPaymentListBinding
import com.kuwait.showroomz.extras.DesignUtils
import com.kuwait.showroomz.view.MainActivity
import com.kuwait.showroomz.view.adapters.CallbackAdapter
import com.kuwait.showroomz.view.adapters.PaymentsAdapter
import com.kuwait.showroomz.viewModel.PaymentListVM


class PaymentListFragment : Fragment() {

    private lateinit var binding: FragmentPaymentListBinding
    private lateinit var viewModel: PaymentListVM
    private val paymentsAdapter: PaymentsAdapter by lazy {
        PaymentsAdapter(listOf())

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_payment_list, container, false)
        binding.root.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(PaymentListVM::class.java)
        viewModel.getPayment()
        binding.paymentRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = paymentsAdapter
        }
        DesignUtils.instance.setStatusBar(
            requireActivity() as MainActivity, false,
            ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark)
        )
        onClickListener()
        observeData()
    }

    private fun observeData() {
        viewModel.payments.observe(viewLifecycleOwner, Observer {
            paymentsAdapter.refresh(it)
        })
        viewModel.loading.observe(viewLifecycleOwner, Observer { isloading ->
            binding.progressCircular.isVisible = isloading
            binding.paymentRecycler.isVisible = !isloading
            if (isloading) {
                binding.emptyDataTxt.container.isVisible = false

            }
        })
        viewModel.empty.observe(viewLifecycleOwner, Observer {
            binding.emptyDataTxt.container.isVisible = it
            binding.paymentRecycler.isVisible=!it
        })
        viewModel.noConectionError.observe(viewLifecycleOwner,  androidx.lifecycle.Observer {
            if (it){
                DesignUtils.instance.showErrorDialog(requireContext(), null,null){

                }
            }
        })

    }

    private fun onClickListener() {
        binding.backBtn.setOnClickListener {
            Navigation.findNavController(it).navigateUp()
        }

    }

}