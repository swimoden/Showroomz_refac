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
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.CallbackSuccessBottomSheetBinding
import com.kuwait.showroomz.databinding.ErrorBottomSheetBinding
import com.kuwait.showroomz.databinding.FinanceCallbackBottomSheetBinding
import com.kuwait.showroomz.databinding.FragmentFinanceBinding
import com.kuwait.showroomz.extras.*
import com.kuwait.showroomz.model.data.Bank
import com.kuwait.showroomz.model.data.Model
import com.kuwait.showroomz.model.data.Trim
import com.kuwait.showroomz.model.repository.LogProgressRepository
import com.kuwait.showroomz.model.simplifier.ApplyForFinanceParams
import com.kuwait.showroomz.model.simplifier.BankSimplifier
import com.kuwait.showroomz.model.simplifier.ModelSimplifier
import com.kuwait.showroomz.model.simplifier.TrimSimplifier
import com.kuwait.showroomz.view.MainActivity
import com.kuwait.showroomz.view.adapters.BanksAdapter
import com.kuwait.showroomz.view.adapters.PeriodInstallmentAdapter
import com.kuwait.showroomz.viewModel.FinanceVM
import kotlinx.android.synthetic.main.fragment_finance.*
import kotlinx.android.synthetic.main.fragment_finance.apply_now_btn
import kotlinx.android.synthetic.main.fragment_finance.back_bnt
import kotlinx.android.synthetic.main.fragment_finance.banks_recycler
import kotlinx.android.synthetic.main.fragment_finance.calculate_btn
import kotlinx.android.synthetic.main.fragment_finance.calculator_container
import kotlinx.android.synthetic.main.fragment_finance.callback_btn
import kotlinx.android.synthetic.main.fragment_finance.down_img
import kotlinx.android.synthetic.main.fragment_finance.down_payment_txt
import kotlinx.android.synthetic.main.fragment_finance.five_container
import kotlinx.android.synthetic.main.fragment_finance.five_year_text
import kotlinx.android.synthetic.main.fragment_finance.four_container
import kotlinx.android.synthetic.main.fragment_finance.four_year_text
import kotlinx.android.synthetic.main.fragment_finance.installment_period_container
import kotlinx.android.synthetic.main.fragment_finance.more_img
import kotlinx.android.synthetic.main.fragment_finance.one_container
import kotlinx.android.synthetic.main.fragment_finance.one_year_text
import kotlinx.android.synthetic.main.fragment_finance.result_container
import kotlinx.android.synthetic.main.fragment_finance.three_container
import kotlinx.android.synthetic.main.fragment_finance.three_year_text
import kotlinx.android.synthetic.main.fragment_finance.two_container
import kotlinx.android.synthetic.main.fragment_finance.two_year_text
import kotlinx.android.synthetic.main.fragment_request_amount.*
import kotlin.math.pow
import kotlin.math.roundToInt


class FinanceFragment : Fragment() {

    private var monthlyPayment: Double? = null
    private lateinit var trimSimplifier: TrimSimplifier
    private lateinit var modelSimplifier: ModelSimplifier
    lateinit var binding: FragmentFinanceBinding
    private lateinit var viewModel: FinanceVM
    private var banks: List<Bank> = arrayListOf()
    private var ratios: List<Double> = arrayListOf()
    private var selectedBank: Bank? = null
    private var selectedRatio: String? = null
    private lateinit var model: Model
    private val successCallbackDialog: BottomSheetDialog by lazy {
        BottomSheetDialog(
            requireContext(),
            R.style.BottomSheetDialog
        )
    }
    private val dialogCallback: BottomSheetDialog by lazy {
        BottomSheetDialog(
            requireContext(),
            R.style.BottomSheetDialog
        )
    }
    private val bankAdapter: BanksAdapter by lazy {
        BanksAdapter(banks, context)
    }
    private val installmentAdapter: PeriodInstallmentAdapter by lazy {
        PeriodInstallmentAdapter(ratios, context)
    }
    var dialog = SimpleDialog(null, null)
    override fun onResume() {
        super.onResume()

        LogProgressRepository.logProgress(
            "Calculator_screen",
            category = modelSimplifier.brand?.cat?.id ?: "",
            dealerData = modelSimplifier.brand?.id ?: "",
            modelData = modelSimplifier.id,
            trim = if (::trimSimplifier.isInitialized) trimSimplifier.id else ""
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_finance, container, false)
        binding.root.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(FinanceVM::class.java)

        arguments?.let {
            model = FinanceFragmentArgs.fromBundle(it).model
            modelSimplifier = ModelSimplifier(FinanceFragmentArgs.fromBundle(it).model)
            viewModel.modelId = modelSimplifier.id
            val category = modelSimplifier.brand?.category
            category?.isCivilIdMandatory?.let { it1 ->
                viewModel.isCivilIdMandatory.set(
                    it1
                )
            }
            category?.isKFh?.let { it1 ->
                viewModel.isKfh.set(
                    it1
                )
            }
            if (FinanceFragmentArgs.fromBundle(it).bank == null) {
                viewModel.getBanks()
            } else {
                var list = arrayListOf<Bank>()
                list.add(FinanceFragmentArgs.fromBundle(it).bank!!)
                bankAdapter.refreshActions(list)

                bankAdapter.selectedItem = 0
                bankAdapter.lastSelected = 0
                selectedBank = FinanceFragmentArgs.fromBundle(it).bank!!
                binding.bank = BankSimplifier(selectedBank)
                selectedBank?.let {
                    val x:ArrayList<Bank> = arrayListOf()
                    x.add(it)
                    CacheObjects.selectedBanks=x
                }

                if (FinanceFragmentArgs.fromBundle(it).bank!!.downPayment != "0") {
                    binding.downPaymentTxt.setText(FinanceFragmentArgs.fromBundle(it).bank!!.downPayment)
                }
                //calculate()
            }
            //if (!(::trimSimplifier.isInitialized)) {
                if (FinanceFragmentArgs.fromBundle(it).trim == null) {
                    var trim = Trim()
                    trim.price = Utils.instance.removeUnitFromPriceString(modelSimplifier.price).toInt()
                    trimSimplifier = TrimSimplifier(trim)
                } else trimSimplifier = FinanceFragmentArgs.fromBundle(it).trim!!
                binding.trim = trimSimplifier
                binding.model = modelSimplifier
            calculate()
            //}
        }

        DesignUtils.instance.setStatusBar(
            requireActivity() as MainActivity,
            false,
            modelSimplifier.brand?.category?.setBgColor()
        )
        if (!selectedBank.isNull() && !selectedRatio.isNullOrEmpty()) {
            calculator_container.visibility = View.VISIBLE
            calculate_btn.visibility = View.INVISIBLE
            callback_btn.isVisible = true
            apply_now_btn.isVisible = true
            selectedBank?.let {
                val x:ArrayList<Bank> = arrayListOf()
                x.add(it)
                CacheObjects.selectedBanks=x
            }
        } else {
            calculator_container.visibility = View.GONE
            //calculate_btn.isVisible = true
            callback_btn.visibility = View.INVISIBLE
            apply_now_btn.visibility = View.INVISIBLE

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
        observeData()
        onClickListener()
    }

    private fun onClickListener() {
        calculate_btn.setOnClickListener {
            if (!down_payment_txt.text.toString()
                    .isNullOrEmpty() && down_payment_txt.text.toString()
                    .toBigDecimal() > trimSimplifier.price.let { it1 ->
                    Utils.instance.removeUnitFromPriceString(
                        it1
                    ).toBigDecimal()
                }
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

        }
        back_bnt.setOnClickListener {
            if (Navigation.findNavController(it).previousBackStackEntry?.destination?.id == R.id.splashFragment){
                val navHostFragment = activity?.supportFragmentManager?.findFragmentById(R.id.fragment) as NavHostFragment
                val navController = navHostFragment.navController
                navController.navigate(R.id.dashboardFragment)
            }else{
                Navigation.findNavController(it).navigateUp()
            }
           // Navigation.findNavController(it).navigateUp()
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
                selectedBank?.let {
                    val x:ArrayList<Bank> = arrayListOf()
                    x.add(it)
                    CacheObjects.selectedBanks=x
                }
                binding.bank = BankSimplifier(selectedBank)
                viewModel.bankId = bank.id
                if (bank.downPayment != "0.000" &&  binding.downPaymentTxt.text.isEmpty()) {
                    binding.downPaymentTxt.setText(bank.downPayment)
                }
                calculate()
            }
        })
        callback_btn.setOnClickListener {
            showCallbackBottomSheet()

        }

        binding.applyNowBtn.setOnClickListener {
            var params = ApplyForFinanceParams(
                viewModel.downpayment,
                viewModel.loanAmount,
                viewModel.installmentPeriod,
                viewModel.totalCost,
                viewModel.installmentAmount,
                viewModel.profite,
                viewModel.modelId,
                viewModel.bankId
            )
            viewModel.prefs.setParcelable("APPLY_FINANCE", params)
            if (selectedBank?.allowSemiApproval == true && !viewModel.isCivilIdMandatory.get()) {
                if (viewModel.isConnected()) {

                    Navigation.findNavController(it).navigate(
                        FinanceFragmentDirections.showApplyForFinanceStepOne(
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
                        FinanceFragmentDirections.showCallbackBank(
                        )
                    )

                } else {
                    Navigation.findNavController(it)
                        .navigate(FinanceFragmentDirections.showLogin(R.id.applyForFinance))
                }
            }


        }


    }

    private fun showCallbackBottomSheet() {
        LogProgressRepository.logProgress(
            "Finance_callback_screen",
            category = modelSimplifier.brand?.cat?.id ?: "",
            dealerData = modelSimplifier.brand?.id ?: "",
            modelData = modelSimplifier.id,
            trim = if (::trimSimplifier.isInitialized) trimSimplifier.id else ""
        )
        var binding: FinanceCallbackBottomSheetBinding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.finance_callback_bottom_sheet,
            null,
            false
        )
        binding.viewModel = viewModel
        binding.model = modelSimplifier
        binding.checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
            viewModel.acceptCondition.set(isChecked)
        }
        dialogCallback.setContentView(binding.root)
        dialogCallback.show()
        binding.exitBtn.setOnClickListener {
            dialogCallback.dismiss()

        }

    }


    private fun calculate() {

        if (!down_payment_txt.text.toString()
                .isNullOrEmpty() && down_payment_txt.text.toString()
                .toBigDecimal() > Utils.instance.removeUnitFromPriceString(trimSimplifier.price)
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
        if (!selectedBank.isNull() && !selectedRatio.isNullOrEmpty()) {
            calculator_container.visibility = View.VISIBLE
            calculate_btn.visibility = View.INVISIBLE
            callback_btn.isVisible = true
            apply_now_btn.isVisible = true
            val ratio =
                if (selectedBank!!.ratio.size > 1) selectedBank!!.ratio[selectedRatio!!.toInt() - 1]
                else (if (selectedBank!!.ratio.isNotEmpty()) selectedBank!!.ratio[0] else 0.0 )
            val downPayment = if (down_payment_txt.text.toString().isNullOrEmpty()) {
                if (selectedBank?.downPayment.isNullOrEmpty()) 0.0 else selectedBank?.downPayment?.toDouble()!!
            } else down_payment_txt.text.toString().toDouble()

            val userPayment = Utils.instance.removeUnitFromPriceString(trimSimplifier.price)
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
            selectedBank?.let {
                LogProgressRepository.logProgress("Calculator_screen_calculate","","","","","",it.id)
            }
        }

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
        viewModel.acceptConditionError.observe(viewLifecycleOwner, Observer {
            if (it) {
                showErrorDialog(getString(R.string.accept_conditions))
                viewModel.acceptConditionError.value = false
            }
        })
        viewModel.banks.observe(viewLifecycleOwner, Observer { banks ->

            bankAdapter.refreshActions(banks)

            installmentAdapter.refreshActions(banks[0].ratio)
            val simplifier = BankSimplifier(banks[0])
            binding.bank = simplifier

            if (banks.isNotEmpty()) banks_recycler.scrollToPosition(0)
        })

        viewModel.error.observe(viewLifecycleOwner, Observer {

        })

        viewModel.loading.observe(viewLifecycleOwner, Observer {

        })
        viewModel.successCallback.observe(viewLifecycleOwner, Observer {

            if (it) {
                viewModel.successCallback.value = false
                showSuccessCallbackBottomSheet()

            }
        })
        viewModel.civilIdError.observe(viewLifecycleOwner, Observer {
            if (it) {
                showErrorDialog(getString(R.string.invalid_civil_id))
            }
        })
        viewModel.phoneError.observe(viewLifecycleOwner, Observer {
            if (it) {
                showErrorDialog(getString(R.string.invalid_phone))
            }
        })
        viewModel.nameError.observe(viewLifecycleOwner, Observer {
            if (it) {
                showErrorDialog(getString(R.string.empty_name))
            }
        })
        viewModel.callbackLoading.observe(viewLifecycleOwner, Observer {
            if (it) {
                dialogCallback.dismiss()
                binding.progressCircularCallback.isVisible = true
                activity?.let { it1 -> DesignUtils.instance.disableUserInteraction(it1) }
            } else {
                binding.progressCircularCallback.isVisible = false
                activity?.let { it1 -> DesignUtils.instance.enableUserInteraction(it1) }
            }

        })

        /*viewModel.verifyPhone.observe(viewLifecycleOwner, Observer {
            if (it) {
                viewModel.phoneNumber.get()?.let { it1 ->
                    dialog = SimpleDialog.emptyInstance(it1, {
                        viewModel.verifyPhone.value = true
                        viewModel.requestCallback()
                        dialog.dismiss()
                    },{
                        viewModel.verifyPhone.value = false
                    })
                } ?: kotlin.run { return@Observer }
                fragmentManager?.let { dialog.show(it, SimpleDialog.TAG) }

            }

        })*/
    }

    private fun showSuccessCallbackBottomSheet() {
        val bindingSuccess: CallbackSuccessBottomSheetBinding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.callback_success_bottom_sheet,
            null,
            false
        )
        successCallbackDialog.setContentView(bindingSuccess.root)
        successCallbackDialog.setCanceledOnTouchOutside(false)
        bindingSuccess.titleText.text = getString(R.string.done_successfully)
        bindingSuccess.messageText.text = getString(R.string.representative_contact)


        successCallbackDialog.show()

        bindingSuccess.exitBtn.setOnClickListener {
            dialogCallback.dismiss()
            successCallbackDialog.dismiss()

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