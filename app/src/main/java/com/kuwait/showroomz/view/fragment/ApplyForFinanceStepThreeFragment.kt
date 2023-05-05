package com.kuwait.showroomz.view.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.ErrorBottomSheetBinding
import com.kuwait.showroomz.databinding.FragmentApplyForFinanceStepThreeBinding
import com.kuwait.showroomz.databinding.FragmentApplyForFinanceStepTwoBinding
import com.kuwait.showroomz.extras.CacheObjects
import com.kuwait.showroomz.extras.DesignUtils
import com.kuwait.showroomz.model.data.Bank
import com.kuwait.showroomz.model.repository.LogProgressRepository
import com.kuwait.showroomz.model.simplifier.BankSimplifier
import com.kuwait.showroomz.view.MainActivity
import com.kuwait.showroomz.view.adapters.ApplyForFinanceBanksAdapter
import com.kuwait.showroomz.view.adapters.ApplyForFinanceBanksSelectAdapter
import com.kuwait.showroomz.viewModel.ApplyForFinanceStepThreeVM
import com.kuwait.showroomz.viewModel.ApplyForFinanceStepTwoVM


class ApplyForFinanceStepThreeFragment : Fragment() {
    private lateinit var viewModel: ApplyForFinanceStepThreeVM
    private lateinit var binding: FragmentApplyForFinanceStepThreeBinding
    private var bank:Bank?=null
    private var selectedBank:Bank?=null
    private val bankAdapter: ApplyForFinanceBanksSelectAdapter by lazy {
        ApplyForFinanceBanksSelectAdapter(arrayListOf(), context,viewModel)
    }
    override fun onResume() {
        super.onResume()

        LogProgressRepository.logProgress("Request_finance_callback_screen_with_semi_approval_step3")
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_apply_for_finance_step_three,
                container,
                false
            )
        binding.root.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        return binding.root

    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        DesignUtils.instance.setStatusBar(
            requireActivity() as MainActivity,
            false,
            ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark)
        )
        arguments.let {
            bank= ApplyForFinanceStepThreeFragmentArgs.fromBundle(it!!).bank
            if (bank!=null) {

                binding.message.text =
                    getString(R.string.you_are_not_eligible_with_the_requirements_of) + "\n \"${
                        BankSimplifier(bank).name
                    }\""
            }
        }
        viewModel = ViewModelProviders.of(this).get(ApplyForFinanceStepThreeVM::class.java)
        binding.viewModel = viewModel
        binding.bankRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = bankAdapter
        }
        onClickListener()
        observeData()

    }

    private fun observeData() {

        if (!CacheObjects.availableBanks.isNullOrEmpty()) {
           binding.bankRecycler.isVisible =  CacheObjects.isEligible
            bankAdapter.refreshActions(CacheObjects.availableBanks!!)
        }
        binding.nextBtn.isVisible = CacheObjects.isEligible
        binding.txt.isVisible = CacheObjects.isEligible

    }

    private fun onClickListener() {
        binding.backBtn.setOnClickListener {
            Navigation.findNavController(it).navigateUp()
        }

        binding.nextBtn.setOnClickListener {
            if (viewModel.selectedBanks.isEmpty()){
                showErrorDialog("Select Bank")
            }else{
                CacheObjects.selectedBanks=viewModel.selectedBanks
                Navigation.findNavController(it).navigate(ApplyForFinanceStepThreeFragmentDirections.actionShowFinanceStepFour())
            }
        }

    }
    private fun showErrorDialog(message: String) {
        val errorDialog =
            BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
        val bindingError: ErrorBottomSheetBinding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.error_bottom_sheet,
            null,
            false
        )
        errorDialog.setContentView(bindingError.root)
        errorDialog.setCancelable(false)
        bindingError.titleText.text = getString(R.string.error)
        bindingError.messageText.text =
            message


        errorDialog.show()

        bindingError.exitBtn.setOnClickListener {
            errorDialog.dismiss()


        }

    }

}