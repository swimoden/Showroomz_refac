package com.kuwait.showroomz.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.FragmentFinanceCallbackItemBinding
import com.kuwait.showroomz.extras.DesignUtils
import com.kuwait.showroomz.model.data.Bank
import com.kuwait.showroomz.model.data.Callback
import com.kuwait.showroomz.model.simplifier.CallbackSimplifier
import com.kuwait.showroomz.model.simplifier.ModelSimplifier
import com.kuwait.showroomz.view.MainActivity
import com.kuwait.showroomz.view.adapters.BanksAdapter
import com.kuwait.showroomz.viewModel.FinanceVM
import kotlinx.android.synthetic.main.fragment_finance.*
import java.util.Observer


class FinanceCallbackItemFragment : Fragment() {

    lateinit var binding: FragmentFinanceCallbackItemBinding
    private lateinit var viewModel: FinanceVM
    private var banks: ArrayList<Bank> = arrayListOf()
    private var ratios: List<Double> = arrayListOf()
    private lateinit var callback: Callback
    private val bankAdapter: BanksAdapter by lazy {
        BanksAdapter(listOf(), context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_finance_callback_item,
            container,
            false
        )
        binding.root.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(FinanceVM::class.java)
        arguments.let {
             it?.let { it1 ->
                 callback =  FinanceCallbackItemFragmentArgs.fromBundle(it1).callback
                 binding.callback = CallbackSimplifier(callback)
            }

        }
        DesignUtils.instance.setStatusBar(
            requireActivity() as MainActivity,
            false,
            callback.let { it1 -> CallbackSimplifier(it1) }.model?.brand?.category?.setBgColor()
        )
        callback.bank?.let {
            val simp = CallbackSimplifier(callback)
            simp.bankData?.let { it1 ->
                banks.clear()
                banks.add(it1)
            }
        }
        bankAdapter.refreshActions(banks)
        binding.banksRecycler.apply {
            adapter = bankAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
        bankAdapter.selectedItem = 0
        bankAdapter.lastSelected = 0
        bankAdapter.clickable = false
        if (callback.installmentAmount != "" && callback.installmentAmount != null) {
            when (callback.installmentPeriod) {
                "60" -> {
                    binding.fiveContainer.background =
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.round_background_black
                        )
                    binding.fiveYearText.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            (R.color.colorWhite)
                        )
                    )
                }
                "48" -> {
                    binding.fourContainer.background =
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.round_background_black
                        )
                    binding.fourYearText.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            (R.color.colorWhite)
                        )
                    )
                }
                "36" -> {
                    binding.threeContainer.background =
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.round_background_black
                        )
                    binding.threeYearText.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            (R.color.colorWhite)
                        )
                    )
                }
                "24" -> {
                    binding.twoContainer.background =
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.round_background_black
                        )
                    binding.twoYearText.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            (R.color.colorWhite)
                        )
                    )
                }
                "12" -> {
                    binding.oneContainer.background =
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.round_background_black
                        )
                    binding.oneYearText.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            (R.color.colorWhite)
                        )
                    )
                }
            }
        }
        binding.backBnt.setOnClickListener {
            Navigation.findNavController(it).navigateUp()
        }
        binding.downImg.setOnClickListener {
            result_container.scrollTo(0, result_container.bottom)
        }
        binding.applyNowBtn.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(FinanceCallbackItemFragmentDirections.showRequestDetail(null, callback))
        }
        viewModel.noConnectionError.observe(viewLifecycleOwner,  androidx.lifecycle.Observer {
            if (it){
                DesignUtils.instance.showErrorDialog(requireContext(), null,null){

                }
            }
        })
    }


}