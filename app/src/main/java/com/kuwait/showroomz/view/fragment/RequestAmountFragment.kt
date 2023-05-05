package com.kuwait.showroomz.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.CallbackSuccessBottomSheetBinding
import com.kuwait.showroomz.databinding.FinanceCallbackBottomSheetBinding
import com.kuwait.showroomz.databinding.FragmentFinanceBinding
import com.kuwait.showroomz.databinding.FragmentRequestAmountBinding
import com.kuwait.showroomz.extras.*
import com.kuwait.showroomz.model.data.Bank
import com.kuwait.showroomz.model.data.Model
import com.kuwait.showroomz.model.simplifier.ApplyForFinanceParams
import com.kuwait.showroomz.model.simplifier.BankSimplifier
import com.kuwait.showroomz.model.simplifier.ModelSimplifier
import com.kuwait.showroomz.model.simplifier.TrimSimplifier
import com.kuwait.showroomz.view.MainActivity
import com.kuwait.showroomz.view.adapters.BanksAdapter
import com.kuwait.showroomz.view.adapters.PeriodInstallmentAdapter
import com.kuwait.showroomz.viewModel.FinanceVM

import kotlinx.android.synthetic.main.fragment_request_amount.*
import kotlin.math.pow
import kotlin.math.roundToInt


class RequestAmountFragment : Fragment() {

    private var monthlyPayment: Double? = null
    lateinit var binding: FragmentRequestAmountBinding
    private lateinit var viewModel: FinanceVM
    private var banks: List<Bank> = arrayListOf()
    private var ratios: List<Double> = arrayListOf()
    private var selectedBank: Bank? = null
    private var selectedRatio: String? = null
    private lateinit var model: Model

    private val bankAdapter: BanksAdapter by lazy {
        BanksAdapter(banks, context)
    }
    private val installmentAdapter: PeriodInstallmentAdapter by lazy {
        PeriodInstallmentAdapter(ratios, context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_request_amount, container, false)
        binding.root.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(FinanceVM::class.java)

        arguments?.let {
            RequestAmountFragmentArgs.fromBundle(it).bank?.let{
                selectedBank = it
                val list:ArrayList<Bank> = arrayListOf()
                list.add(it)
                banks = list
                CacheObjects.selectedBanks = banks
                viewModel.bankId = it.id
                bankAdapter.refreshActions(banks)
                if (!banks.isNullOrEmpty()) {
                    val b = banks[0]
                    installmentAdapter.refreshActions(b.ratio)
                    val simplifier = BankSimplifier(b)
                    binding.bank = simplifier
                    banks_recycler.scrollToPosition(0)
                }
            }?: run {
                viewModel.getBanks()
            }
        }
        DesignUtils.instance.setStatusBar(
            requireActivity() as MainActivity,
            false,
            ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark)
        )
        if (!selectedBank.isNull() && !selectedRatio.isNullOrEmpty()) {
            calculator_container.visibility = View.VISIBLE
            calculate_btn.isVisible = false
            callback_btn.isVisible = true
            apply_now_btn.isVisible = true
        } else {
            calculator_container.visibility = View.GONE
           // calculate_btn.isVisible = true
            callback_btn.isVisible = false
            apply_now_btn.isVisible = false

        }
            if (viewModel.isRatioSelected()&&selectedBank!=null&&binding.amountTxt.text.toString()!=""){
                calculate()
            }
        binding.banksRecycler.apply {
            adapter = bankAdapter
//            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        }
        more_img.setOnClickListener {
            binding.banksRecycler.layoutManager?.childCount?.plus(1)?.let { it1 ->
                binding.banksRecycler.smoothScrollToPosition(
                    it1
                )
            }

        }
        down_img.setOnClickListener {
            result_container.scrollTo(0, result_container.bottom)
        }
        binding.installmentPeriodRecycler.apply {
            adapter = installmentAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
        checkSelectedRatio()
        observeData()
        onClickListener()
        view.setOnClickListener {
            it.hideKeyboard()
        }
    }

    private fun checkSelectedRatio() {
        if (viewModel.isRatioSelected()) {
            when (viewModel.installmentPeriod) {
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
    }

    private fun onClickListener() {

        calculate_btn.setOnClickListener {
            if (amount_txt.text.toString().isNullOrEmpty()) {
                amount_txt.startAnimation(
                    AnimationUtils.loadAnimation(
                        activity,
                        R.anim.shake
                    )
                )
                return@setOnClickListener
            }
            if (!down_payment_txt.text.toString()
                    .isNullOrEmpty() && down_payment_txt.text.toString()
                    .toBigDecimal() > amount_txt.text.toString()
                    .toBigDecimal()
            ) {
                down_payment_txt.startAnimation(
                    AnimationUtils.loadAnimation(
                        activity,
                        R.anim.shake
                    )
                )
                return@setOnClickListener
            }
            if (!selectedBank.isNull()) {
                if (!down_payment_txt.text.toString()
                        .isNullOrEmpty() && down_payment_txt.text.toString()
                        .toInt() < selectedBank?.downPayment!!.toInt()
                ) {
                    down_payment_txt.setText(selectedBank?.downPayment)
                    down_payment_txt.startAnimation(
                        AnimationUtils.loadAnimation(
                            activity,
                            R.anim.shake
                        )
                    )
                    return@setOnClickListener
                }
            }

            if (selectedRatio.isNullOrEmpty()) {

                installment_period_container.startAnimation(
                    AnimationUtils.loadAnimation(
                        activity,
                        R.anim.shake
                    )
                )
                return@setOnClickListener
            }
            if (selectedBank.isNull()) {
                banks_recycler.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.shake))
                return@setOnClickListener
            }
            calculate()

        }
        binding.backBnt.setOnClickListener {

            Navigation.findNavController(it).navigateUp()
        }
        one_container.setOnClickListener {
            setUnselectedYears()
            selectedRatio = one_container.tag.toString()
            calculate()
            one_container.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.round_background_black)
            one_year_text.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    (R.color.colorWhite)
                )
            )
        }
        two_container.setOnClickListener {
            setUnselectedYears()
            selectedRatio = two_container.tag.toString()
            calculate()
            two_container.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.round_background_black)
            two_year_text.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    (R.color.colorWhite)
                )
            )
        }
        three_container.setOnClickListener {
            setUnselectedYears()
            selectedRatio = three_container.tag.toString()
            calculate()
            three_container.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.round_background_black)
            three_year_text.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    (R.color.colorWhite)
                )
            )
        }
        four_container.setOnClickListener {
            setUnselectedYears()
            selectedRatio = four_container.tag.toString()
            calculate()
            four_container.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.round_background_black)
            four_year_text.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    (R.color.colorWhite)
                )
            )
        }
        five_container.setOnClickListener {
            setUnselectedYears()

            selectedRatio = five_container.tag.toString()
            calculate()
            five_container.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.round_background_black)
            five_year_text.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    (R.color.colorWhite)
                )
            )
        }
        bankAdapter.setOnItemCLickListener(object : BanksAdapter.OnItemClickListener {
            override fun onItemClick(bank: Bank) {
                selectedBank = bank
                binding.bank = BankSimplifier(selectedBank)
                viewModel.bankId = bank.id
                if (bank.downPayment != "0.000" &&  binding.downPaymentTxt.text.isEmpty()) {
                    binding.downPaymentTxt.setText(bank.downPayment)
                }
                selectedBank?.let {
                    val x:ArrayList<Bank> = arrayListOf()
                    x.add(it)
                    CacheObjects.selectedBanks=x
                }
                calculate()
            }
        })
        callback_btn.setOnClickListener {
            var dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
            var binding: FinanceCallbackBottomSheetBinding = DataBindingUtil.inflate(
                LayoutInflater.from(requireContext()),
                R.layout.finance_callback_bottom_sheet,
                null,
                false
            )
            binding.viewModel = viewModel
            dialog.setContentView(binding.root)
            dialog.show()
            binding.exitBtn.setOnClickListener {
                dialog.hide()
            }
            viewModel.successCallback.observe(viewLifecycleOwner, Observer {

                if (it) {
                    viewModel.successCallback.value = false
                    var successDialog =
                        BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
                    var bindingSuccess: CallbackSuccessBottomSheetBinding = DataBindingUtil.inflate(
                        LayoutInflater.from(requireContext()),
                        R.layout.callback_success_bottom_sheet,
                        null,
                        false
                    )
                    successDialog.setContentView(bindingSuccess.root)
                    bindingSuccess.titleText.text = getString(R.string.done_successfully)
                    bindingSuccess.messageText.text =
                        getString(R.string.bank_representative_contact)
//                            binding.messageText.text=getString(R.string.bank_representative_contact_delay)

                    successDialog.show()

                    bindingSuccess.exitBtn.setOnClickListener {
                        successDialog.dismiss()
                        dialog.dismiss()

                    }
                }
            })

        }

        binding.applyNowBtn.setOnClickListener {

            var params = ApplyForFinanceParams(
                viewModel.downpayment,
                viewModel.loanAmount,
                viewModel.installmentPeriod,
                viewModel.totalCost,
                viewModel.installmentAmount,
                viewModel.profite,
                if (!viewModel.isValidModel()) null else viewModel.modelId,
                viewModel.bankId
            )

            viewModel.prefs.setParcelable("APPLY_FINANCE", params)
            if (selectedBank?.allowSemiApproval == true && !viewModel.isCivilIdMandatory.get()) {
                if (viewModel.isConnected()) {

                    Navigation.findNavController(it).navigate(
                        RequestAmountFragmentDirections.showApplyForFinanceStepOneAmount(
                            selectedBank!!
                        )
                    )

                } else {
                    CacheObjects.bank = selectedBank
                    Navigation.findNavController(it)
                        .navigate(FinanceFragmentDirections.showLogin(R.id.applyForFinanceStepOne))
                }
            } else {

                if (viewModel.isConnected()) {

                    Navigation.findNavController(it).navigate(
                        RequestAmountFragmentDirections.showCallbackBank(
                        )
                    )

                } else {
                    Navigation.findNavController(it)
                        .navigate(RequestAmountFragmentDirections.showLogin(R.id.applyForFinance))
                }
            }
        }


    }


    private fun calculate() {
        if (amount_txt.text.toString().isNullOrEmpty()) {
            amount_txt.startAnimation(
                AnimationUtils.loadAnimation(
                    activity,
                    R.anim.shake
                )
            )
            return
        }
        if (!down_payment_txt.text.toString()
                .isNullOrEmpty() && down_payment_txt.text.toString()
                .toBigDecimal() > amount_txt.text.toString()
                .toBigDecimal()
        ) {
            down_payment_txt.startAnimation(
                AnimationUtils.loadAnimation(
                    activity,
                    R.anim.shake
                )
            )
            return
        }
        if (!selectedBank.isNull() && !selectedRatio.isNullOrEmpty()&&isValidAmount()) {
            calculator_container.visibility = View.VISIBLE
            calculate_btn.isVisible = false
            callback_btn.isVisible = true
            apply_now_btn.isVisible = true
            val ratio =
                if (selectedBank!!.ratio.size > 1) selectedBank!!.ratio[selectedRatio!!.toInt() - 1] else selectedBank!!.ratio[0]
            val downPayment = if (down_payment_txt.text.toString().isNullOrEmpty()) {
                if (selectedBank?.downPayment.isNullOrEmpty()) 0.0 else selectedBank?.downPayment?.toDouble()!!
            } else down_payment_txt.text.toString().toDouble()

            val userPayment = binding.amountTxt.text.toString()
                .toDouble() - downPayment
            if (selectedBank?.ratio!!.size > 1) {
                val d: Double = userPayment * ratio!!
                monthlyPayment = (d.roundToInt() + userPayment) / (selectedRatio!!.toInt() * 12)
            } else {
                val d = (Math.pow(
                    1 + ratio!!,
                    (selectedRatio!!.toInt() * 12).toDouble()
                ) - 1) / (ratio * (ratio + 1).pow((selectedRatio!!.toInt() * 12).toDouble()))
                monthlyPayment = userPayment / d
            }
            val totalCost = monthlyPayment!! * (selectedRatio!!.toInt() * 12)
            val x: Double = (monthlyPayment!! - monthlyPayment!!) * (selectedRatio!!.toInt() * 12)
            val profit = totalCost - userPayment - x
            val newDownPayment = x + downPayment
            val difference =
                (monthlyPayment!!.toDouble() - monthlyPayment!!.toInt()) * (selectedRatio!!.toDouble() * 12)

            binding.totalCoastValue.text = (totalCost + downPayment).toInt()
                .toString() + " " + resources.getString(R.string.kwd)
            binding.installmentValue.text =
                (selectedRatio!!.toInt() * 12).toString() + " " + resources.getString(R.string.months)
            binding.downPaymentValue.text = "" + Math.ceil(newDownPayment + Math.floor(difference))
                .toInt() + " " + resources.getString(R.string.kwd)
            binding.debitValue.text =
                (monthlyPayment!! * (selectedRatio!!.toDouble() * 12) - Math.floor(difference)).toInt()
                    .toString() + " " + getString(R.string.kwd)
            binding.profitValue.text =
                (profit + Math.ceil(x)).toInt().toString() + " " + getString(R.string.kwd)
            binding.monthlyInstallmentValue.text =
                monthlyPayment!!.toInt().toString() + " " + resources.getString(R.string.kwd)


            viewModel.totalCost = (totalCost + downPayment).toInt().toString()

            viewModel.installmentAmount = monthlyPayment!!.toInt().toString()
            viewModel.downpayment = "" + Math.ceil(newDownPayment + Math.floor(difference))
                .toInt()
            viewModel.profite = "" + (profit + Math.ceil(x)).toInt()
            viewModel.installmentPeriod = (selectedRatio!!.toInt() * 12).toString()
            viewModel.loanAmount =
                (monthlyPayment!! * (selectedRatio!!.toDouble() * 12) - Math.floor(difference)).toInt()
                    .toString()

        }

    }

    private fun isValidAmount(): Boolean {
        return (!binding.amountTxt.text.toString().isNullOrEmpty() &&binding.amountTxt.text.toString().toInt()>0 &&binding.amountTxt.text.toString().toInt()> selectedBank?.downPayment?.replace(".", "")?.toInt() ?: 0)
    }

    private fun setUnselectedYears() {
        one_container.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.round_background_light_gray)
        one_year_text.setTextColor(ContextCompat.getColor(requireContext(), (R.color.colorBlack)))
        two_container.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.round_background_light_gray)
        two_year_text.setTextColor(ContextCompat.getColor(requireContext(), (R.color.colorBlack)))
        three_container.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.round_background_light_gray)
        three_year_text.setTextColor(ContextCompat.getColor(requireContext(), (R.color.colorBlack)))
        four_container.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.round_background_light_gray)
        four_year_text.setTextColor(ContextCompat.getColor(requireContext(), (R.color.colorBlack)))
        five_container.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.round_background_light_gray)
        five_year_text.setTextColor(ContextCompat.getColor(requireContext(), (R.color.colorBlack)))
    }


    private fun observeData() {
        viewModel.noConnectionError.observe(viewLifecycleOwner,  androidx.lifecycle.Observer {
            if (it){
                DesignUtils.instance.showErrorDialog(requireContext(), null,null){

                }
            }
        })
        viewModel.banks.observe(viewLifecycleOwner, Observer { banks ->
            bankAdapter.refreshActions(banks)
            if (!banks.isNullOrEmpty()) {
                val b = banks[0]
                installmentAdapter.refreshActions(b.ratio)
                val simplifier = BankSimplifier(b)
                binding.bank = simplifier
                banks_recycler.scrollToPosition(0)
            }
            //if (banks.isNotEmpty()) banks_recycler.scrollToPosition(0)
        })

        viewModel.error.observe(viewLifecycleOwner, Observer {

        })

        viewModel.loading.observe(viewLifecycleOwner, Observer {

        })
    }
}