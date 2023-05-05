package com.kuwait.showroomz.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.ApplyForFinanceBankListBottomSheetBinding
import com.kuwait.showroomz.databinding.ErrorBottomSheetBinding
import com.kuwait.showroomz.databinding.FragmentApplyForFinanceStepTwoBinding
import com.kuwait.showroomz.databinding.IndustriesListBottomSheetBinding
import com.kuwait.showroomz.extras.CacheObjects
import com.kuwait.showroomz.extras.DesignUtils
import com.kuwait.showroomz.extras.hideKeyboard
import com.kuwait.showroomz.model.data.Bank
import com.kuwait.showroomz.model.repository.LogProgressRepository
import com.kuwait.showroomz.model.simplifier.BankSimplifier
import com.kuwait.showroomz.view.MainActivity
import com.kuwait.showroomz.view.adapters.ApplyForFinanceBanksAdapter
import com.kuwait.showroomz.viewModel.ApplyForFinanceStepTwoVM
import kotlinx.android.synthetic.main.fragment_apply_for_finance_step_two.*


class ApplyForFinanceStepTwoFragment : Fragment() {
    private lateinit var viewModel: ApplyForFinanceStepTwoVM
    private lateinit var binding: FragmentApplyForFinanceStepTwoBinding
    private var bank: Bank? = null
    private var isEligible:Boolean?=false
    private val banksDialog: BottomSheetDialog by lazy {
        BottomSheetDialog(
            requireContext(),
            R.style.BottomSheetDialog
        )
    }
    private val bankAdapter: ApplyForFinanceBanksAdapter by lazy {
        ApplyForFinanceBanksAdapter(arrayListOf(), context)
    }
    override fun onResume() {
        super.onResume()

        LogProgressRepository.logProgress("Request_finance_callback_screen_with_semi_approval_step2")
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_apply_for_finance_step_two,
                container,
                false
            )
        binding.root.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        viewModel = ViewModelProviders.of(this).get(ApplyForFinanceStepTwoVM::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        DesignUtils.instance.setStatusBar(
            requireActivity() as MainActivity,
            false,
            ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark)
        )
        arguments.let {
            bank = ApplyForFinanceStepTwoFragmentArgs.fromBundle(it!!).bank
            isEligible = ApplyForFinanceStepTwoFragmentArgs.fromBundle(it).isEligible
        }

        binding.viewModel = viewModel
        onClickListener()
        observeData()

    }

    private fun observeData() {
        viewModel.noConectionError.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it){
                DesignUtils.instance.showErrorDialog(requireContext(), null,null){

                }
            }
        })
        viewModel.success.observe(viewLifecycleOwner, Observer {
            if (it) {
                viewModel.success.value = false
                if (isEligible == true) {
                    Navigation.findNavController(requireView())
                        .navigate(ApplyForFinanceStepTwoFragmentDirections.actionShowFinanceStepFour())
                }else {
                    Navigation.findNavController(requireView())
                        .navigate(ApplyForFinanceStepTwoFragmentDirections.actionShowFinanceStepThree(bank))
                }
            }
        })
        viewModel.banks.observe(viewLifecycleOwner, Observer {
            bankAdapter.refreshActions(it)
        })
        viewModel.error.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it == "1") {
                viewModel.error.value = ""
                Navigation.findNavController(requireView())
                    .navigate(ApplyForFinanceStepTwoFragmentDirections.actionShowFinanceStepThree(bank))
            }
            if (it == "error") {
                showErrorDialog("error")
            }

        })
        viewModel.validBank.observe(viewLifecycleOwner, Observer {
            if (!it)
                showErrorDialog("Select Bank")
        })
        viewModel.validLoanAmount.observe(viewLifecycleOwner, Observer {
            if (!it) showErrorDialog("Invalid Loan Amount")
        })
        viewModel.validCreditCardAmount.observe(viewLifecycleOwner, Observer {
            if (!it) showErrorDialog("Invalid Credit card Amount")
        })
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

    private fun showBanks() {

        var dialogBinding: ApplyForFinanceBankListBottomSheetBinding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.apply_for_finance_bank_list_bottom_sheet,
            null,
            false
        )
        banksDialog.setContentView(dialogBinding.root)
        dialogBinding.bankListRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = bankAdapter
        }

        banksDialog.show()
        banksDialog.setCancelable(false)
        dialogBinding.exitBtn.setOnClickListener {
            banksDialog.dismiss()
        }

    }

    private fun onClickListener() {
        scroll.setOnClickListener {
            it.hideKeyboard()
        }
        binding.backBtn.setOnClickListener {
            Navigation.findNavController(it).navigateUp()
        }
        binding.bankConstraint.setOnClickListener {
            showBanks()
        }
        bankAdapter.setOnItemCLickListener(object :
            ApplyForFinanceBanksAdapter.OnItemClickListener {
            override fun onItemClick(bank: Bank) {
                binding.bankTextInputEditText.setText(BankSimplifier(bank).name)
                banksDialog.dismiss()
                viewModel.selectedBank = bank
            }

        })
        binding.yesCheckbox.setOnCheckedChangeListener { compoundButton, isChecked ->
            viewModel.haveLoan.set(isChecked)
        }
        binding.noCheckbox.setOnCheckedChangeListener { compoundButton, isChecked ->
            if (isChecked)
                viewModel.loanPayment.set("")
            viewModel.haveLoan.set(!isChecked)

        }
        binding.yesCheckboxCreditCard.setOnCheckedChangeListener { compoundButton, isChecked ->
            viewModel.haveCreditCard.set(isChecked)
        }
        binding.noCheckboxCreditCard.setOnCheckedChangeListener { compoundButton, isChecked ->
            if (isChecked)
                viewModel.creditCardPayment.set("")
            viewModel.haveCreditCard.set(!isChecked)


        }

    }

}


